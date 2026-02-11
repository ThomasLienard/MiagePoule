import React, { useState } from 'react';
import { Form, Button, Row, Col, Card, Container, Alert, Spinner } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const CreateChampionshipPage = () => {
    const navigate = useNavigate();

    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState({ type: '', message: '' });
    const [validated, setValidated] = useState(false);

    const [formData, setFormData] = useState({
        name: '',
        description: '',
        start: '',
        end: ''
    });

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

        setStatus({ type: '', message: '' });

        if (form.checkValidity() === false) {
            e.stopPropagation();
            setValidated(true);
            return;
        }

        if (new Date(formData.start) >= new Date(formData.end)) {
            setStatus({ type: 'danger', message: 'La date de fin doit être strictement après la date de début.' });
            setValidated(true);
            return;
        }

        setLoading(true);

        try {
            await axios.post('http://localhost:8084/admin/champs', formData);

            setStatus({ type: 'success', message: 'Championnat planifié avec succès !' });
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
            <h2 className="mb-4">📅 Planifier un nouveau championnat</h2>

            {status.message && <Alert variant={status.type}>{status.message}</Alert>}

            <Form noValidate validated={validated} onSubmit={handleSubmit}>
                <Card className="shadow-sm mb-4 border-0">
                    <Card.Body>
                        <h5 className="text-primary fw-bold mb-3">Détails du championnat</h5>
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

export default CreateChampionshipPage;