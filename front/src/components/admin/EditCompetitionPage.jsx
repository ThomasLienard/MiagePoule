import React, { useState, useEffect, useCallback } from 'react';
import { Form, Button, Row, Col, Card, Container, Alert, Spinner } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';

const EditCompetitionPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const compIdFromUrl = queryParams.get('id');

    // Listes pour les sélecteurs
    const [championships, setChampionships] = useState([]);
    const [competitions, setCompetitions] = useState([]);

    // IDs sélectionnés
    const [selectedChampId, setSelectedChampId] = useState('');
    const [selectedCompId, setSelectedCompId] = useState(compIdFromUrl || '');

    // États de chargement et statut
    const [loadingInitial, setLoadingInitial] = useState(true);
    const [loadingComps, setLoadingComps] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [status, setStatus] = useState({ type: '', message: '' });

    const [formData, setFormData] = useState({
        name: '',
        description: '',
        championshipId: '',
        start: '',
        end: ''
    });

    // Fonction pour charger les compétitions d'un championnat
    const fetchCompetitions = useCallback(async (champId) => {
        if (!champId) return;
        setLoadingComps(true);
        try {
            const res = await axios.get(`http://localhost:8084/public/championship/${champId}/comp`);
            setCompetitions(res.data);
            return res.data;
        } catch (err) {
            setStatus({ type: 'danger', message: "Erreur lors du chargement des compétitions." });
        } finally {
            setLoadingComps(false);
        }
    }, []);

    // Initialisation de la page
    useEffect(() => {
        const init = async () => {
            try {
                const champsRes = await axios.get('http://localhost:8084/public/championship');
                setChampionships(champsRes.data);

                // Si on arrive avec un ID dans l'URL, on doit trouver son championnat
                if (compIdFromUrl) {
                    for (const champ of champsRes.data) {
                        const comps = await fetchCompetitions(champ.id);
                        const match = comps.find(c => c.id === parseInt(compIdFromUrl));
                        if (match) {
                            setSelectedChampId(champ.id);
                            setFormData({
                                name: match.name,
                                description: match.description,
                                championshipId: champ.id,
                                start: match.start,
                                end: match.end
                            });
                            break;
                        }
                    }
                }
            } catch (err) {
                setStatus({ type: 'danger', message: "Erreur serveur au démarrage." });
            } finally {
                setLoadingInitial(false);
            }
        };
        init();
    }, [compIdFromUrl, fetchCompetitions]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);

        const url = `http://localhost:8084/admin/comps/${selectedCompId}`;

        try {
            await axios.put(url, {
                id: parseInt(selectedCompId),
                ...formData,
                championshipId: parseInt(formData.championshipId)
            });
            setStatus({ type: 'success', message: 'Compétition mise à jour avec succès !' });
            setTimeout(() => navigate(-1), 1500);
        } catch (error) {
            setStatus({ type: 'danger', message: "Erreur lors de la sauvegarde." });
        } finally {
            setSubmitting(false);
        }
    };

    if (loadingInitial) return <Container className="py-5 text-center"><Spinner animation="border" /></Container>;

    return (
        <Container className="py-5">
            <h2 className="mb-4 text-center">🏁 Gestion des Compétitions</h2>

            {status.message && <Alert variant={status.type} dismissible onClose={() => setStatus({type:'', message:''})}>{status.message}</Alert>}

            {/* ZONE DE SÉLECTION */}
            <Card className="shadow-sm mb-4 bg-light">
                <Card.Body>
                    <Row>
                        <Col md={6}>
                            <Form.Label className="fw-bold">1. Choisir le Championnat</Form.Label>
                            <Form.Select
                                value={selectedChampId}
                                onChange={(e) => {
                                    const id = e.target.value;
                                    setSelectedChampId(id);
                                    setSelectedCompId(''); // Reset la compet si on change de champ
                                    fetchCompetitions(id);
                                }}
                            >
                                <option value="">--- Sélectionner ---</option>
                                {championships.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                            </Form.Select>
                        </Col>
                        <Col md={6}>
                            <Form.Label className="fw-bold">2. Choisir la Compétition</Form.Label>
                            <Form.Select
                                value={selectedCompId}
                                disabled={!selectedChampId || loadingComps}
                                onChange={(e) => {
                                    const id = e.target.value;
                                    setSelectedCompId(id);
                                    const comp = competitions.find(c => c.id === parseInt(id));
                                    if (comp) {
                                        setFormData({
                                            name: comp.name,
                                            description: comp.description,
                                            championshipId: selectedChampId,
                                            start: comp.start,
                                            end: comp.end
                                        });
                                    }
                                }}
                            >
                                <option value="">{loadingComps ? "Chargement..." : "--- Sélectionner ---"}</option>
                                {competitions.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                            </Form.Select>
                        </Col>
                    </Row>
                </Card.Body>
            </Card>

            {/* FORMULAIRE DE MODIFICATION */}
            {selectedCompId && (
                <Form onSubmit={handleSubmit}>
                    <Card className="shadow-sm border-success">
                        <Card.Header className="bg-success text-white">
                            Modification de : {formData.name}
                        </Card.Header>
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

                            <Form.Group className="mb-3">
                                <Form.Label className="fw-bold text-muted small">Transférer vers un autre championnat ?</Form.Label>
                                <Form.Select name="championshipId" value={formData.championshipId} onChange={handleChange}>
                                    {championships.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                </Form.Select>
                            </Form.Group>

                            <Button variant="success" size="lg" type="submit" className="w-100 mt-2" disabled={submitting}>
                                {submitting ? <Spinner size="sm" /> : "Enregistrer les modifications"}
                            </Button>
                        </Card.Body>
                    </Card>
                </Form>
            )}
        </Container>
    );
};

export default EditCompetitionPage;