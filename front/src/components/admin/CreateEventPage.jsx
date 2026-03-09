import React, { useState, useEffect } from 'react';
import { Form, Button, Row, Col, Card, Container, Alert, Spinner } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const CreateEventPage = () => {
    const navigate = useNavigate();

    const [championships, setChampionships] = useState([]);
    const [competitions, setCompetitions] = useState([]);
    const [commissaires, setCommissaires] = useState([]);

    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState({ type: '', message: '' });
    const [selectedChampionshipId, setSelectedChampionshipId] = useState('');
    const [validated, setValidated] = useState(false);

    const [formData, setFormData] = useState({
        name: '',
        description: '',
        typeEventName: 'MEETING',
        competitionId: '',
        commissaireId: '', // Ajout du champ pour la liaison
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

    useEffect(() => {
        // Appels via la Gateway (port 8082)
        axios.get('http://localhost:8082/public/championship')
            .then(res => setChampionships(res.data))
            .catch(err => console.error("Erreur championnats", err));

        // Récupération des utilisateurs avec le rôle COMMISSAIRE
        axios.get('http://localhost:8082/commissaire/users?role=COMMISSAIRE')
            .then(res => setCommissaires(res.data))
            .catch(err => console.error("Erreur récupération commissaires", err));
    }, []);

    useEffect(() => {
        if (selectedChampionshipId) {
            axios.get(`http://localhost:8082/public/championship/${selectedChampionshipId}/comp`)
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
        const form = e.currentTarget;
        e.preventDefault();
        setValidated(false);
        setStatus({ type: '', message: '' });

        if (form.checkValidity() === false) {
            e.stopPropagation();
            setValidated(true);
            return;
        }

        const selectedComp = competitions.find(c => c.id === parseInt(formData.competitionId));
        const compStart = new Date(selectedComp.start);
        const compEnd = new Date(selectedComp.end);

        if (new Date(formData.startTime) >= new Date(formData.endTime)) {
            setStatus({ type: 'danger', message: 'La date de fin doit être strictement après la date de début.' });
            setValidated(true);
            return;
        }

        if (new Date(formData.startTime) < compStart || new Date(formData.endTime) > compEnd) {
            setStatus({
                type: 'danger',
                message: `Les dates doivent être comprises entre le ${selectedComp.start} et le ${selectedComp.end}.`
            });
            setValidated(true);
            return;
        }

        setLoading(true);

        try {
            // Envoi à la Gateway qui dispatchera vers AdminEventService
            await axios.post('http://localhost:8082/admin/events', formData);
            setStatus({ type: 'success', message: 'Évènement planifié et commissaire lié avec succès !' });
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
            </div>

            {status.message && <Alert variant={status.type} className="shadow-sm">{status.message}</Alert>}

            <Form noValidate validated={validated} onSubmit={handleSubmit}>
                {/* 1. CONTEXTE */}
                <Card className="shadow-sm mb-4 border-0">
                    <Card.Body>
                        <h5 className="text-primary fw-bold mb-3">1. Contexte</h5>
                        <Row>
                            <Form.Group as={Col} md={6} className="mb-3">
                                <Form.Label className="fw-bold">Championnat</Form.Label>
                                <Form.Select value={selectedChampionshipId} onChange={handleChampionshipChange} required>
                                    <option value="">Choisir un championnat...</option>
                                    {championships.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                </Form.Select>
                                <Form.Control.Feedback type="invalid">Veuillez sélectionner un championnat.</Form.Control.Feedback>
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
                                <Form.Control.Feedback type="invalid">Veuillez sélectionner une compétition.</Form.Control.Feedback>
                            </Form.Group>
                        </Row>
                    </Card.Body>
                </Card>

                {/* 2. DÉTAILS */}
                <Card className="shadow-sm mb-4 border-0">
                    <Card.Body>
                        <h5 className="text-primary fw-bold mb-3">2. Détails de l'évènement</h5>
                        <Row>
                            <Form.Group as={Col} md={8} className="mb-3">
                                <Form.Label className="fw-bold">Nom</Form.Label>
                                <Form.Control
                                    name="name"
                                    value={formData.name}
                                    placeholder="Ex: Finale 100m"
                                    onChange={handleChange}
                                    required
                                />
                                <Form.Control.Feedback type="invalid">Le nom de l'évènement est requis.</Form.Control.Feedback>
                            </Form.Group>
                            <Form.Group as={Col} md={4} className="mb-3">
                                <Form.Label className="fw-bold">Type</Form.Label>
                                <Form.Select name="typeEventName" value={formData.typeEventName} onChange={handleChange}>
                                    <option value="MEETING">Réunion</option>
                                    <option value="TRAINING">Entraînement</option>
                                    <option value="TRIAL">Épreuve (TRIAL)</option>
                                </Form.Select>
                            </Form.Group>
                        </Row>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Description</Form.Label>
                            <Form.Control
                                as="textarea"
                                name="description"
                                value={formData.description}
                                rows={2}
                                onChange={handleChange}
                                required
                            />
                            <Form.Control.Feedback type="invalid">Veuillez fournir une description.</Form.Control.Feedback>
                        </Form.Group>
                        <Row>
                            <Form.Group as={Col} md={6} className="mb-3">
                                <Form.Label className="fw-bold">Début</Form.Label>
                                <Form.Control
                                    type="datetime-local"
                                    name="startTime"
                                    value={formData.startTime}
                                    onChange={handleChange}
                                    required
                                />
                            </Form.Group>
                            <Form.Group as={Col} md={6} className="mb-3">
                                <Form.Label className="fw-bold">Fin</Form.Label>
                                <Form.Control
                                    type="datetime-local"
                                    name="endTime"
                                    value={formData.endTime}
                                    onChange={handleChange}
                                    required
                                />
                            </Form.Group>
                        </Row>
                    </Card.Body>
                </Card>

                {/* 3. LIEU */}
                <Card className="shadow-sm mb-4 border-0">
                    <Card.Body>
                        <h5 className="text-primary fw-bold mb-3">3. Lieu et Logistique</h5>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Nom du lieu</Form.Label>
                            <Form.Control name="placeName" value={formData.placeName} onChange={handleChange} required />
                        </Form.Group>
                        <Row className="mb-3">
                            <Form.Group as={Col} md={3}>
                                <Form.Label className="fw-bold">N°</Form.Label>
                                <Form.Control name="number" value={formData.number} onChange={handleChange} required />
                            </Form.Group>
                            <Form.Group as={Col} md={9}>
                                <Form.Label className="fw-bold">Rue</Form.Label>
                                <Form.Control name="street" value={formData.street} onChange={handleChange} required />
                            </Form.Group>
                        </Row>
                        <Row className="mb-3">
                            <Form.Group as={Col} md={4}>
                                <Form.Label className="fw-bold">Code Postal</Form.Label>
                                <Form.Control name="zipCode" value={formData.zipCode} onChange={handleChange} required />
                            </Form.Group>
                            <Form.Group as={Col} md={8}>
                                <Form.Label className="fw-bold">Ville</Form.Label>
                                <Form.Control name="city" value={formData.city} onChange={handleChange} required />
                            </Form.Group>
                        </Row>
                        <Form.Check
                            type="switch"
                            id="hasParking"
                            name="hasParking"
                            checked={formData.hasParking}
                            label="Parking disponible"
                            className="fw-bold"
                            onChange={handleChange}
                        />
                    </Card.Body>
                </Card>

                <Card className="shadow-sm mb-4 border-0 border-start border-warning border-4">
                    <Card.Body>
                        <h5 className="text-warning fw-bold mb-3">👮 4. Personnel Responsable</h5>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Commissaire affecté à l'épreuve</Form.Label>
                            <Form.Select
                                name="commissaireId"
                                value={formData.commissaireId}
                                onChange={handleChange}
                                required
                            >
                                <option value="">-- Sélectionner un commissaire --</option>
                                {commissaires.map(c => (
                                    <option key={c.id} value={c.id}>
                                        {c.firstName} {c.lastName} ({c.email})
                                    </option>
                                ))}
                            </Form.Select>
                            <Form.Control.Feedback type="invalid">
                                Vous devez lier un commissaire à cette épreuve.
                            </Form.Control.Feedback>
                            <Form.Text className="text-muted">
                                Ce commissaire sera responsable de la saisie des résultats pour cette épreuve.
                            </Form.Text>
                        </Form.Group>
                    </Card.Body>
                </Card>

                <div className="d-grid gap-2 mb-5">
                    <Button variant="secondary" size="lg" type="submit" disabled={loading} className="shadow">
                        {loading ? <><Spinner animation="border" size="sm" className="me-2" />Traitement...</> : 'Confirmer la planification'}
                    </Button>
                </div>
            </Form>
        </Container>
    );
};

export default CreateEventPage;