import React, { useEffect, useState } from "react";
import { Container, Card, Spinner, Form, Button, Alert } from "react-bootstrap";
import axios from "axios";
import ChangePassword from "./ChangePassword.jsx";

const Profile = () => {
    const [user, setUser] = useState(null);
    const [countries, setCountries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isEditing, setIsEditing] = useState(false);
    const [formData, setFormData] = useState({});
    const [message, setMessage] = useState({ type: "", text: "" });
    const [showPassModal, setShowPassModal] = useState(false);
    const [hasRead, setHasRead] = useState(false); // État pour forcer le dépliage

    const API_URL = "http://localhost:8084";
    const token = localStorage.getItem("token");
    const config = { headers: { Authorization: `Bearer ${token}` } };

    useEffect(() => {
        const loadData = async () => {
            try {
                const [userRes, countriesRes] = await Promise.all([
                    axios.get(`${API_URL}/account`, config),
                    axios.get(`${API_URL}/countries`, config)
                ]);
                setUser(userRes.data);
                setFormData(userRes.data);
                setCountries(countriesRes.data);
            } catch (error) {
                setMessage({ type: "danger", text: "Erreur de chargement des données" });
            } finally {
                setLoading(false);
            }
        };
        loadData();
    }, []);

    const handleUpdate = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.put(`${API_URL}/account/settings`, formData, config);

            setUser(response.data.user);

            if (response.data.token) {
                localStorage.setItem("token", response.data.token);
                setMessage({ type: "success", text: "Profil et identifiants mis à jour !" });
            } else {
                setMessage({ type: "success", text: "Profil mis à jour !" });
            }

            setIsEditing(false);
        } catch (error) {
            const errorMsg = error.response?.data?.message || "Échec de la mise à jour.";
            setMessage({ type: "danger", text: errorMsg });
        }
    };

    const handleSignCharter = async () => {
        try {
            await axios.post(`${API_URL}/account/sign-charter`, {}, config);
            setUser({ ...user, hasSignedCharter: true, charterSignedAt: new Date().toISOString() });
            setMessage({ type: "success", text: "Charte signée avec succès !" });
        } catch (error) {
            setMessage({ type: "danger", text: "Erreur lors de la signature." });
        }
    };

    if (loading) return <Container className="text-center pt-5"><Spinner animation="border" /></Container>;

    return (
        <Container className="pt-4">
            <div className="text-center mb-4">
                <h2 className="fw-bold">Mon Profil</h2>
            </div>

            {message.text && <Alert variant={message.type} dismissible>{message.text}</Alert>}

            <div className="d-flex justify-content-center">
                <Card className="shadow-sm" style={{maxWidth: "500px", width: "100%"}}>
                    <Card.Body className="p-4">
                        {!isEditing ? (
                            <>
                                <h5>{user.name} {user.lastname}</h5>
                                <p className="text-muted">{user.email}</p>
                                <p><strong>Pays :</strong> {user.countryCode || "Non renseigné"}</p>
                                <Button variant="outline-secondary" onClick={() => setIsEditing(true)}
                                        className="w-100">Modifier le profil</Button>
                                <Button variant="outline-danger" onClick={() => setShowPassModal(true)}
                                        className="w-100 mt-2">Changer le mot de passe</Button>
                            </>
                        ) : (
                            <Form onSubmit={handleUpdate}>
                                <Form.Group className="mb-2">
                                    <Form.Label>Email</Form.Label>
                                    <Form.Control
                                        type="email"
                                        value={formData.email || ""}
                                        onChange={e => setFormData({...formData, email: e.target.value})}
                                    />
                                    <Form.Text className="text-muted">
                                        Attention : changer votre email modifiera vos identifiants de connexion.
                                    </Form.Text>
                                </Form.Group>
                                <Form.Group className="mb-2">
                                    <Form.Label>Prénom</Form.Label>
                                    <Form.Control value={formData.name || ""}
                                                  onChange={e => setFormData({...formData, name: e.target.value})}/>
                                </Form.Group>
                                <Form.Group className="mb-2">
                                    <Form.Label>Nom</Form.Label>
                                    <Form.Control value={formData.lastname || ""}
                                                  onChange={e => setFormData({...formData, lastname: e.target.value})}/>
                                </Form.Group>
                                <Form.Group className="mb-3">
                                    <Form.Label>Pays</Form.Label>
                                    <Form.Select
                                        value={formData.countryCode || ""}
                                        onChange={e => setFormData({...formData, countryCode: e.target.value})}
                                    >
                                        <option value="">Sélectionner...</option>
                                        {countries.map(code => <option key={code} value={code}>{code}</option>)}
                                    </Form.Select>
                                </Form.Group>
                                <div className="d-flex gap-2">
                                    <Button variant="success" type="submit" className="flex-grow-1">Sauvegarder</Button>
                                    <Button variant="link" onClick={() => setIsEditing(false)}>Annuler</Button>
                                </div>
                            </Form>
                        )}
                    </Card.Body>
                </Card>

                {user.role === "ATHLETE" && (
                    <Card className="shadow-sm border-primary mt-4" style={{ maxWidth: "700px", margin: "auto" }}>
                        <Card.Header className="bg-primary text-white text-center">
                            <h5 className="mb-0">Charte Européenne du Sport</h5>
                        </Card.Header>
                        <Card.Body>
                            <p className="text-muted small">
                                Conformément à la Recommandation CM/Rec(2021)5[cite: 8], veuillez lire les principes fondamentaux
                                avant de confirmer votre engagement.
                            </p>

                            <Accordion onSelect={() => setHasRead(true)} className="mb-3">
                                <Accordion.Item eventKey="0">
                                    <Accordion.Header>Lire le texte de la Charte révisée (2021)</Accordion.Header>
                                    <Accordion.Body style={{ maxHeight: "400px", overflowY: "auto", fontSize: "0.9rem" }}>
                                        <div className="text-center mb-3">
                                            <strong>ANNEXE À LA RECOMMANDATION CM/REC(2021)5</strong> [cite: 59]
                                        </div>

                                        <h6>Article 1 – But de la charte</h6>
                                        <p>
                                            La présente charte a pour but de donner aux gouvernements des orientations pour mettre en valeur les bénéfices du sport sur les plans individuel et social (santé, inclusion, éducation).
                                            Elle vise à protéger et développer un sport fondé sur des valeurs et les droits de l'homme[cite: 78, 80].
                                        </p>

                                        <h6>Article 2 – Définition du sport</h6>
                                        <p>
                                            On entend par « sport » toutes formes d'activités physiques qui ont pour objectif le maintien ou l'amélioration de la condition physique et psychique, le développement des relations sociales ou l'obtention de résultats en compétition.
                                        </p>

                                        <h6>Article 6 – Droits de l'homme</h6>
                                        <p>
                                            Toutes les parties prenantes doivent respecter et protéger les droits de l'homme[cite: 144].
                                            Cela inclut la lutte contre toutes les formes de discrimination (race, genre, orientation sexuelle, etc.) et une politique de tolérance zéro face à la violence[cite: 156, 213].
                                        </p>

                                        <h6>Article 8 – Intégrité</h6>
                                        <p>
                                            L'intégrité du sport englobe l'aspect personnel, la compétition et l'organisation[cite: 174].
                                            Elle implique la lutte contre la corruption, la manipulation de compétitions et le dopage[cite: 175, 185].
                                        </p>

                                        <h6>Article 10 – Droit au sport</h6>
                                        <p>
                                            L'accès au sport pour tous est considéré comme un droit fondamental[cite: 209].
                                            Tout être humain a le droit inaliénable d'accéder au sport dans un environnement sain[cite: 210].
                                        </p>

                                        <hr />
                                        <div className="bg-light p-2 rounded text-center">
                                            <p className="mb-0 small">
                                                Vous pouvez consulter la version intégrale de la charte sur le site du Conseil de l'Europe :
                                            </p>
                                            <a
                                                href="https://www.coe.int/fr/web/sport/european-sports-charter"
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="fw-bold"
                                            >
                                                www.coe.int/fr/web/sport/european-sports-charter
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
                                    // Bloqué si déjà signé OU si n'a pas encore ouvert l'accordéon
                                    disabled={user.hasSignedCharter || !hasRead}
                                    checked={user.hasSignedCharter}
                                    onChange={handleSignCharter}
                                    className={user.hasSignedCharter ? "text-success fw-bold" : ""}
                                />
                                {user.hasSignedCharter && (
                                    <div className="mt-2 small text-success">
                                        ✅ Signée numériquement
                                    </div>
                                )}
                                {!hasRead && !user.hasSignedCharter && (
                                    <div className="mt-1 x-small text-danger" style={{fontSize: '0.8rem'}}>
                                        * Veuillez déplier et lire la charte pour pouvoir cocher.
                                    </div>
                                )}
                            </div>
                        </Card.Body>
                    </Card>
                )}
            </div>
            <ChangePassword show={showPassModal} handleClose={() => setShowPassModal(false)}/>
        </Container>
    );
};

export default Profile;