import React, { useEffect, useState } from "react";
import axios from "axios";
import Competition from "./Competition.jsx";
import competitionsData from "../dataTest/competitions.json";
import "../styles/ListeCompetition.css";
import { useParams } from "react-router-dom";


const ListeCompetitions = () => {
    const {id: championshipId} = useParams();
    const [competitions, setCompetitions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchCompetitions = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/public/championship/${championshipId}/comp`);
                setCompetitions(response.data);
            } catch (err) {
                setError("Erreur lors du chargement des compétitions");
            } finally {
                setLoading(false);
            }
        };

        fetchCompetitions();
    }, []);

    if (loading) return <p>Chargement...</p>;
    if (error) return <p>{error}</p>;
    if (competitions.length === 0) return <p>Aucune compétition disponible.</p>;

    return (
        <div className="competition-page">
            <h2 className="competition-title">Liste des compétitions</h2>
            <div className="competition-container">
                {competitions.map((c) => (
                    <Competition key={c.id} competition={c} />
                ))}
            </div>
        </div>
    );
};

export default ListeCompetitions;
