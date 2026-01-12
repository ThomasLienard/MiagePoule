import React, {useEffect, useState} from "react";
import "../styles/Competition.css";
import {Link, useNavigate, useParams} from "react-router-dom";
import {getCompetitionById} from "../services/competitionService.jsx";

const Competition = () => {
    const {id: championshipId, idComp} = useParams();
    const [competition, setCompetition] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchCompetitions = async () => {
            try {
                const data = await getCompetitionById(championshipId,idComp);
                setCompetition(data);
            } catch (err) {
                setError("Erreur lors du chargement des compétitions" + err);
            } finally {
                setLoading(false);
            }
        };

        fetchCompetitions();
    }, [championshipId, idComp]);

    if (loading) return <p>Chargement...</p>;
    if (error) return <p>{error}</p>;
    if (!competition) return <p>Aucune compétition disponible.</p>;
    const { name, start, end, description } = competition;
    return (
        <div className="competition">
            <button onClick={() => navigate(-1)} className="back-button">
                ← Retour à la liste
            </button>
            <h3>{name}</h3>
            <h4> {description}</h4>
            <p><strong>Date de début:</strong> {formatDate(start)}</p>
            <p><strong>Date de fin:</strong> {formatDate(end)}</p>
            <Link to={`/public/championship/${championshipId}/comp/${idComp}/events`}>
                <button>Voir les détails</button>
            </Link>
        </div>
    );
};

function formatDate(dateStr) {
    const [year, month, day] = dateStr.split("-");
    return `${day}/${month}/${year}`;
}


export default Competition;
