import React, { useEffect, useState } from 'react';
import { Form, Button, Row, Col, Card, Container, Alert, Spinner } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const CreateCompetitionPage = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState({ type: '', message: '' });
    const [validated, setValidated] = useState(false);
    const [championships, setChampionships] = useState([]);

    const [formData, setFormData] = useState({
        name: '',
        description: '',
        championshipId: '',
        start: '',
        end: ''
    });

    useEffect(() => {
        axios.get(`${import.meta.env.VITE_API_URL}/public/championship`)
            .then(res => setChampionships(res.data))
            .catch(err => console.error("Erreur championnats", err));
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
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

        const selectedChamp = championships.find(c => c.id === parseInt(formData.championshipId));

        if (!selectedChamp) {
            setStatus({ type: 'danger', message: 'Veuillez sélectionner un championnat valide.' });
            return;
        }

        const compStart = new Date(formData.start);
        const compEnd = new Date(formData.end);
        const champStart = new Date(selectedChamp.start);
        const champEnd = new Date(selectedChamp.end);

        if (compStart >= compEnd) {
            setStatus({ type: 'danger', message: 'La date de fin doit être strictement après la date de début.' });
            setValidated(true);
            return;
        }

        if (compStart < champStart || compEnd > champEnd) {
            setStatus({
                type: 'danger',
                message: `Les dates doivent être comprises entre le ${selectedChamp.start} et le ${selectedChamp.end}.`
            });
            setValidated(true);
            return;
        }

        setLoading(true);

        try {
            const dataToSend = {
                ...formData,
                championshipId: parseInt(formData.championshipId, 10)
            };

            await axios.post(`${import.meta.env.VITE_API_URL}/admin/comps`, dataToSend);

            setStatus({ type: 'success', message: 'Compétition planifiée avec succès !' });
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
            <h2 className="mb-4">📅 Planifier une nouvelle compétition</h2>

            {status.message && <Alert variant={status.type}>{status.message}</Alert>}

            <Form noValidate validated={validated} onSubmit={handleSubmit}>
                <Card className="shadow-sm mb-4 border-0">
                    <Card.Body>
                        <h5 className="text-primary fw-bold mb-3">1. Contexte</h5>
                        <Row>
                            <Form.Group as={Col} md={6} className="mb-3">
                                <Form.Label className="fw-bold">Championnat</Form.Label>
                                <Form.Select
                                    name="championshipId"
                                    value={formData.championshipId}
                                    onChange={handleChange}
                                    required
                                >
                                    <option value="">Choisir un championnat...</option>
                                    {championships.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                </Form.Select>
                                <Form.Control.Feedback type="invalid">Veuillez sélectionner un championnat.</Form.Control.Feedback>
                            </Form.Group>
                        </Row>
                    </Card.Body>
                </Card>

                <Card className="shadow-sm mb-4 border-0">
                    <Card.Body>
                        <h5 className="text-primary fw-bold mb-3">2. Détails de la compétition</h5>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Nom</Form.Label>
                            <Form.Control
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                required
                            />
                            <Form.Control.Feedback type="invalid">Le nom est requis.</Form.Control.Feedback>
                        </Form.Group>
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
                            <Form.Control.Feedback type="invalid">La description est requise.</Form.Control.Feedback>
                        </Form.Group>
                        <Row>
                            <Form.Group as={Col} md={6} className="mb-3">
                                <Form.Label className="fw-bold">Début</Form.Label>
                                <Form.Control
                                    type="date"
                                    name="start"
                                    value={formData.start}
                                    onChange={handleChange}
                                    onKeyDown={(e) => e.preventDefault()}
                                    min={championships.find(c => c.id === parseInt(formData.championshipId))?.start}
                                    max={championships.find(c => c.id === parseInt(formData.championshipId))?.end}
                                    required
                                />
                                <Form.Control.Feedback type="invalid">La date de début est requise.</Form.Control.Feedback>
                            </Form.Group>
                            <Form.Group as={Col} md={6} className="mb-3">
                                <Form.Label className="fw-bold">Fin</Form.Label>
                                <Form.Control
                                    type="date"
                                    name="end"
                                    value={formData.end}
                                    onChange={handleChange}
                                    onKeyDown={(e) => e.preventDefault()}
                                    min={formData.start || championships.find(c => c.id === parseInt(formData.championshipId))?.start}
                                    max={championships.find(c => c.id === parseInt(formData.championshipId))?.end}
                                    required
                                />
                                <Form.Control.Feedback type="invalid">La date de fin est requise.</Form.Control.Feedback>
                            </Form.Group>
                        </Row>
                    </Card.Body>
                </Card>

                <div className="d-grid">
                    <Button variant="secondary" size="lg" type="submit" disabled={loading}>
                        {loading ? <Spinner animation="border" size="sm" /> : 'Confirmer la planification'}
                    </Button>
                </div>
            </Form>
        </Container>
    );
};

export default CreateCompetitionPage;