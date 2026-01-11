import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {Badge, Button, Card, Col, Row} from "react-bootstrap";

const ListeEvenements = () => {
    const [events, setEvents] = useState([]);
    const [trials, setTrials] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        fetchAllData();
    }, []);

    const fetchAllData = async () => {
        try {
            setLoading(true);
            
            // Récupérer les événements
            const eventsResponse = await fetch('http://localhost:8083/public/events');
            if (!eventsResponse.ok) throw new Error('Erreur chargement événements');
            const eventsData = await eventsResponse.json();
            
            // Récupérer les trials
            const trialsResponse = await fetch('http://localhost:8083/public/trials');
            if (!trialsResponse.ok) throw new Error('Erreur chargement épreuves');
            const trialsData = await trialsResponse.json();
            
            console.log('Events:', eventsData); // Debug
            console.log('Trials:', trialsData); // Debug
            
            // Créer un Set des IDs d'événements qui sont des trials
            const trialEventIds = new Set(trialsData.map(trial => trial.idEvent));
            
            console.log('Trial Event IDs:', Array.from(trialEventIds)); // Debug
            
            // Filtrer les événements pour exclure ceux qui sont des trials
            const nonTrialEvents = eventsData.filter(event => !trialEventIds.has(event.id));
            
            console.log('Filtered events:', nonTrialEvents); // Debug
            
            setEvents(nonTrialEvents);
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
            <div>
                <h2>🏆 Épreuves Sportives</h2>
                    <div className="events-grid">
                        <Row>
                        {trials.length === 0 ? (
                            <p className="empty-message">Aucune épreuve disponible</p>
                        ) : (
                            trials.map(trial => (
                                <Col xs={12} md={4}
                                    key={`trial-${trial.id}`}
                                    onClick={() => handleTrialClick(trial.id)}
                                >
                                    <Card>
                                        <Card.Body>
                                            <Card.Title className="d-flex justify-content-center">
                                                {trial.name}
                                                <Badge bg="primary" className="ms-2">Épreuve</Badge>
                                            </Card.Title>
                                            <Card.Text className="d-flex justify-content-center">
                                                {trial.description || 'Aucune description'}
                                            </Card.Text>
                                        </Card.Body>
                                        <div className="d-flex justify-content-center mb-2">
                                            <Button variant="secondary">Voir les détails</Button>
                                        </div>
                                    </Card>
                                </Col>
                            ))
                        )}
                        </Row>
                    </div>
            </div>

            <div>
                <h2>📅 Événements</h2>
                <Row>
                    {trials.length === 0 ? (
                        <p className="empty-message">Aucune événement disponible</p>
                    ) : (
                        events.map(event => (
                            <Col xs={12} md={4}
                                 key={`event-${event.id}`}
                                 onClick={() => handleEventClick(event.id)}
                            >
                                <Card>
                                    <Card.Body>
                                        <Card.Title className="d-flex justify-content-center">
                                            {event.name}
                                        </Card.Title>
                                        <Card.Text className="d-flex justify-content-center">
                                            {event.description || 'Aucune description'}
                                        </Card.Text>
                                    </Card.Body>
                                    <div className="d-flex justify-content-center mb-2">
                                        <Button variant="secondary">Voir les détails</Button>
                                    </div>
                                </Card>
                            </Col>
                        ))
                    )}
                </Row>
            </div>
        </div>
    );
};

export default ListeEvenements;


