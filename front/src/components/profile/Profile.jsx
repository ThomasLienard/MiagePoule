import React, { useEffect, useState } from "react";
import { Container, Card, Badge, Spinner, Form, Button, Alert } from "react-bootstrap";
import axios from "axios";

const Profile = () => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isEditing, setIsEditing] = useState(false);
    const [formData, setFormData] = useState({});
    const [message, setMessage] = useState({ type: "", text: "" });

    const API_URL = "http://localhost:8084/account";
    const token = localStorage.getItem("token");
    const config = { headers: { Authorization: `Bearer ${token}` } };

    useEffect(() => {
        fetchProfile();
    }, []);

    const fetchProfile = async () => {
        try {
            const response = await axios.get(API_URL, config);
            setUser(response.data);
            setFormData(response.data);
        } catch (error) {
            setMessage({ type: "danger", text: "Erreur lors du chargement du profil" });
        } finally {
            setLoading(false);
        }
    };

    const handleUpdate = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.put(`${API_URL}/settings`, formData, config);
            setUser(response.data);
            setIsEditing(false);
            setMessage({ type: "success", text: "Profil mis à jour avec succès !" });
        } catch (error) {
            setMessage({ type: "danger", text: "Échec de la mise à jour." });
        }
    };

    if (loading) return <Container className="text-center pt-5"><Spinner animation="border" variant="primary" /></Container>;

    return (
        <Container className="pt-4">
            <h2 className="text-center mb-4">👤 Mon Compte</h2>

            {message.text && <Alert variant={message.type} onClose={() => setMessage({})} dismissible>{message.text}</Alert>}

            <div className="d-flex justify-content-center">
                <Card className="shadow-sm" style={{ maxWidth: "500px", width: "100%" }}>
                    <Card.Body className="p-4">
                        {!isEditing ? (
                            <>
                                <div className="mb-3">
                                    <label className="text-muted small">Nom complet</label>
                                    <p className="h5">{user.name} {user.lastname}</p>
                                </div>
                                <div className="mb-3">
                                    <label className="text-muted small">Email</label>
                                    <p>{user.email}</p>
                                </div>
                                <div className="mb-3">
                                    <label className="text-muted small">Pays</label>
                                    <p>{user.countryName || "Non renseigné"}</p>
                                </div>
                                <Button variant="outline-primary" onClick={() => setIsEditing(true)} className="w-100">
                                    Modifier le profil
                                </Button>
                            </>
                        ) : (
                            <Form onSubmit={handleUpdate}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Prénom</Form.Label>
                                    <Form.Control
                                        type="text"
                                        value={formData.name || ""}
                                        onChange={(e) => setFormData({...formData, name: e.target.value})}
                                    />
                                </Form.Group>
                                <Form.Group className="mb-3">
                                    <Form.Label>Nom</Form.Label>
                                    <Form.Control
                                        type="text"
                                        value={formData.lastname || ""}
                                        onChange={(e) => setFormData({...formData, lastname: e.target.value})}
                                    />
                                </Form.Group>
                                <div className="d-flex gap-2">
                                    <Button variant="success" type="submit" className="flex-grow-1">Sauvegarder</Button>
                                    <Button variant="link" onClick={() => setIsEditing(false)} className="text-muted">Annuler</Button>
                                </div>
                            </Form>
                        )}
                    </Card.Body>
                </Card>
            </div>
        </Container>
    );
};

export default Profile;