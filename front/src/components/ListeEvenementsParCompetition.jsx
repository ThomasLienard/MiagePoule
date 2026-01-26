import React, {useState, useEffect} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import {eventService} from '../services/eventService';
import {Button, Card, Col, Row} from "react-bootstrap";

const ListeEvenementsParCompetition = () => {
    const [events, setEvents] = useState([]);
    const [trials, setTrials] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const {championshipId, competitionId} = useParams();

    useEffect(() => {
        fetchAllData();
    }, [championshipId, competitionId]);

    const fetchAllData = async () => {
        try {
            setLoading(true);
            const [justEvents, trialsData] = await Promise.all([
                eventService.getJustEventsByCompetition(championshipId, competitionId),
                eventService.getTrialsByCompetition(championshipId, competitionId)
            ]);

            setEvents(justEvents);
            setTrials(trialsData);
        } catch (err) {
            console.error('Fetch error:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleEventClick = (id) => {
        navigate(`/public/events/${id}`);
    };

    const handleTrialClick = (id) => {
        navigate(`/public/trials/${id}`);
    };

    if (loading) return <div className="loading">Chargement des événements...</div>;
    if (error) return <div className="error">Erreur: {error}</div>;

    return (
        <div className="pt-2">
            <Button variant="outline-secondary" onClick={() => navigate(-1)}>
                ← Retour
            </Button>
            <h2 className="text-center">🏆 Épreuves Sportives</h2>
            <Row className="row-gap-3 p-2">
                {trials.length === 0 ? (
                    <p>Aucune épreuve disponible</p>
                ) : (
                    trials.map(trial => (
                        <Col xs={12} md={4}
                             key={`trial-${trial.id}`}
                             className="d-flex justify-content-center"
                        >
                            <Card style={{width: "24rem"}}>
                                <Card.Body>
                                    <Card.Title className="d-flex justify-content-center">
                                        {trial.name}
                                    </Card.Title>
                                    <Card.Text className="d-flex justify-content-center">
                                        {trial.description || 'Aucune description'}
                                    </Card.Text>
                                </Card.Body>
                                <div className="d-flex justify-content-center mb-2">
                                    <Button variant="secondary" onClick={() => handleTrialClick(trial.id)}>Voir les
                                        détails</Button>
                                </div>
                            </Card>
                        </Col>
                    ))
                )}
            </Row>
            <div className="d-flex justify-content-center">
                <hr style={{width: "32rem"}}/>
            </div>
            <h2 className="text-center">📅 Événements</h2>
            <Row className="row-gap-3 p-2">
                {events.length === 0 ? (
                    <p>Aucune événement disponible</p>
                ) : (
                    events.map(event => (
                        <Col xs={12} md={4}
                             key={`event-${event.id}`}
                             className="d-flex justify-content-center"
                        >
                            <Card style={{width: "24rem"}}>
                                <Card.Body>
                                    <Card.Title className="d-flex justify-content-center">
                                        {event.name}
                                    </Card.Title>
                                    <Card.Text className="d-flex justify-content-center">
                                        {event.description || 'Aucune description'}
                                    </Card.Text>
                                </Card.Body>
                                <div className="d-flex justify-content-center mb-2">
                                    <Button variant="secondary" onClick={() => handleEventClick(event.id)}>Voir les
                                        détails</Button>
                                </div>
                            </Card>
                        </Col>
                    ))
                )}
            </Row>
        </div>
    );
};

export default ListeEvenementsParCompetition;
