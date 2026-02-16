import React, { useState, useEffect } from 'react';
import { Form, Button, Row, Col, Card, Container, Alert, Spinner } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const CreateEventPage = () => {
    const navigate = useNavigate();

    const [championships, setChampionships] = useState([]);
    const [competitions, setCompetitions] = useState([]);

    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState({ type: '', message: '' });
    const [selectedChampionshipId, setSelectedChampionshipId] = useState('');
    const [validated, setValidated] = useState(false);

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

    useEffect(() => {
        axios.get('http://localhost:8084/public/championship')
            .then(res => setChampionships(res.data))
            .catch(err => console.error("Erreur championnats", err));
    }, []);

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
            </div>

            {status.message && <Alert variant={status.type} className="shadow-sm">{status.message}</Alert>}

            <Form noValidate validated={validated} onSubmit={handleSubmit}>
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
                                    min={competitions.find(c => c.id === parseInt(formData.competitionId))?.start}
                                    max={competitions.find(c => c.id === parseInt(formData.competitionId))?.end}
                                    required
                                />
                                <Form.Control.Feedback type="invalid">Date de début requise.</Form.Control.Feedback>
                            </Form.Group>
                            <Form.Group as={Col} md={6} className="mb-3">
                                <Form.Label className="fw-bold">Fin</Form.Label>
                                <Form.Control
                                    type="datetime-local"
                                    name="endTime"
                                    value={formData.endTime}
                                    onChange={handleChange}
                                    min={formData.start || competitions.find(c => c.id === parseInt(formData.competitionId))?.start}
                                    max={competitions.find(c => c.id === parseInt(formData.competitionId))?.end}
                                    required
                                />
                                <Form.Control.Feedback type="invalid">Date de fin requise.</Form.Control.Feedback>
                            </Form.Group>
                        </Row>
                    </Card.Body>
                </Card>

                <Card className="shadow-sm mb-4 border-0">
                    <Card.Body>
                        <h5 className="text-primary fw-bold mb-3">3. Lieu et Logistique</h5>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Nom du lieu</Form.Label>
                            <Form.Control
                                name="placeName"
                                value={formData.placeName}
                                placeholder="Stade de France"
                                onChange={handleChange}
                                required
                            />
                        </Form.Group>
                        <Row className="mb-3">
                            <Form.Group as={Col} md={3}>
                                <Form.Label className="fw-bold">N°</Form.Label>
                                <Form.Control name="number" value={formData.number} placeholder="11" onChange={handleChange} required />
                            </Form.Group>
                            <Form.Group as={Col} md={9}>
                                <Form.Label className="fw-bold">Rue</Form.Label>
                                <Form.Control name="street" value={formData.street} placeholder="Avenue Jules Rimet" onChange={handleChange} required />
                            </Form.Group>
                        </Row>
                        <Row className="mb-3">
                            <Form.Group as={Col} md={4}>
                                <Form.Label className="fw-bold">Code Postal</Form.Label>
                                <Form.Control name="zipCode" value={formData.zipCode} placeholder="93200" onChange={handleChange} required />
                            </Form.Group>
                            <Form.Group as={Col} md={8}>
                                <Form.Label className="fw-bold">Ville</Form.Label>
                                <Form.Control name="city" value={formData.city} placeholder="Saint-Denis" onChange={handleChange} required />
                            </Form.Group>
                        </Row>
                        <Form.Group className="mb-3 pt-3 border-top">
                            <Form.Label className="fw-bold">Accès PMR / Logistique</Form.Label>
                            <Form.Control as="textarea" name="descriptionPlace" value={formData.descriptionPlace} rows={2} onChange={handleChange} />
                        </Form.Group>
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