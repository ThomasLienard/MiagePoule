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
            const isTrialPath = currentPath.includes('/trials/');
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
    const tasks = event.tasks || [];
    const metrics = event.metrics || [];

    return (
        <div className="event-details">
            <button onClick={() => navigate('/public/events')} className="back-button">
                ← Retour à la liste
            </button>
            
            <div className="details-container">
                <div className="header">
                    <h1>{event.name}</h1>
                    {isTrial && <span className="badge trial-badge">🏆 Épreuve Sportive</span>}
                    <span className="badge type-badge">{event.typeEvent?.name || 'N/A'}</span>
                </div>
                
                <div className="info-section">
                    <h2>📋 Description</h2>
                    <p>{event.description || 'Aucune description disponible'}</p>
                </div>

                {event.competition && (
                    <div className="info-section">
                        <h2>🏅 Compétition</h2>
                        <div className="info-grid">
                            <div className="info-item">
                                <strong>Nom : </strong>
                                <span>{event.competition.name}</span>
                            </div>
                            <div className="info-item">
                                <strong>Description : </strong>
                                <span>{event.competition.description || 'N/A'}</span>
                            </div>
                            <div className="info-item">
                                <strong>Dates : </strong>
                                <span>
                                    {new Date(event.competition.start).toLocaleDateString('fr-FR')} 
                                    {' - '}
                                    {new Date(event.competition.end).toLocaleDateString('fr-FR')}
                                </span>
                            </div>
                        </div>
                    </div>
                )}

                {event.timeSlot && (
                    <div className="info-section">
                        <h2>⏰ Horaires</h2>
                        <div className="info-grid">
                            <div className="info-item">
                                <strong>Début : </strong>
                                <span>{new Date(event.timeSlot.start).toLocaleString('fr-FR', {
                                    dateStyle: 'full',
                                    timeStyle: 'short'
                                })}</span>
                            </div>
                            <div className="info-item">
                                <strong>Fin :</strong>
                                <span>{new Date(event.timeSlot.end).toLocaleString('fr-FR', {
                                    dateStyle : 'full',
                                    timeStyle : 'short'
                                })}</span>
                            </div>
                            <div className="info-item">
                                <strong>Durée : </strong>
                                <span>
                                    {Math.round((new Date(event.timeSlot.end) - new Date(event.timeSlot.start)) / (1000 * 60))} minutes
                                </span>
                            </div>
                        </div>
                    </div>
                )}

                {event.place && (
                    <div className="info-section">
                        <h2>📍 Lieu</h2>
                        <div className="place-card">
                            <h3>{event.place.name}</h3>
                            <p className="place-description">{event.place.description || ''}</p>
                            <div className="place-details">
                                <div className="place-address">
                                    <strong>Adresse:</strong>
                                    <p>
                                        {event.place.number} {event.place.street}<br/>
                                        {event.place.zip} {event.place.city}
                                    </p>
                                </div>
                                <div className="place-features">
                                    {event.place.parking && (
                                        <span className="feature-badge">🅿️ Parking disponible</span>
                                    )}
                                    {event.place.latitude && event.place.longitude && (
                                        <span className="feature-badge">
                                            🗺️ GPS: {event.place.latitude.toFixed(2)}, {event.place.longitude.toFixed(2)}
                                        </span>
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                <div className="info-section">
                    <h2>👥 Participants ({participants.length})</h2>
                    {participants.length === 0 ? (
                        <p className="empty-message">Aucun participant inscrit pour le moment</p>
                    ) : (
                        <div className="participants-grid">
                            {participants.map((user) => (
                                <div key={user.id} className="participant-card">
                                    <div className="participant-header">
                                        <div className="participant-avatar">
                                            {user.name.charAt(0)}{user.lastname.charAt(0)}
                                        </div>
                                        <div className="participant-info">
                                            <h3>{user.name} {user.lastname}</h3>
                                            <p className="participant-email">{user.email}</p>
                                        </div>
                                    </div>
                                    <div className="participant-details">
                                        {user.country && (
                                            <div className="participant-detail">
                                                <span className="detail-label">Pays:</span>
                                                <span className="detail-value">{user.country.code}</span>
                                            </div>
                                        )}
                                        {user.role && (
                                            <div className="participant-detail">
                                                <span className="detail-label">Rôle:</span>
                                                <span className="detail-value">{user.role.roleName}</span>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {tasks.length > 0 && (
                    <div className="info-section">
                        <h2>📝 Tâches associées ({tasks.length})</h2>
                        <div className="tasks-list">
                            {tasks.map((task) => (
                                <div key={task.id} className="task-item">
                                    <h4>{task.name}</h4>
                                    <p>{task.description}</p>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {metrics.length > 0 && (
                    <div className="info-section">
                        <h2>📊 Métriques ({metrics.length})</h2>
                        <div className="metrics-list">
                            {metrics.map((metric, index) => (
                                <div key={index} className="metric-item">
                                    <p>Métrique #{index + 1}</p>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default EventDetails;
