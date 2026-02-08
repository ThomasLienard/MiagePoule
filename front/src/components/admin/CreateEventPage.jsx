import React, { useState } from 'react';
import { Form, Button, Row, Col, Card, Container } from 'react-bootstrap';
import axios from 'axios';

const CreateEventPage = () => {
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        typeEventName: 'MEETING',
        competitionId: 1,
        startTime: '',
        endTime: '',
        placeName: '',
        city: '',
        descriptionPlace: '',
        hasParking: false,
        latitude: '',
        longitude: ''
    });

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            // URL protégée par le rôle ADMIN côté Backend
            await axios.post('http://localhost:8082/admin/events', formData);
            alert("L'évènement a été créé avec succès !");
        } catch (error) {
            console.error("Erreur creation:", error);
            alert("Erreur lors de la création : " + (error.response?.data?.message || error.message));
        }
    };

    return (
        <Container className="mt-5 mb-5">
            <Card className="shadow-sm">
                <Card.Header className="bg-primary text-white">
                    <h4 className="mb-0">Nouveau Déploiement d'Évènement</h4>
                </Card.Header>
                <Card.Body>
                    <Form onSubmit={handleSubmit}>
                        {/* --- Section Identification --- */}
                        <Row className="mb-3">
                            <Form.Group as={Col} md={8} controlId="name">
                                <Form.Label>Nom de l'évènement</Form.Label>
                                <Form.Control
                                    name="name"
                                    placeholder="Ex: Finale 100m"
                                    onChange={handleChange}
                                    required
                                />
                            </Form.Group>

                            <Form.Group as={Col} md={4} controlId="typeEventName">
                                <Form.Label>Type d'activité</Form.Label>
                                <Form.Select name="typeEventName" onChange={handleChange}>
                                    <option value="MEETING">Réunion (Extra)</option>
                                    <option value="TRAINING">Entraînement (Extra)</option>
                                    <option value="TRIAL">Épreuve (Compétition)</option>
                                </Form.Select>
                            </Form.Group>
                        </Row>

                        <Form.Group className="mb-3" controlId="description">
                            <Form.Label>Description de l'évènement</Form.Label>
                            <Form.Control
                                as="textarea"
                                name="description"
                                rows={2}
                                onChange={handleChange}
                            />
                        </Form.Group>

                        {/* --- Section Dates --- */}
                        <Row className="mb-4">
                            <Form.Group as={Col} controlId="startTime">
                                <Form.Label>Date & Heure de début</Form.Label>
                                <Form.Control
                                    type="datetime-local"
                                    name="startTime"
                                    onChange={handleChange}
                                    required
                                />
                            </Form.Group>
                            <Form.Group as={Col} controlId="endTime">
                                <Form.Label>Date & Heure de fin</Form.Label>
                                <Form.Control
                                    type="datetime-local"
                                    name="endTime"
                                    onChange={handleChange}
                                    required
                                />
                            </Form.Group>
                        </Row>

                        <hr />

                        {/* --- Section Lieu & Logistique --- */}
                        <h5 className="mb-3 text-secondary">Logistique & Lieu</h5>
                        <Row className="mb-3">
                            <Form.Group as={Col} md={6} controlId="placeName">
                                <Form.Label>Nom du lieu</Form.Label>
                                <Form.Control name="placeName" onChange={handleChange} required />
                            </Form.Group>
                            <Form.Group as={Col} md={6} controlId="city">
                                <Form.Label>Ville</Form.Label>
                                <Form.Control name="city" onChange={handleChange} required />
                            </Form.Group>
                        </Row>

                        <Row className="mb-3">
                            <Form.Group as={Col} md={6} controlId="latitude">
                                <Form.Label>Latitude</Form.Label>
                                <Form.Control
                                    type="number" step="any"
                                    name="latitude"
                                    placeholder="48.8566"
                                    onChange={handleChange}
                                />
                            </Form.Group>
                            <Form.Group as={Col} md={6} controlId="longitude">
                                <Form.Label>Longitude</Form.Label>
                                <Form.Control
                                    type="number" step="any"
                                    name="longitude"
                                    placeholder="2.3522"
                                    onChange={handleChange}
                                />
                            </Form.Group>
                        </Row>

                        <Form.Group className="mb-3" controlId="descriptionPlace">
                            <Form.Label>Détails spécifiques (Accès PMR, consignes...)</Form.Label>
                            <Form.Control
                                as="textarea"
                                name="descriptionPlace"
                                rows={2}
                                placeholder="Détaillez ici les accès PMR ou autres spécificités..."
                                onChange={handleChange}
                            />
                        </Form.Group>

                        <Form.Group className="mb-4" controlId="hasParking">
                            <Form.Check
                                type="switch"
                                label="Parking disponible sur place"
                                name="hasParking"
                                onChange={handleChange}
                            />
                        </Form.Group>

                        <div className="d-grid gap-2">
                            <Button variant="success" size="lg" type="submit">
                                Enregistrer l'évènement
                            </Button>
                        </div>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default CreateEventPage;