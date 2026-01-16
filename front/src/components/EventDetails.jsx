import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

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
                ? `http://localhost:8083/public/trials/${id}`
                : `http://localhost:8083/public/events/${id}`;
                
            const response = await fetch(endpoint);
            if (!response.ok) throw new Error('Événement non trouvé');
            const data = await response.json();
            
            console.log('Data received:', data); // Debug
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

    return (
        <div className="event-details">
            <button onClick={() => navigate(-1)} className="back-button">
                ← Retour à la liste
            </button>
            
            <div className="details-container">
                <div className="header">
                    <h1>{eventData.name}</h1>
                    {isTrial && <span className="badge trial-badge">🏆 Épreuve Sportive</span>}
                </div>
                
                <div className="info-section">
                    <h2>📋 Description</h2>
                    <p>{eventData.description || 'Aucune description disponible'}</p>
                </div>

                {eventData.competitionName && (
                    <div className="info-section">
                        <h2>🏅 Compétition</h2>
                        <div className="info-item">
                            <span className="info-value">{eventData.competitionName}</span>
                        </div>
                    </div>
                )}

                {eventData.timeSlot && (
                    <div className="info-section">
                        <h2>⏰ Horaires</h2>
                        <div className="info-grid">
                            <div className="info-item">
                                <strong>Début:</strong>
                                <span>{new Date(eventData.timeSlot.start).toLocaleString('fr-FR', {
                                    dateStyle: 'full',
                                    timeStyle: 'short'
                                })}</span>
                            </div>
                            <div className="info-item">
                                <strong>Fin:</strong>
                                <span>{new Date(eventData.timeSlot.end).toLocaleString('fr-FR', {
                                    dateStyle: 'full',
                                    timeStyle: 'short'
                                })}</span>
                            </div>
                            <div className="info-item">
                                <strong>Durée:</strong>
                                <span>
                                    {Math.round(
                                        (new Date(eventData.timeSlot.end) - new Date(eventData.timeSlot.start)) 
                                        / (1000 * 60)
                                    )} minutes
                                </span>
                            </div>
                        </div>
                    </div>
                )}

                {eventData.place && (
                    <div className="info-section">
                        <h2>📍 Lieu</h2>
                        <div className="place-card">
                            <h3>{eventData.place.name}</h3>
                            {eventData.place.description && (
                                <p className="place-description">{eventData.place.description}</p>
                            )}
                            <div className="place-details">
                                <div className="place-address">
                                    <strong>Adresse:</strong>
                                    <p>
                                        {eventData.place.number} {eventData.place.street}<br/>
                                        {eventData.place.zip} {eventData.place.city}
                                    </p>
                                </div>
                                <div className="place-features">
                                    {eventData.place.parking && (
                                        <span className="feature-badge">🅿️ Parking disponible</span>
                                    )}
                                    {eventData.place.latitude && eventData.place.longitude && (
                                        <span className="feature-badge">
                                            🗺️ Coordonnées: {eventData.place.latitude.toFixed(2)}, {eventData.place.longitude.toFixed(2)}
                                        </span>
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* Section Classement / Résultats */}
                {eventData.rankings && eventData.rankings.length > 0 && (
                    <div className="info-section">
                        <h2>🏆 Classement</h2>
                        <div className="rankings-container">
                            {/* Séparation des résultats par type */}
                            {eventData.rankings.some(r => r.participantType === 'TEAM') && (
                                <div className="rankings-group">
                                    <h3>Équipes</h3>
                                    <table className="rankings-table">
                                        <thead>
                                            <tr>
                                                <th>Rang</th>
                                                <th>Équipe</th>
                                                <th>Résultat</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {eventData.rankings
                                                .filter(r => r.participantType === 'TEAM')
                                                .map((ranking, index) => (
                                                    <tr key={`team-${ranking.participantId}-${index}`} className={`rank-${ranking.rank}`}>
                                                        <td className="rank-cell">
                                                            {ranking.rank === 1 && '🥇'}
                                                            {ranking.rank === 2 && '🥈'}
                                                            {ranking.rank === 3 && '🥉'}
                                                            {ranking.rank > 3 && ranking.rank}
                                                        </td>
                                                        <td className="participant-name">{ranking.participantName}</td>
                                                        <td className="result-cell">{ranking.result}</td>
                                                    </tr>
                                                ))
                                            }
                                        </tbody>
                                    </table>
                                </div>
                            )}
                            
                            {eventData.rankings.some(r => r.participantType === 'ATHLETE') && (
                                <div className="rankings-group">
                                    <h3>Athlètes</h3>
                                    <table className="rankings-table">
                                        <thead>
                                            <tr>
                                                <th>Rang</th>
                                                <th>Athlète</th>
                                                <th>Résultat</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {eventData.rankings
                                                .filter(r => r.participantType === 'ATHLETE')
                                                .map((ranking, index) => (
                                                    <tr key={`athlete-${ranking.participantId}-${index}`} className={`rank-${ranking.rank}`}>
                                                        <td className="rank-cell">
                                                            {ranking.rank === 1 && '🥇'}
                                                            {ranking.rank === 2 && '🥈'}
                                                            {ranking.rank === 3 && '🥉'}
                                                            {ranking.rank > 3 && ranking.rank}
                                                        </td>
                                                        <td className="participant-name">{ranking.participantName}</td>
                                                        <td className="result-cell">{ranking.result}</td>
                                                    </tr>
                                                ))
                                            }
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default EventDetails;
