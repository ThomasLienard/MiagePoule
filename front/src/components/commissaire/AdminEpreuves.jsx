import React, { useState, useEffect } from 'react';
import { Container, Card, Button, Spinner, Alert, Badge } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import participantService from '../../services/participantService';
import resultService from '../../services/resultService';

const AdminEpreuves = () => {
    const [trials, setTrials] = useState([]);
    const [resultsStats, setResultsStats] = useState({}); // { trialId: { validated, total } }
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

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
                Gérer les participants des épreuves en tant que commissaire.
            </p>

            <Card>
                <Card.Header as="h5">Vos épreuves</Card.Header>
                <Card.Body className="overflow-auto" style={{ maxHeight: '70vh' }}>
                    {trials.length === 0 ? (
                        <p className="text-center text-muted">Aucune épreuve disponible</p>
                    ) : (
                        <div className="d-flex flex-column gap-3">
                            {trials.map((trial) => (
                                <Card key={trial.trialId} className="shadow-sm">
                                    <Card.Body>
                                        <div className="d-flex justify-content-between align-items-start">
                                            <div>
                                                <Card.Title>{trial.trialName}</Card.Title>
                                                <div className="mb-2">
                                                    <Badge bg={trial.teamTrial ? 'info' : 'success'} className="me-2">
                                                        {trial.teamTrial ? '👥 Équipe' : '🏃 Solo'}
                                                    </Badge>
                                                    <Badge bg="secondary">
                                                        {trial.participants?.length || 0} participant(s)
                                                    </Badge>
                                                    {resultsStats[trial.trialId] && (() => {
                                                        const { validated, total } = resultsStats[trial.trialId];
                                                        const allValidated = total > 0 && validated === total;
                                                        const noneValidated = validated === 0;
                                                        return (
                                                            <Badge bg={allValidated ? 'success' : noneValidated ? 'secondary' : 'warning'}>
                                                                ✔ {validated}/{total} résultat(s) validé(s)
                                                            </Badge>
                                                        );
                                                    })()}
                                                </div>
                                            </div>
                                            <div className="d-flex gap-2">
                                                <Button 
                                                    variant="outline-secondary"
                                                    onClick={() => navigate(`/commissaire/trials/${trial.trialId}/participants`)}
                                                >
                                                    Modifier participants
                                                </Button>
                                                <Button
                                                    variant="outline-secondary"
                                                    onClick={() => navigate(`/commissaire/trials/${trial.trialId}/results`)}
                                                >
                                                     Gérer résultats
                                                </Button>
                                                <Button
                                                    variant="outline-secondary"
                                                    onClick={() => navigate(`/commissaire/update-event`)}
                                                >
                                                    Modifier la date de l'épreuve
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
        </Container>
    );
};

export default AdminEpreuves;
