import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/ListeEvenements.css';

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
            const eventsResponse = await fetch('http://localhost:8080/public/events');
            if (!eventsResponse.ok) throw new Error('Erreur chargement événements');
            const eventsData = await eventsResponse.json();
            
            // Récupérer les trials
            const trialsResponse = await fetch('http://localhost:8080/public/trials');
            if (!trialsResponse.ok) throw new Error('Erreur chargement épreuves');
            const trialsData = await trialsResponse.json();
            
            // Extraire les IDs des événements qui sont des trials
            const trialEventIds = trialsData.map(trial => trial.event?.id).filter(id => id != null);
            
            // Filtrer les événements pour exclure ceux qui sont déjà des trials
            const nonTrialEvents = eventsData.filter(event => !trialEventIds.includes(event.id));
            
            setEvents(nonTrialEvents);
            setTrials(trialsData);
        } catch (err) {
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
        <div className="liste-evenements">
            <h1>Liste des Événements</h1>
            
            <section>
                <h2>Épreuves Sportives</h2>
                <div className="events-grid">
                    {trials.length === 0 ? (
                        <p>Aucune épreuve disponible</p>
                    ) : (
                        trials.map(trial => (
                            <div 
                                key={`trial-${trial.id}`} 
                                className="event-card trial-card"
                                onClick={() => handleTrialClick(trial.id)}
                            >
                                <h3>{trial.event?.name || `Épreuve #${trial.id}`}</h3>
                                <p className="description">{trial.event?.description || ''}</p>
                                <p className="type">{trial.event?.typeEvent?.name || 'Épreuve sportive'}</p>
                            </div>
                        ))
                    )}
                </div>
            </section>

            <section>
                <h2>Événements Non Sportifs</h2>
                <div className="events-grid">
                    {events.length === 0 ? (
                        <p>Aucun événement disponible</p>
                    ) : (
                        events.map(event => (
                            <div 
                                key={`event-${event.id}`} 
                                className="event-card"
                                onClick={() => handleEventClick(event.id)}
                            >
                                <h3>{event.name}</h3>
                                <p className="description">{event.description}</p>
                                <p className="type">{event.typeEvent?.name || 'N/A'}</p>
                            </div>
                        ))
                    )}
                </div>
            </section>
        </div>
    );
};

export default ListeEvenements;
