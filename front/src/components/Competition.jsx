import React, {useEffect, useState} from "react";
import {Link, useParams} from "react-router-dom";
import {getCompetitionById} from "../services/competitionService.jsx";
import {Button, Card} from "react-bootstrap";

const Competition = () => {
    const {id: championshipId, idComp} = useParams();
    const [competition, setCompetition] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

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
        <div className="d-flex justify-content-center pt-2">
            <Card className="p-3">
                <Card.Body>
                    <Card.Title className="text-center" as="h3">{name}</Card.Title>
                    <Card.Subtitle className="text-center text-body-tertiary">{description}</Card.Subtitle>
                    <div className="d-flex justify-content-center">
                        <hr style={{width:"3rem"}}/>
                    </div>
                    <Card.Text className="mt-2">
                        <p><strong>Date de début:</strong> {formatDate(start)}</p>
                        <p><strong>Date de fin:</strong> {formatDate(end)}</p>
                    </Card.Text>
                </Card.Body>
            </Card>
        </div>
    );
};

function formatDate(dateStr) {
    const [year, month, day] = dateStr.split("-");
    return `${day}/${month}/${year}`;
}


export default Competition;
