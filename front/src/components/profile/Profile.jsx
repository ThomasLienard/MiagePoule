import React, { useEffect, useState } from "react";
import {
  Container,
  Card,
  Spinner,
  Form,
  Button,
  Alert,
  Tab,
  Tabs,
  Accordion,
  Badge,
} from "react-bootstrap";
import { useAuth } from "../../contexts/AuthContext";
import { AlertCircle, User, FileText, FileSignature } from "lucide-react";
import axios from "axios";
import ChangePassword from "./ChangePassword.jsx";
import DocumentUpload from "./DocumentUpload.jsx";
import documentService from "../../services/documentService.jsx";
import authService from "../../services/authService.jsx";

const Profile = () => {
  const { isAccountValidated, setIsAccountValidated } = useAuth();
  const [user, setUser] = useState(null);
  const [userRole, setUserRole] = useState(null);
  const [countries, setCountries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({});
  const [message, setMessage] = useState({ type: "", text: "" });
  const [showPassModal, setShowPassModal] = useState(false);
  const [documents, setDocuments] = useState({});
  const [activeTab, setActiveTab] = useState("profile");
  const [hasRead, setHasRead] = useState(false); // État pour forcer le dépliage de la charte
  const API_URL = import.meta.env.VITE_API_URL;
  const token = localStorage.getItem("token");
  const config = { headers: { Authorization: `Bearer ${token}` } };

  // Mapper les IDs de type de document
  const documentTypeIds = {
    CEN_ACCREDITATION: 2,
    PASSPORT: 3,
    MEDICAL_CERTIFICATE: 4,
  };

  useEffect(() => {
    const loadData = async () => {
      try {
        // Récupérer le rôle depuis le token JWT
        const authUser = authService.getUser();
        console.log("Auth User depuis token:", authUser);
        if (authUser && authUser.roles && authUser.roles.length > 0) {
          setUserRole(authUser.roles[0]);
          console.log("Rôle détecté:", authUser.roles[0]);
        }

        const [userRes, countriesRes] = await Promise.all([
          axios.get(`${API_URL}/account`, config),
          axios.get(`${API_URL}/countries`, config),
        ]);
        console.log("Données utilisateur complètes:", userRes.data);
        setUser(userRes.data);
        setFormData(userRes.data);
        setCountries(countriesRes.data);

        // Charger les documents existants
        await loadDocuments();

        // Déterminer l'onglet par défaut
        const roleName = authUser?.roles?.[0]?.toUpperCase();

        // Si ATHLETE et charte non signée, afficher onglet charte
        if (roleName === "ATHLETE" && !userRes.data.hasSignedCharter) {
          setActiveTab("charter");
        }
        // Sinon si le compte n'est pas validé et nécessite des documents, afficher onglet documents
        else if (
          !userRes.data.isAccountValidated &&
          (roleName === "VOLONTAIRE" ||
            roleName === "COMMISSAIRE" ||
            roleName === "ATHLETE" ||
            roleName === "SPORTIF")
        ) {
          setActiveTab("documents");
        }
      } catch (err) {
        console.error(err);
        setMessage({
          type: "danger",
          text: "Erreur de chargement des données",
        });
      } finally {
        setLoading(false);
      }
    };
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadDocuments = async () => {
    try {
      const allDocs = await documentService.getUserDocuments();
      const docMap = {};
      allDocs.forEach((doc) => {
        docMap[doc.typeName] = doc;
      });
      setDocuments(docMap);
    } catch (error) {
      console.error("Erreur de chargement des documents:", error);
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.put(
        `${API_URL}/account/settings`,
        formData,
        config,
      );

      setUser(response.data.user);

      if (response.data.token) {
        localStorage.setItem("token", response.data.token);
        setMessage({
          type: "success",
          text: "Profil et identifiants mis à jour !",
        });
      } else {
        setMessage({ type: "success", text: "Profil mis à jour !" });
      }

      setIsEditing(false);
    } catch (error) {
      const errorMsg =
        error.response?.data?.message || "Échec de la mise à jour.";
      setMessage({ type: "danger", text: errorMsg });
    }
  };

  const handleDocumentUploadSuccess = async () => {
    setMessage({
      type: "success",
      text: "Document téléversé avec succès ! Votre compte sera vérifié par un administrateur.",
    });
    await loadDocuments();

    // Vérifier si tous les documents requis sont uploadés
    await checkAccountValidation();
  };

  const checkAccountValidation = async () => {
    try {
      // Recharger les infos utilisateur pour vérifier le statut
      const userRes = await axios.get(`${API_URL}/account`, config);
      if (userRes.data.isAccountValidated) {
        setIsAccountValidated(true);
        localStorage.setItem("isAccountValidated", "true");
      }
    } catch (error) {
      console.error("Erreur de vérification du compte:", error);
    }
  };

  const handleSignCharter = async () => {
    try {
      await axios.post(`${API_URL}/account/sign-charter`, {}, config);
      setUser({
        ...user,
        hasSignedCharter: true,
        charterSignedAt: new Date().toISOString(),
      });
      setMessage({ type: "success", text: "Charte signée avec succès !" });
    } catch (error) {
      setMessage({ type: "danger", text: "Erreur lors de la signature." });
    }
  };

  const getRequiredDocuments = () => {
    if (!userRole) {
      console.log("Aucun rôle défini");
      return [];
    }

    const roleName = userRole.toUpperCase();
    console.log("Rôle normalisé pour vérification:", roleName);

    if (roleName === "VOLONTAIRE" || roleName === "COMMISSAIRE") {
      return [
        {
          type: "CEN_ACCREDITATION",
          typeId: documentTypeIds["CEN_ACCREDITATION"],
          label: "Accréditation CEN",
          description:
            "Déposez votre accréditation CEN (format PDF, JPG ou PNG, max 10 MB)",
        },
      ];
    } else if (roleName === "ATHLETE" || roleName === "SPORTIF") {
      return [
        {
          type: "MEDICAL_CERTIFICATE",
          typeId: documentTypeIds["MEDICAL_CERTIFICATE"],
          label: "Certificat médical",
          description:
            "Déposez votre certificat médical à jour (format PDF, JPG ou PNG, max 10 MB)",
        },
        {
          type: "PASSPORT",
          typeId: documentTypeIds["PASSPORT"],
          label: "Passeport",
          description:
            "Déposez le scan de votre passeport (format PDF, JPG ou PNG, max 10 MB)",
        },
      ];
    }

    return [];
  };

  const requiredDocuments = getRequiredDocuments();
  const hasRequiredDocuments = requiredDocuments.length > 0;

  if (loading)
    return (
      <Container className="text-center pt-5">
        <Spinner animation="border" />
      </Container>
    );

  return (
    <Container className="pt-4" style={{ maxWidth: "800px" }}>
      {message.text && (
        <Alert
          variant={message.type}
          dismissible
          onClose={() => setMessage({ type: "", text: "" })}
        >
          {message.text}
        </Alert>
      )}

      {!isAccountValidated && hasRequiredDocuments && (
        <Alert variant="warning" className="d-flex align-items-center mb-4">
          <AlertCircle size={24} className="me-2" />
          <div>
            <strong>Compte non validé</strong>
            <p className="mb-0 mt-1">
              Vous devez déposer vos documents d'accréditation pour activer
              votre compte. Une fois vos documents soumis, un administrateur les
              vérifiera.
            </p>
          </div>
        </Alert>
      )}

      <Card className="shadow-sm">
        <Card.Body className="p-4">
          <Tabs
            activeKey={activeTab}
            onSelect={(k) => setActiveTab(k)}
            className="mb-4"
          >
            <Tab
              eventKey="profile"
              title={
                <span>
                  <User size={16} className="me-1" />
                  Profil
                </span>
              }
            >
              <div className="mt-3">
                {!isEditing ? (
                  <>
                    <h5>
                      {user.name} {user.lastname}
                    </h5>
                    <p className="text-muted">{user.email}</p>
                    <p>
                      <strong>Pays :</strong>{" "}
                      {user.countryCode || "Non renseigné"}
                    </p>
                    <p>
                      <strong>Rôle :</strong> {userRole || "Non renseigné"}
                    </p>
                    {user.isAccountValidated !== undefined && (
                      <p>
                        <strong>Statut :</strong>{" "}
                        <span
                          className={
                            user.isAccountValidated
                              ? "text-success"
                              : "text-warning"
                          }
                        >
                          {user.isAccountValidated
                            ? "✓ Compte validé"
                            : "⏳ En attente de validation"}
                        </span>
                      </p>
                    )}
                    <Button
                      variant="outline-secondary"
                      onClick={() => setIsEditing(true)}
                      className="w-100 mt-3"
                    >
                      Modifier le profil
                    </Button>
                    <Button
                      variant="outline-danger"
                      onClick={() => setShowPassModal(true)}
                      className="w-100 mt-2"
                    >
                      Changer le mot de passe
                    </Button>
                  </>
                ) : (
                  <Form onSubmit={handleUpdate}>
                    <Form.Group className="mb-2">
                      <Form.Label>Email</Form.Label>
                      <Form.Control
                        type="email"
                        value={formData.email || ""}
                        onChange={(e) =>
                          setFormData({ ...formData, email: e.target.value })
                        }
                      />
                      <Form.Text className="text-muted">
                        Attention : changer votre email modifiera vos
                        identifiants de connexion.
                      </Form.Text>
                    </Form.Group>
                    <Form.Group className="mb-2">
                      <Form.Label>Prénom</Form.Label>
                      <Form.Control
                        value={formData.name || ""}
                        onChange={(e) =>
                          setFormData({ ...formData, name: e.target.value })
                        }
                      />
                    </Form.Group>
                    <Form.Group className="mb-2">
                      <Form.Label>Nom</Form.Label>
                      <Form.Control
                        value={formData.lastname || ""}
                        onChange={(e) =>
                          setFormData({ ...formData, lastname: e.target.value })
                        }
                      />
                    </Form.Group>
                    <Form.Group className="mb-3">
                      <Form.Label>Pays</Form.Label>
                      <Form.Select
                        value={formData.countryCode || ""}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            countryCode: e.target.value,
                          })
                        }
                      >
                        <option value="">Sélectionner...</option>
                        {countries.map((code) => (
                          <option key={code} value={code}>
                            {code}
                          </option>
                        ))}
                      </Form.Select>
                    </Form.Group>
                    <div className="d-flex gap-2">
                      <Button
                        variant="success"
                        type="submit"
                        className="flex-grow-1"
                      >
                        Sauvegarder
                      </Button>
                      <Button
                        variant="link"
                        onClick={() => setIsEditing(false)}
                      >
                        Annuler
                      </Button>
                    </div>
                  </Form>
                )}
              </div>
            </Tab>

            <Tab
              eventKey="documents"
              title={
                <span>
                  <FileText size={16} className="me-1" />
                  Documents
                  {!isAccountValidated && hasRequiredDocuments && (
                    <span className="badge bg-warning text-dark ms-2">!</span>
                  )}
                </span>
              }
            >
              <div className="mt-3">
                {hasRequiredDocuments ? (
                  <>
                    {user.isAccountValidated ? (
                      // Compte déjà validé - afficher message et documents existants
                      <>
                        <Alert variant="success" className="mb-4">
                          <strong>✓ Compte validé</strong>
                          <p className="mb-0 mt-2">
                            Votre compte a été validé par un administrateur. Vos
                            documents d'accréditation sont enregistrés et vous
                            ne pouvez plus en uploader de nouveaux.
                          </p>
                        </Alert>

                        <h5 className="mb-3">Vos documents enregistrés</h5>
                        {requiredDocuments.map((doc) => {
                          const existingDoc = documents[doc.type];
                          return existingDoc ? (
                            <Card key={doc.type} className="mb-3">
                              <Card.Body>
                                <div className="d-flex justify-content-between align-items-center">
                                  <div>
                                    <h6 className="mb-1">{doc.label}</h6>
                                    <p className="text-muted mb-0 small">
                                      Uploadé le{" "}
                                      {new Date(
                                        existingDoc.uploadedAt,
                                      ).toLocaleDateString("fr-FR")}
                                    </p>
                                  </div>
                                  <span className="badge bg-success">
                                    Validé
                                  </span>
                                </div>
                              </Card.Body>
                            </Card>
                          ) : null;
                        })}
                      </>
                    ) : (
                      // Compte non validé - permettre l'upload
                      <>
                        <h5 className="mb-3">Documents requis</h5>
                        <p className="text-muted mb-4">
                          Déposez vos documents d'accréditation. Ils seront
                          chiffrés et stockés en toute sécurité.
                        </p>

                        {requiredDocuments.map((doc) => (
                          <DocumentUpload
                            key={doc.type}
                            documentType={doc.typeId}
                            label={doc.label}
                            description={doc.description}
                            existingDocument={documents[doc.type]}
                            onUploadSuccess={handleDocumentUploadSuccess}
                          />
                        ))}

                        <Alert variant="info" className="mt-4">
                          <strong>Information :</strong> Une fois tous vos
                          documents déposés, votre compte sera examiné par un
                          administrateur. Vous recevrez une notification une
                          fois votre compte validé.
                        </Alert>
                      </>
                    )}
                  </>
                ) : (
                  <Alert variant="info">
                    <strong>Aucun document requis</strong>
                    <p className="mb-0 mt-2">
                      Votre rôle ({userRole || "Non défini"}) ne nécessite pas
                      de documents d'accréditation spécifiques.
                    </p>
                  </Alert>
                )}
              </div>
            </Tab>

            {/* ONGLET CHARTE (ATHLÈTE UNIQUEMENT) */}
            {userRole === "ATHLETE" && (
              <Tab
                eventKey="charter"
                title={
                  <span>
                    <FileSignature size={16} className="me-1" />
                    Charte Européenne
                    {!user?.hasSignedCharter && (
                      <span className="badge bg-warning text-dark ms-2">!</span>
                    )}
                  </span>
                }
              >
                <div className="mt-3">
                  <Card className="border-secondary">
                    <Card.Header className="bg-secondary text-white text-center">
                      <h5 className="mb-0">Charte Européenne du Sport</h5>
                    </Card.Header>
                    <Card.Body>
                      <p className="text-muted small">
                        Conformément à la Recommandation CM/Rec(2021)5, veuillez
                        lire les principes fondamentaux avant de signer.
                      </p>

                      <Accordion
                        onSelect={() => setHasRead(true)}
                        className="mb-3"
                      >
                        <Accordion.Item eventKey="0">
                          <Accordion.Header>
                            Lire le texte de la Charte révisée (2021)
                          </Accordion.Header>
                          <Accordion.Body
                            style={{
                              maxHeight: "400px",
                              overflowY: "auto",
                              fontSize: "0.9rem",
                            }}
                          >
                            <div className="text-center mb-3">
                              <Badge bg="light" text="dark" className="border">
                                ANNEXE À LA RECOMMANDATION CM/REC(2021)5
                              </Badge>
                              <div className="mt-2 fw-bold text-uppercase">
                                Charte Européenne du Sport (Révisée)
                              </div>
                            </div>

                            <h6 className="fw-bold text-primary">
                              Article 1 – But de la charte
                            </h6>
                            <p>
                              La présente charte a pour but de donner des
                              orientations pour mettre en valeur les bénéfices
                              du sport sur les plans individuel et social. Elle
                              vise à protéger et développer un sport fondé sur
                              des valeurs et les droits de l'homme.
                            </p>

                            <h6 className="fw-bold text-primary">
                              Article 2 – Définition du sport
                            </h6>
                            <p>
                              On entend par « sport » toutes formes d'activités
                              physiques qui, à travers une participation
                              organisée ou non, ont pour objectif l'expression
                              ou l'amélioration de la condition physique et
                              psychique, le développement des relations sociales
                              ou l'obtention de résultats en compétition.
                            </p>

                            <h6 className="fw-bold text-primary">
                              Article 6 – Droits de l'homme
                            </h6>
                            <p>
                              Toutes les parties prenantes du sport doivent
                              respecter et protéger les droits de l'homme et les
                              libertés fondamentales. Cela inclut une politique
                              de <strong>tolérance zéro</strong> face à la
                              violence et aux comportements discriminatoires.
                            </p>

                            <h6 className="fw-bold text-primary">
                              Article 8 – Intégrité du sport
                            </h6>
                            <p>
                              L'intégrité englobe les composantes personnelles,
                              de compétition et organisationnelles. Signer cette
                              charte implique un engagement ferme contre :
                              <ul className="mt-1">
                                <li>
                                  La corruption et la manipulation des
                                  compétitions.
                                </li>
                                <li>Le dopage sous toutes ses formes.</li>
                                <li>
                                  Les mauvais traitements et l'exploitation.
                                </li>
                              </ul>
                            </p>

                            <h6 className="fw-bold text-primary">
                              Article 10 – Droit au sport
                            </h6>
                            <p>
                              L'accès au sport pour tous est considéré comme un
                              droit fondamental. Tout être humain a le droit
                              inaliénable d'accéder au sport dans un
                              environnement sain, sûr et éthique.
                            </p>

                            <h6 className="fw-bold text-primary">
                              Article 11 – Sport et développement durable
                            </h6>
                            <p>
                              Les activités sportives doivent être planifiées et
                              pratiquées de manière à respecter l'environnement
                              et à promouvoir la durabilité sociale et
                              économique.
                            </p>

                            <hr />
                            <div className="text-center mt-3 small bg-light p-2 rounded">
                              Version complète :{" "}
                              <a
                                href="https://www.coe.int/fr/web/sport/european-sports-charter"
                                target="_blank"
                                rel="noopener noreferrer"
                              >
                                coe.int/sport
                              </a>
                            </div>
                          </Accordion.Body>
                        </Accordion.Item>
                      </Accordion>

                      <div className="bg-light p-3 rounded">
                        <Form.Check
                          type="checkbox"
                          id="check-charte"
                          label="Je m'engage à respecter les principes de la Charte Européenne du Sport."
                          disabled={user?.hasSignedCharter || !hasRead}
                          checked={user?.hasSignedCharter || false}
                          onChange={handleSignCharter}
                          className={
                            user?.hasSignedCharter ? "text-success fw-bold" : ""
                          }
                        />
                        {user?.hasSignedCharter && (
                          <div className="mt-2 small text-success">
                            ✅ Signée numériquement le{" "}
                            {new Date(user.charterSignedAt).toLocaleDateString(
                              "fr-FR",
                            )}
                          </div>
                        )}
                      </div>
                    </Card.Body>
                  </Card>
                </div>
              </Tab>
            )}
          </Tabs>
        </Card.Body>
      </Card>

      <ChangePassword
        show={showPassModal}
        handleClose={() => setShowPassModal(false)}
      />
    </Container>
  );
};

export default Profile;
