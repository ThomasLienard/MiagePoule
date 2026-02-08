import React, { useState, useEffect } from 'react';
import { Form, Button, Row, Col, Card, Container, Alert, Spinner } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

const CreateEventPage = () => {
    const navigate = useNavigate();

    const [championships, setChampionships] = useState([]);
    const [competitions, setCompetitions] = useState([]);

    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState({ type: '', message: '' });
    const [selectedChampionshipId, setSelectedChampionshipId] = useState('');

    const [formData, setFormData] = useState({
        name: '',
        description: '',
        typeEventName: 'MEETING',
        competitionId: '',
        startTime: '',
        endTime: '',
        placeName: '',
        number: '',
        street: '',
        zipCode: '',
        city: '',
        descriptionPlace: '',
        latitude: null,
        longitude: null,
        hasParking: false
    });

    // Charger les championnats (Port 8084 via Gateway)
    useEffect(() => {
        axios.get('http://localhost:8084/public/championship')
            .then(res => setChampionships(res.data))
            .catch(err => console.error("Erreur championnats", err));
    }, []);

    // Charger les compétitions liées au championnat
    useEffect(() => {
        if (selectedChampionshipId) {
            axios.get(`http://localhost:8084/public/championship/${selectedChampionshipId}/comp`)
                .then(res => setCompetitions(res.data))
                .catch(err => {
                    console.error("Erreur compétitions", err);
                    setCompetitions([]);
                });
        } else {
            setCompetitions([]);
        }
    }, [selectedChampionshipId]);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleChampionshipChange = (e) => {
        setSelectedChampionshipId(e.target.value);
        setFormData(prev => ({ ...prev, competitionId: '' }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setStatus({ type: '', message: '' });

        try {
            await axios.post('http://localhost:8084/admin/events', formData);
            setStatus({ type: 'success', message: 'Évènement planifié avec succès !' });
            setTimeout(() => navigate('/admin'), 2000);
        } catch (error) {
            setStatus({
                type: 'danger',
                message: error.response?.data?.message || "Erreur lors de la création."
            });
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="py-5">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="mb-0">📅 Planifier un nouvel évènement</h2>
                <Button as={Link} to="/admin" variant="outline-secondary shadow-sm">
                    Retour Administration
                </Button>
            </div>

            {status.message && <Alert variant={status.type} className="shadow-sm">{status.message}</Alert>}

            <Form onSubmit={handleSubmit}>
                <Card className="shadow-sm mb-4 border-0">
                    <Card.Body>
                        <h5 className="text-primary mb-3">1. Contexte</h5>
                        <Row>
                            <Form.Group as={Col} md={6} className="mb-3">
                                <Form.Label className="fw-bold">Championnat</Form.Label>
                                <Form.Select value={selectedChampionshipId} onChange={handleChampionshipChange} required>
                                    <option value="">Choisir un championnat...</option>
                                    {championships.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                </Form.Select>
                            </Form.Group>

                            <Form.Group as={Col} md={6} className="mb-3">
                                <Form.Label className="fw-bold">Compétition</Form.Label>
                                <Form.Select
                                    name="competitionId"
                                    value={formData.competitionId}
                                    onChange={handleChange}
                                    disabled={!selectedChampionshipId}
                                    required
                                >
                                    <option value="">Choisir une compétition...</option>
                                    {competitions.map(comp => <option key={comp.id} value={comp.id}>{comp.name}</option>)}
                                </Form.Select>
                            </Form.Group>
                        </Row>
                    </Card.Body>
                </Card>

                <Card className="shadow-sm mb-4 border-0">
                    <Card.Body>
                        <h5 className="text-primary mb-3">2. Détails de l'évènement</h5>
                        <Row>
                            <Form.Group as={Col} md={8} className="mb-3">
                                <Form.Label className="fw-bold">Nom</Form.Label>
                                <Form.Control name="name" placeholder="Finale..." onChange={handleChange} required />
                            </Form.Group>
                            <Form.Group as={Col} md={4} className="mb-3">
                                <Form.Label className="fw-bold">Type</Form.Label>
                                <Form.Select name="typeEventName" onChange={handleChange}>
                                    <option value="MEETING">Réunion</option>
                                    <option value="TRAINING">Entraînement</option>
                                    <option value="TRIAL">Épreuve (TRIAL)</option>
                                </Form.Select>
                            </Form.Group>
                        </Row>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Description</Form.Label>
                            <Form.Control as="textarea" name="description" rows={2} onChange={handleChange} />
                        </Form.Group>
                        <Row>
                            <Form.Group as={Col} md={6} className="mb-3">
                                <Form.Label className="fw-bold">Début</Form.Label>
                                <Form.Control type="datetime-local" name="startTime" onChange={handleChange} required />
                            </Form.Group>
                            <Form.Group as={Col} md={6} className="mb-3">
                                <Form.Label className="fw-bold">Fin</Form.Label>
                                <Form.Control type="datetime-local" name="endTime" onChange={handleChange} required />
                            </Form.Group>
                        </Row>
                    </Card.Body>
                </Card>

                <Card className="shadow-sm mb-4 border-0">
                    <Card.Body>
                        <h5 className="text-primary mb-3">3. Lieu et Logistique</h5>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Nom du lieu</Form.Label>
                            <Form.Control name="placeName" placeholder="Stade de France" onChange={handleChange} required />
                        </Form.Group>
                        <Row className="mb-3">
                            <Form.Group as={Col} md={3}>
                                <Form.Label className="fw-bold">N° (number)</Form.Label>
                                <Form.Control name="number" placeholder="11" onChange={handleChange} required />
                            </Form.Group>
                            <Form.Group as={Col} md={9}>
                                <Form.Label className="fw-bold">Rue (street)</Form.Label>
                                <Form.Control name="street" placeholder="Avenue Jules Rimet" onChange={handleChange} required />
                            </Form.Group>
                        </Row>
                        <Row className="mb-3">
                            <Form.Group as={Col} md={4}>
                                <Form.Label className="fw-bold">Code Postal (zipCode)</Form.Label>
                                <Form.Control name="zipCode" placeholder="93200" onChange={handleChange} required />
                            </Form.Group>
                            <Form.Group as={Col} md={8}>
                                <Form.Label className="fw-bold">Ville (city)</Form.Label>
                                <Form.Control name="city" placeholder="Saint-Denis" onChange={handleChange} required />
                            </Form.Group>
                        </Row>
                        <Form.Group className="mb-3 pt-3 border-top">
                            <Form.Label className="fw-bold">Accès PMR / Logistique</Form.Label>
                            <Form.Control as="textarea" name="descriptionPlace" rows={2} onChange={handleChange} />
                        </Form.Group>
                        <Form.Check type="switch" id="hasParking" name="hasParking" label="Parking disponible" className="fw-bold" onChange={handleChange} />
                    </Card.Body>
                </Card>

                <div className="d-grid gap-2 mb-5">
                    <Button variant="primary" size="lg" type="submit" disabled={loading} className="shadow">
                        {loading ? <><Spinner animation="border" size="sm" className="me-2" />Traitement...</> : 'Confirmer la planification'}
                    </Button>
                </div>
            </Form>
        </Container>
    );
};

export default CreateEventPage;