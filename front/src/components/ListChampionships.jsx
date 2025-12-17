import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {getChampionships} from "../services/championshipService.jsx";


const ListChampionships = () => {
    const [championships, setChampionships] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchListCompetitions = async () => {
            try {
                const data = await getChampionships();
                setChampionships(data);
            } catch (err) {
                setError("Erreur lors du chargement des compétitions" + err);
            } finally {
                setLoading(false);
            }
        };

        fetchListCompetitions();
    }, []);

    if (loading) return <p>Chargement...</p>;
    if (error) return <p>{error}</p>;
    if (championships.length === 0) return <p>Aucun championnat disponible.</p>;

    return (
        <div className="championship-page">
            <h2 className="championship-title">Liste des championnats</h2>
            <div className="championship-container">
                {championships.map((c) => (
                    <div key={c.id}>
                        <h2>{c.name}</h2>
                        <Link to={`/public/championship/${c.id}/comp`}>
                            <button>Voir les détails</button>
                        </Link>

                    </div>
                ))}
            </div>
        </div>
    );
};

export default ListChampionships;
