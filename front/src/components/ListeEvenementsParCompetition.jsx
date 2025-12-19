import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { fetchEventAndTrialsByCompetition } from '../services/eventService';
import '../styles/ListeEvenements.css';

const ListeEvenementsParCompetition = () => {
    const [events, setEvents] = useState([]);
    const [trials, setTrials] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const { championshipId, competitionId } = useParams();

    useEffect(() => {
        fetchAllData();
    }, [championshipId, competitionId]);

    const fetchAllData = async () => {
        try {
            setLoading(true);
            const { events, trials } = await fetchEventAndTrialsByCompetition(championshipId, competitionId);
            
            // Créer un Set des IDs d'événements qui sont des trials
            const trialEventIds = new Set(trials.map(trial => trial.idEvent));
            
            // Filtrer les événements pour exclure ceux qui sont des trials
            const nonTrialEvents = events.filter(event => !trialEventIds.has(event.id));
            
            setEvents(nonTrialEvents);
            setTrials(trials);
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
        <div className="liste-evenements">
            <h1>Événements de la Compétition</h1>
            
            {/* Section Épreuves Sportives */}
            <section className="events-section">
                <h2>🏆 Épreuves Sportives</h2>
                <div className="events-grid">
                    {trials.length === 0 ? (
                        <p className="empty-message">Aucune épreuve disponible</p>
                    ) : (
                        trials.map(trial => (
                            <div 
                                key={`trial-${trial.id}`} 
                                className="event-card trial-card"
                                onClick={() => handleTrialClick(trial.id)}
                            >
                                <div className="card-badge">Épreuve</div>
                                <h3>{trial.name}</h3>
                                <p className="description">
                                    {trial.description || 'Aucune description'}
                                </p>
                                <button className="view-details">Voir les détails →</button>
                            </div>
                        ))
                    )}
                </div>
            </section>

            {/* Section Événements Non Sportifs */}
            <section className="events-section">
                <h2>📅 Événements</h2>
                <div className="events-grid">
                    {events.length === 0 ? (
                        <p className="empty-message">Aucun événement disponible</p>
                    ) : (
                        events.map(event => (
                            <div 
                                key={`event-${event.id}`} 
                                className="event-card"
                                onClick={() => handleEventClick(event.id)}
                            >
                                <h3>{event.name}</h3>
                                <p className="description">
                                    {event.description || 'Aucune description'}
                                </p>
                                <button className="view-details">Voir les détails →</button>
                            </div>
                        ))
                    )}
                </div>
            </section>
        </div>
    );
};

export default ListeEvenementsParCompetition;
