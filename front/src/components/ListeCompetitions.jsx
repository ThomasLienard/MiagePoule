import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import {getCompetitionsByChampionship} from "../services/competitionService.jsx";


const ListeCompetitions = () => {
    const {id: championshipId} = useParams();
    const [competitions, setCompetitions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchCompetitions = async () => {
            try {
                const data = await getCompetitionsByChampionship(championshipId);
                setCompetitions(data);
            } catch (err) {
                setError("Erreur lors du chargement des compétitions" + err);
            } finally {
                setLoading(false);
            }
        };

        fetchCompetitions();
    }, [championshipId]);

    if (loading) return <p>Chargement...</p>;
    if (error) return <p>{error}</p>;
    if (competitions.length === 0) return <p>Aucune compétition disponible.</p>;

    return (
        <div className="competition-page">
            <h2 className="competition-title">Liste des compétitions</h2>
            <div className="competition-container">
                {competitions.map((c) => (
                    <div key={c.id} className="competition-card">
                        <h2>{c.name}</h2>
                        <Link to={`/public/championship/${championshipId}/comp/${c.id}`}>
                            <button>Voir les détails</button>
                        </Link>

                    </div>
                ))}
            </div>
        </div>
    );
};

export default ListeCompetitions;
