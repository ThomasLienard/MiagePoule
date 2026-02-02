import React, { useEffect, useState } from "react";
import { Container, Card, Spinner, Form, Button, Alert, Row, Col } from "react-bootstrap";
import axios from "axios";
import ChangePassword from "./ChangePassword.jsx";

const Profile = () => {
    const [user, setUser] = useState(null);
    const [countries, setCountries] = useState([]); // Liste des codes pays ["FR", "BE"...]
    const [loading, setLoading] = useState(true);
    const [isEditing, setIsEditing] = useState(false);
    const [formData, setFormData] = useState({});
    const [message, setMessage] = useState({ type: "", text: "" });
    const [showPassModal, setShowPassModal] = useState(false);

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

    if (loading) return <Container className="text-center pt-5"><Spinner animation="border" /></Container>;

    return (
        <Container className="pt-4">
            {message.text && <Alert variant={message.type} dismissible>{message.text}</Alert>}

            <div className="d-flex justify-content-center">
                <Card className="shadow-sm" style={{ maxWidth: "500px", width: "100%" }}>
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
                                    <Form.Control value={formData.name || ""} onChange={e => setFormData({...formData, name: e.target.value})} />
                                </Form.Group>
                                <Form.Group className="mb-2">
                                    <Form.Label>Nom</Form.Label>
                                    <Form.Control value={formData.lastname || ""} onChange={e => setFormData({...formData, lastname: e.target.value})} />
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
            </div>
            <ChangePassword show={showPassModal} handleClose={() => setShowPassModal(false)} />
        </Container>
    );
};

export default Profile;