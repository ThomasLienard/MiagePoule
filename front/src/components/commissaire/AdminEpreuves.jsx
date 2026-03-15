import React, { useState, useEffect } from 'react';
import { Container, Card, Button, Spinner, Alert, Badge, Modal, Form } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import participantService from '../../services/participantService';
import resultService from '../../services/resultService';

const AdminEpreuves = () => {
    const [trials, setTrials] = useState([]);
    const [resultsStats, setResultsStats] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    // États pour le Modal d'annulation
    const [showCancelModal, setShowCancelModal] = useState(false);
    const [selectedTrial, setSelectedTrial] = useState(null);
    const [cancelReason, setCancelReason] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        fetchTrials();
    }, []);

    const fetchTrials = async () => {
        try {
            setLoading(true);
            const data = await participantService.getAllTrials();
            setTrials(data);

            // Charge les stats de résultats pour chaque épreuve en parallèle
            const statsEntries = await Promise.all(
                data.map(async (trial) => {
                    try {
                        const res = await resultService.getTrialResults(trial.trialId);
                        const nonForfeit = res.results?.filter(r => !r.isForfeit) ?? [];
                        const total = nonForfeit.length;
                        const validated = nonForfeit.filter(r => r.isValidated).length;
                        return [trial.trialId, { validated, total }];
                    } catch {
                        return [trial.trialId, { validated: 0, total: 0 }];
                    }
                })
            );
            setResultsStats(Object.fromEntries(statsEntries));
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleShowCancel = (trial) => {
        setSelectedTrial(trial);
        setCancelReason("");
        setShowCancelModal(true);
    };

    const handleCloseCancel = () => {
        setShowCancelModal(false);
        setSelectedTrial(null);
    };

    const confirmCancel = async () => {
        if (!cancelReason.trim()) {
            alert("Veuillez saisir une raison pour l'annulation.");
            return;
        }

        setIsSubmitting(true);
        try {
            // Appel au endpoint PATCH pour l'annulation
            await axios.patch(`http://localhost:8084/commissaire/events/${selectedTrial.trialId}/cancel`, {
                reason: cancelReason
            });

            handleCloseCancel();
            // Rafraîchir la liste : l'épreuve disparaîtra grâce au filtre ci-dessous
            await fetchTrials();
        } catch (err) {
            console.error("Erreur lors de l'annulation de l'épreuve:", err);
            alert("Erreur lors de l'annulation de l'épreuve.");
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) {
        return (
            <Container className="py-4 text-center">
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">Chargement...</span>
                </Spinner>
            </Container>
        );
    }

    if (error) {
        return (
            <Container className="py-4">
                <Alert variant="danger">{error}</Alert>
            </Container>
        );
    }

    return (
        <Container className="py-4">
            <h1 className="mb-4">🏆 Administration des épreuves</h1>
            <p className="text-muted mb-4">
                Gérer les participants et l'état des épreuves en tant que commissaire.
            </p>

            {error && <Alert variant="danger">{error}</Alert>}

            <Card className="shadow-sm">
                <Card.Header as="h5" className="bg-white">Épreuves à gérer</Card.Header>
                <Card.Body className="overflow-auto" style={{ maxHeight: '70vh' }}>
                    {trials.filter(t => t.status !== 'CANCELLED').length === 0 ? (
                        <p className="text-center text-muted my-4">Aucune épreuve active à gérer.</p>
                    ) : (
                        <div className="d-flex flex-column gap-3">
                            {trials
                                .filter(trial => trial.status !== 'CANCELLED')
                                .map((trial) => (
                                    <Card key={trial.trialId} className="border-start border-4">
                                        <Card.Body>
                                            <div className="d-flex justify-content-between align-items-center">
                                                <div>
                                                    <Card.Title className="mb-1">{trial.trialName}</Card.Title>
                                                    <div className="d-flex gap-2">
                                                        <Badge bg={trial.teamTrial ? 'info' : 'success'}>
                                                            {trial.teamTrial ? '👥 Équipe' : '🏃 Solo'}
                                                        </Badge>
                                                        <Badge bg="secondary">
                                                            {trial.participants?.length || 0} participant(s)
                                                        </Badge>
                                                        <Badge bg="warning" text="dark">
                                                            {resultsStats[trial.trialId]?.validated ?? 0}/
                                                            {resultsStats[trial.trialId]?.total ?? 0} validés
                                                        </Badge>
                                                    </div>
                                                </div>
                                                <div className="d-flex gap-2">
                                                    <Button
                                                        variant="outline-secondary"
                                                        size="sm"
                                                        onClick={() => navigate(`/commissaire/trials/${trial.trialId}/participants`)}
                                                    >
                                                        Modifier les participants
                                                    </Button>
                                                    <Button
                                                        variant="outline-secondary"
                                                        size="sm"
                                                        onClick={() => navigate(`/commissaire/update-event?id=${trial.trialId}`)}
                                                    >
                                                        Modifier date
                                                    </Button>
                                                    <Button
                                                        variant="outline-secondary"
                                                        onClick={() => navigate(`/commissaire/trials/${trial.trialId}/results`)}
                                                    >
                                                        Gérer résultats
                                                    </Button>
                                                    <Button
                                                        variant="outline-danger"
                                                        size="sm"
                                                        onClick={() => handleShowCancel(trial)}
                                                    >
                                                        Annuler
                                                    </Button>
                                                </div>
                                            </div>
                                        </Card.Body>
                                </Card>
                            ))}
                        </div>
                    )}
                </Card.Body>
            </Card>

            <Button
                variant="outline-secondary"
                className="mt-3"
                onClick={() => navigate(-1)}
            >
                ← Retour
            </Button>
            {/* MODAL D'ANNULATION */}
            <Modal show={showCancelModal} onHide={handleCloseCancel} centered>
                <Modal.Header closeButton>
                    <Modal.Title className="text-danger">🚫 Annulation d'épreuve</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p>Confirmez-vous l'annulation de : <strong>{selectedTrial?.trialName}</strong> ?</p>
                    <Form.Group className="mt-3">
                        <Form.Label className="fw-bold">Raison de l'annulation :</Form.Label>
                        <Form.Control
                            as="textarea"
                            rows={3}
                            placeholder="Ex: Conditions météo, problème technique..."
                            value={cancelReason}
                            onChange={(e) => setCancelReason(e.target.value)}
                            autoFocus
                        />
                        <Form.Text className="text-muted">
                            Cette raison sera ajoutée à la description de l'épreuve.
                        </Form.Text>
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer className="bg-light">
                    <Button variant="secondary" onClick={handleCloseCancel} disabled={isSubmitting}>
                        Abandonner
                    </Button>
                    <Button variant="danger" onClick={confirmCancel} disabled={isSubmitting}>
                        {isSubmitting ? <Spinner size="sm" className="me-2" /> : null}
                        Confirmer l'annulation
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
};

export default AdminEpreuves;
