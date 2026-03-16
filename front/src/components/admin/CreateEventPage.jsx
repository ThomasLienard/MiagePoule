import React, { useState, useEffect } from 'react';
import { Form, Button, Row, Col, Card, Container, Alert, Spinner } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const CreateEventPage = () => {
    const navigate = useNavigate();

    // États pour les listes
    const [championships, setChampionships] = useState([]);
    const [competitions, setCompetitions] = useState([]);
    const [commissaires, setCommissaires] = useState([]);

    // États de gestion
    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState({ type: '', message: '' });
    const [selectedChampionshipId, setSelectedChampionshipId] = useState('');
    const [validated, setValidated] = useState(false);

    const [formData, setFormData] = useState({
        name: '',
        description: '',
        typeEventName: 'MEETING',
        competitionId: '',
        commissaireId: '',
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

    // Utilitaire pour formater les dates pour l'input (YYYY-MM-DDTHH:mm)
    const formatForPicker = (dateString) => {
        if (!dateString) return "";
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return "";
        const offset = date.getTimezoneOffset() * 60000;
        return new Date(date.getTime() - offset).toISOString().slice(0, 16);
    };

    // 1. Chargement initial
    useEffect(() => {
        const fetchData = async () => {
            const token = localStorage.getItem('token');
            const config = { headers: { Authorization: `Bearer ${token}` } };
            try {
                const resChamp = await axios.get('http://localhost:8084/public/championship');
                setChampionships(resChamp.data);

                try {
                    const resComm = await axios.get('http://localhost:8084/commissaire/users?role=COMMISSAIRE', config);
                    setCommissaires(resComm.data);
                } catch (err) {
                    console.warn("Accès commissaires restreint (403).");
                }
            } catch (globalErr) {
                console.error("Erreur de chargement", globalErr);
            }
        };
        fetchData();
    }, []);

    // 2. Chargement des compétitions
    useEffect(() => {
        if (selectedChampionshipId) {
            axios.get(`http://localhost:8084/public/championship/${selectedChampionshipId}/comp`)
                .then(res => setCompetitions(res.data))
                .catch(() => setCompetitions([]));
        }
    }, [selectedChampionshipId]);

    const selectedComp = competitions.find(c => c.id === parseInt(formData.competitionId));

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value,
            ...(name === 'typeEventName' && value !== 'TRIAL' ? { commissaireId: '' } : {})
        }));
    };

    const handleChampionshipChange = (e) => {
        setSelectedChampionshipId(e.target.value);
        setFormData(prev => ({ ...prev, competitionId: '' }));
    };

    const handleSubmit = async (e) => {
        const form = e.currentTarget;
        e.preventDefault();
        setValidated(false);

        if (form.checkValidity() === false) {
            e.stopPropagation();
            setValidated(true);
            return;
        }

        setLoading(true);
        const token = localStorage.getItem('token');
        const config = { headers: { Authorization: `Bearer ${token}` } };

        try {
            await axios.post('http://localhost:8084/admin/events', formData, config);
            setStatus({ type: 'success', message: 'Évènement planifié avec succès !' });
            setTimeout(() => navigate('/admin'), 2000);
        } catch (error) {
            setStatus({ type: 'danger', message: error.response?.data?.message || "Erreur de création." });
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="py-5">
            <h2 className="mb-4">📅 Planifier un nouvel évènement</h2>

            {status.message && <Alert variant={status.type} className="shadow-sm">{status.message}</Alert>}

            <Form noValidate validated={validated} onSubmit={handleSubmit}>
                {/* 1. CONTEXTE */}
                <Card className="shadow-sm mb-4 border-0">
                    <Card.Body>
                        <h5 className="text-primary fw-bold mb-3">1. Contexte</h5>
                        <Row>
                            <Col md={6} className="mb-3">
                                <Form.Label className="fw-bold">Championnat</Form.Label>
                                <Form.Select value={selectedChampionshipId} onChange={handleChampionshipChange} required>
                                    <option value="">Choisir un championnat...</option>
                                    {championships.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                </Form.Select>
                            </Col>
                            <Col md={6} className="mb-3">
                                <Form.Label className="fw-bold">Compétition</Form.Label>
                                <Form.Select name="competitionId" value={formData.competitionId} onChange={handleChange} disabled={!selectedChampionshipId} required>
                                    <option value="">Choisir une compétition...</option>
                                    {competitions.map(comp => <option key={comp.id} value={comp.id}>{comp.name}</option>)}
                                </Form.Select>
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>

                {/* 2. DÉTAILS */}
                <Card className="shadow-sm mb-4 border-0">
                    <Card.Body>
                        <h5 className="text-primary fw-bold mb-3">2. Détails de l'évènement</h5>
                        <Row>
                            <Col md={8} className="mb-3">
                                <Form.Label className="fw-bold">Nom</Form.Label>
                                <Form.Control name="name" value={formData.name} onChange={handleChange} required />
                            </Col>
                            <Col md={4} className="mb-3">
                                <Form.Label className="fw-bold">Type</Form.Label>
                                <Form.Select name="typeEventName" value={formData.typeEventName} onChange={handleChange}>
                                    <option value="MEETING">Réunion</option>
                                    <option value="TRAINING">Entraînement</option>
                                    <option value="TRIAL">Épreuve (TRIAL)</option>
                                </Form.Select>
                            </Col>
                        </Row>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Description de l'activité</Form.Label>
                            <Form.Control as="textarea" rows={2} name="description" value={formData.description} onChange={handleChange} required />
                        </Form.Group>
                        <Row>
                            <Col md={6} className="mb-3">
                                <Form.Label className="fw-bold">Début</Form.Label>
                                <Form.Control
                                    type="datetime-local"
                                    name="startTime"
                                    value={formData.startTime}
                                    onChange={handleChange}
                                    required
                                    disabled={!selectedComp}
                                    min={selectedComp ? formatForPicker(selectedComp.start) : ""}
                                    max={selectedComp ? formatForPicker(selectedComp.end) : ""}
                                />
                            </Col>
                            <Col md={6} className="mb-3">
                                <Form.Label className="fw-bold">Fin</Form.Label>
                                <Form.Control
                                    type="datetime-local"
                                    name="endTime"
                                    value={formData.endTime}
                                    onChange={handleChange}
                                    required
                                    disabled={!selectedComp}
                                    min={formData.startTime || (selectedComp ? formatForPicker(selectedComp.start) : "")}
                                    max={selectedComp ? formatForPicker(selectedComp.end) : ""}
                                />
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>

                {/* 3. LIEU COMPLET */}
                <Card className="shadow-sm mb-4 border-0">
                    <Card.Body>
                        <h5 className="text-primary fw-bold mb-3">3. Lieu et Adresse</h5>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Nom du lieu</Form.Label>
                            <Form.Control name="placeName" value={formData.placeName} onChange={handleChange} placeholder="Ex: Stade de France" required />
                        </Form.Group>
                        <Row className="mb-3">
                            <Col md={3}>
                                <Form.Label className="fw-bold">N°</Form.Label>
                                <Form.Control name="number" value={formData.number} onChange={handleChange} required />
                            </Col>
                            <Col md={9}>
                                <Form.Label className="fw-bold">Rue</Form.Label>
                                <Form.Control name="street" value={formData.street} onChange={handleChange} required />
                            </Col>
                        </Row>
                        <Row className="mb-3">
                            <Col md={4}>
                                <Form.Label className="fw-bold">Code Postal</Form.Label>
                                <Form.Control name="zipCode" value={formData.zipCode} onChange={handleChange} required />
                            </Col>
                            <Col md={8}>
                                <Form.Label className="fw-bold">Ville</Form.Label>
                                <Form.Control name="city" value={formData.city} onChange={handleChange} required />
                            </Col>
                        </Row>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Description du lieu (Accès, précisions)</Form.Label>
                            <Form.Control as="textarea" rows={2} name="descriptionPlace" value={formData.descriptionPlace} onChange={handleChange} />
                        </Form.Group>
                        <Form.Check type="switch" label="Parking disponible" name="hasParking" checked={formData.hasParking} onChange={handleChange} className="fw-bold" />
                    </Card.Body>
                </Card>

                {/* 4. COMMISSAIRE (TRIAL uniquement) */}
                {formData.typeEventName === 'TRIAL' && (
                    <Card className="shadow-sm mb-4 border-0 border-start border-warning border-4">
                        <Card.Body>
                            <h5 className="text-warning fw-bold mb-3">👮 Commissaire Responsable</h5>
                            <Form.Select
                                name="commissaireId"
                                value={formData.commissaireId}
                                onChange={handleChange}
                                required={formData.typeEventName === 'TRIAL'}
                            >
                                <option value="">-- Sélectionner un commissaire --</option>
                                {commissaires.map(c => (
                                    <option key={c.id} value={c.id}>{c.firstName} {c.lastName} ({c.email})</option>
                                ))}
                            </Form.Select>
                        </Card.Body>
                    </Card>
                )}

                <div className="d-grid gap-2 mb-5">
                    <Button variant="primary" size="lg" type="submit" disabled={loading} className="shadow">
                        {loading ? <Spinner animation="border" size="sm" /> : 'Confirmer la planification'}
                    </Button>
                </div>
            </Form>
        </Container>
    );
};

export default CreateEventPage;