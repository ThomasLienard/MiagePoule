import React, { useState, useEffect } from 'react';
import { Form, Button, Row, Col, Card, Container, Alert, Spinner } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';

const EditChampionshipPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const queryParams = new URLSearchParams(location.search);
    const champIdFromUrl = queryParams.get('id');

    const [allChampionships, setAllChampionships] = useState([]);
    const [selectedChampId, setSelectedChampId] = useState(champIdFromUrl || '');
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [status, setStatus] = useState({ type: '', message: '' });

    const [formData, setFormData] = useState({
        name: '',
        description: '',
        start: '',
        end: ''
    });

    useEffect(() => {
        const fetchData = async () => {
            try {
                const res = await axios.get('http://localhost:8084/public/championship');
                setAllChampionships(res.data);
                if (champIdFromUrl) {
                    const champ = res.data.find(c => c.id === parseInt(champIdFromUrl));
                    if (champ) setFormData({
                        name: champ.name,
                        description: champ.description,
                        start: champ.start,
                        end: champ.end
                    });
                }
            } catch (err) {
                setStatus({ type: 'danger', message: "Erreur de chargement." });
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [champIdFromUrl]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        const finalId = parseInt(selectedChampId);

        try {
            // Attention : Vérifie si ton endpoint admin est /admin/championship/{id}
            await axios.put(`http://localhost:8084/admin/champs/${finalId}`, {
                id: finalId,
                ...formData
            });
            setStatus({ type: 'success', message: 'Championnat mis à jour !' });
            setTimeout(() => navigate(-1), 1500);
        } catch (error) {
            setStatus({ type: 'danger', message: "Erreur lors de la sauvegarde." });
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <Container className="py-5 text-center"><Spinner animation="border" /></Container>;

    return (
        <Container className="py-5">
            <h2 className="mb-4 text-center">🏆 Modifier le Championnat</h2>
            {status.message && <Alert variant={status.type}>{status.message}</Alert>}

            {!champIdFromUrl && (
                <Card className="mb-4 bg-light shadow-sm">
                    <Card.Body>
                        <Form.Label className="fw-bold">Sélectionner un championnat</Form.Label>
                        <Form.Select value={selectedChampId} onChange={(e) => {
                            const id = e.target.value;
                            setSelectedChampId(id);
                            const c = allChampionships.find(item => item.id === parseInt(id));
                            if (c) setFormData({ name: c.name, description: c.description, start: c.start, end: c.end });
                        }}>
                            <option value="">--- Choisir ---</option>
                            {allChampionships.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                        </Form.Select>
                    </Card.Body>
                </Card>
            )}

            {selectedChampId && (
                <Form onSubmit={handleSubmit}>
                    <Card className="shadow-sm">
                        <Card.Body>
                            <Form.Group className="mb-3">
                                <Form.Label className="fw-bold">Nom</Form.Label>
                                <Form.Control name="name" value={formData.name} onChange={handleChange} required />
                            </Form.Group>
                            <Form.Group className="mb-3">
                                <Form.Label className="fw-bold">Description</Form.Label>
                                <Form.Control as="textarea" rows={3} name="description" value={formData.description} onChange={handleChange} />
                            </Form.Group>
                            <Row>
                                <Col md={6} className="mb-3">
                                    <Form.Label className="fw-bold">Date de début</Form.Label>
                                    <Form.Control type="date" name="start" value={formData.start} onChange={handleChange} required />
                                </Col>
                                <Col md={6} className="mb-3">
                                    <Form.Label className="fw-bold">Date de fin</Form.Label>
                                    <Form.Control type="date" name="end" value={formData.end} onChange={handleChange} min={formData.start} required />
                                </Col>
                            </Row>
                            <Button variant="secondary" size="lg" type="submit" className="w-100 mt-3" disabled={submitting}>
                                {submitting ? "Enregistrement..." : "Mettre à jour le championnat"}
                            </Button>
                        </Card.Body>
                    </Card>
                </Form>
            )}
        </Container>
    );
};

export default EditChampionshipPage;