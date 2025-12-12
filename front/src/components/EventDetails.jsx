import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import '../styles/EventDetails.css';

const EventDetails = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [eventData, setEventData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isTrial, setIsTrial] = useState(false);

    useEffect(() => {
        fetchEventDetails();
    }, [id]);

    const fetchEventDetails = async () => {
        try {
            setLoading(true);
            
            // Déterminer si c'est un trial ou un event
            const currentPath = window.location.pathname;
            const isTrialPath = currentPath.includes('/trial/');
            setIsTrial(isTrialPath);
            
            const endpoint = isTrialPath 
                ? `http://localhost:8080/public/trials/${id}`
                : `http://localhost:8080/public/events/${id}`;
                
            const response = await fetch(endpoint);
            if (!response.ok) throw new Error('Événement non trouvé');
            const data = await response.json();
            
            setEventData(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="loading">Chargement des détails...</div>;
    if (error) return <div className="error">Erreur: {error}</div>;
    if (!eventData) return <div className="error">Aucune donnée disponible</div>;

    // Extraire les données selon le type
    const event = isTrial ? eventData.event : eventData;
    const participants = event.users || [];

    return (
        <div className="event-details">
            <button onClick={() => navigate('/public/events')} className="back-button">
                ← Retour à la liste
            </button>
            
            <div className="details-container">
                <h1>{event.name}</h1>
                {isTrial && <span className="badge">Épreuve Sportive</span>}
                
                <div className="info-section">
                    <h2>Description</h2>
                    <p>{event.description || 'Aucune description disponible'}</p>
                </div>

                <div className="info-section">
                    <h2>Informations</h2>
                    <ul>
                        <li><strong>Type:</strong> {event.typeEvent?.name || 'N/A'}</li>
                        {event.place && (
                            <li><strong>Lieu:</strong> {event.place.name || 'N/A'}</li>
                        )}
                        {event.timeSlot && (
                            <>
                                <li><strong>Début:</strong> {new Date(event.timeSlot.startTime).toLocaleString('fr-FR')}</li>
                                <li><strong>Fin:</strong> {new Date(event.timeSlot.endTime).toLocaleString('fr-FR')}</li>
                            </>
                        )}
                    </ul>
                </div>

                <div className="info-section">
                    <h2>Participants ({participants.length})</h2>
                    {participants.length === 0 ? (
                        <p>Aucun participant inscrit</p>
                    ) : (
                        <div className="participants-list">
                            {participants.map((user, index) => (
                                <div key={index} className="participant-card">
                                    <div className="participant-name">
                                        {user.name} {user.lastname}
                                    </div>
                                    {user.country && (
                                        <div className="participant-country">
                                            {user.country.name}
                                        </div>
                                    )}
                                    {user.teams && user.teams.length > 0 && (
                                        <div className="participant-teams">
                                            Équipes: {user.teams.map(t => t.name).join(', ')}
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default EventDetails;
