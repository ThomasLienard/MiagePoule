import React, {useEffect, useState} from "react";
import {Link} from "react-router-dom";
import {getCompetitionsByChampionship} from "../services/competitionService.jsx";
import {Button, Card} from "react-bootstrap";
import {formatDate} from "../utils/dateFormatter.js";

const Championship = ({championship}) => {
    const [competitions, setCompetitions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchCompetitions = async () => {
            try {
                const data = await getCompetitionsByChampionship(championship.id);
                setCompetitions(data)
            } catch (err) {
                setError("Erreur lors du chargement des compétitions" + err);
            } finally {
                setLoading(false);
            }
        };

        fetchCompetitions();
    }, [championship]);

    const formatSingleDate = (dateStr) => {
        const [year, month, day] = dateStr.split("-");
        return `${day}/${month}/${year}`;
    }

    if (loading) return <p>Chargement...</p>;
    if (error) return <p>{error}</p>;

    return (
        <Card className="overflow-y-auto" style={{"height": "70vh"}}>
            <Card.Body className="m-3">
                <Card.Title className="text-center" as="h3">{championship.name}</Card.Title>
                <Card.Subtitle className="text-center text-body-tertiary">
                    <div>{formatSingleDate(championship.start)} - {formatSingleDate(championship.end)}</div>
                    <div className="pt-1">{championship.description}</div>
                </Card.Subtitle>
                <div className="d-flex justify-content-center">
                    <hr style={{width: "3rem"}}/>
                </div>
                <div className="d-flex flex-column gap-3">
                    {competitions.length === 0 && (
                        <span className="text-center">Aucune compétition associée</span>
                    )}
                    {competitions.map((c) => (
                        <Card key={c.id} className="px-3 py-2">
                            <Card.Title className="text-center mt-2">{c.name}</Card.Title>
                            <Card.Subtitle
                                className="text-center text-body-tertiary small pb-3">{formatDate(c.start, c.end)}</Card.Subtitle>
                            <Link to={`/public/championship/${championship.id}/comp/${c.id}`}
                                  className="text-center mb-2">
                                <Button variant="secondary">Voir les événements</Button>
                            </Link>
                        </Card>
                    ))}
                </div>
            </Card.Body>
        </Card>
    )
}
export default Championship;
