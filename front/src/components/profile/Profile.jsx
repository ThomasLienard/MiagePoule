import React, { useEffect, useState } from "react";
import {Container, Card, Spinner, Form, Button, Alert, Accordion, Col, Row, Badge} from "react-bootstrap";
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
        <Container className="pt-4 pb-5">
            <div className="text-center mb-4">
                <h2 className="fw-bold">Mon Profil</h2>
            </div>

            {message.text && <Alert variant={message.type} dismissible onClose={() => setMessage({text: ""})}>{message.text}</Alert>}

            <Row className="justify-content-center align-items-start">

                {/* CARTE INFOS PROFIL */}
                <Col lg={5} md={6} className="mb-4">
                    <Card className="shadow-sm">
                        <Card.Body className="p-4">
                            {!isEditing ? (
                                <>
                                    <h5>{user.name} {user.lastname}</h5>
                                    <p className="text-muted">{user.email}</p>
                                    <p><strong>Pays :</strong> {user.countryCode || "Non renseigné"}</p>
                                    <Button variant="outline-secondary" onClick={() => setIsEditing(true)} className="w-100">Modifier le profil</Button>
                                    <Button variant="outline-danger" onClick={() => setShowPassModal(true)} className="w-100 mt-2">Changer le mot de passe</Button>
                                </>
                            ) : (
                                <Form onSubmit={handleUpdate}>
                                    {/* ... tes champs de formulaire ... */}
                                    <div className="d-flex gap-2">
                                        <Button variant="success" type="submit" className="flex-grow-1">Sauvegarder</Button>
                                        <Button variant="link" onClick={() => setIsEditing(false)}>Annuler</Button>
                                    </div>
                                </Form>
                            )}
                        </Card.Body>
                    </Card>
                </Col>

                {/* CARTE CHARTE (ATHLÈTE UNIQUEMENT) */}
                {user.role === "ATHLETE" && (
                    <Col lg={7} md={6}>
                        <Card className="shadow-sm border-secondary">
                            <Card.Header className="bg-secondary text-white text-center">
                                <h5 className="mb-0">Charte Européenne du Sport</h5>
                            </Card.Header>
                            <Card.Body>
                                <p className="text-muted small">
                                    Conformément à la Recommandation CM/Rec(2021)5, veuillez lire les principes fondamentaux avant de signer.
                                </p>

                                <Accordion onSelect={() => setHasRead(true)} className="mb-3">
                                    <Accordion.Item eventKey="0">
                                        <Accordion.Header>Lire le texte de la Charte révisée (2021)</Accordion.Header>
                                        <Accordion.Body
                                            style={{maxHeight: "400px", overflowY: "auto", fontSize: "0.9rem"}}>
                                            <div className="text-center mb-3">
                                                <Badge bg="light" text="dark" className="border">
                                                    ANNEXE À LA RECOMMANDATION CM/REC(2021)5
                                                </Badge>
                                                <div className="mt-2 fw-bold text-uppercase">Charte Européenne du Sport
                                                    (Révisée)
                                                </div>
                                            </div>

                                            <h6 className="fw-bold text-primary">Article 1 – But de la charte</h6>
                                            <p>
                                                La présente charte a pour but de donner des orientations pour mettre en
                                                valeur les bénéfices du sport sur les plans individuel et social. Elle
                                                vise à protéger et développer un sport fondé sur des valeurs et les
                                                droits de l'homme.
                                            </p>

                                            <h6 className="fw-bold text-primary">Article 2 – Définition du sport</h6>
                                            <p>
                                                On entend par « sport » toutes formes d'activités physiques qui, à
                                                travers une participation organisée ou non, ont pour objectif
                                                l'expression ou l'amélioration de la condition physique et psychique, le
                                                développement des relations sociales ou l'obtention de résultats en
                                                compétition.
                                            </p>

                                            <h6 className="fw-bold text-primary">Article 6 – Droits de l'homme</h6>
                                            <p>
                                                Toutes les parties prenantes du sport doivent respecter et protéger les
                                                droits de l'homme et les libertés fondamentales. Cela inclut une
                                                politique de <strong>tolérance zéro</strong> face à la violence et aux
                                                comportements discriminatoires.
                                            </p>

                                            <h6 className="fw-bold text-primary">Article 8 – Intégrité du sport</h6>
                                            <p>
                                                L'intégrité englobe les composantes personnelles, de compétition et
                                                organisationnelles. Signer cette charte implique un engagement ferme
                                                contre :
                                                <ul className="mt-1">
                                                    <li>La corruption et la manipulation des compétitions.</li>
                                                    <li>Le dopage sous toutes ses formes.</li>
                                                    <li>Les mauvais traitements et l'exploitation.</li>
                                                </ul>
                                            </p>

                                            <h6 className="fw-bold text-primary">Article 10 – Droit au sport</h6>
                                            <p>
                                                L'accès au sport pour tous est considéré comme un droit fondamental.
                                                Tout être humain a le droit inaliénable d'accéder au sport dans un
                                                environnement sain, sûr et éthique.
                                            </p>

                                            <h6 className="fw-bold text-primary">Article 11 – Sport et développement
                                                durable</h6>
                                            <p>
                                                Les activités sportives doivent être planifiées et pratiquées de manière
                                                à respecter l'environnement et à promouvoir la durabilité sociale et
                                                économique.
                                            </p>

                                            <hr/>
                                            <div className="text-center mt-3 small bg-light p-2 rounded">
                                                Version complète : <a
                                                href="https://www.coe.int/fr/web/sport/european-sports-charter"
                                                target="_blank" rel="noopener noreferrer">coe.int/sport</a>
                                            </div>
                                        </Accordion.Body>
                                    </Accordion.Item>
                                </Accordion>

                                <div className="bg-light p-3 rounded">
                                    <Form.Check
                                        type="checkbox"
                                        id="check-charte"
                                        label="Je m'engage à respecter les principes de la Charte Européenne du Sport."
                                        disabled={user.hasSignedCharter || !hasRead}
                                        checked={user.hasSignedCharter}
                                        onChange={handleSignCharter}
                                        className={user.hasSignedCharter ? "text-success fw-bold" : ""}
                                    />
                                    {user.hasSignedCharter && (
                                        <div className="mt-2 small text-success">✅ Signée numériquement</div>
                                    )}
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>
                )}
            </Row>

            <ChangePassword show={showPassModal} handleClose={() => setShowPassModal(false)}/>
        </Container>
    );
};

export default Profile;