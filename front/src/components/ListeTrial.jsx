import React, { useState, useEffect } from 'react';
import './ListeTrials.css';

const ListeTrials = () => {
    const [trials, setTrials] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchTrials();
    }, []);

    const fetchTrials = async () => {
        try {
            setLoading(true);
            // Endpoint mis à jour : /public/trials
            const response = await fetch('http://localhost:8082/public/trials');
            if (!response.ok) throw new Error('Erreur chargement épreuves');
            const data = await response.json();
            setTrials(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="loading">Chargement des épreuves...</div>;
    if (error) return <div className="error">Erreur: {error}</div>;

    return (
        <div className="liste-trials">
            <h2>Liste des Épreuves Sportives</h2>
            <div className="trials-grid">
                {trials.length === 0 ? (
                    <p>Aucune épreuve disponible</p>
                ) : (
                    trials.map(trial => (
                        <div key={trial.id} className="trial-card">
                            <h3>Épreuve #{trial.id}</h3>
                            <p><strong>Événement:</strong> {trial.event?.name || 'N/A'}</p>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
};

export default ListeTrials;
