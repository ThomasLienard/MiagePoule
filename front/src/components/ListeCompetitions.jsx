import React, { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import {getCompetitionsByChampionship} from "../services/competitionService.jsx";
import {Button, Card} from "react-bootstrap";


const ListeCompetitions = () => {
    const {id: championshipId} = useParams();
    const [competitions, setCompetitions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

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
        <div className="d-flex justify-content-center pt-2">
            <Card className="p-3">
                <Card.Body>
                    <Button onClick={() => navigate(-1)} className="back-button">
                        ← Retour au championnat
                    </Button>
                    <Card.Title className="text-center" as="h3">Liste des compétitions</Card.Title>
                    <div className="d-flex justify-content-center">
                        <hr style={{width:"3rem"}}/>
                    </div>
                    <Card.Text>
                        <div className="d-flex flex-column gap-3">
                            {competitions.map((c) => (
                                <Card key={c.id} className="p-2">
                                    <Card.Title className="text-center mt-2">{c.name}</Card.Title>
                                    <Link to={`/public/championship/${championshipId}/comp/${c.id}`} className="text-center mb-2">
                                        <Button variant="secondary">Voir les événements</Button>
                                    </Link>
                                </Card>
                            ))}
                        </div>
                    </Card.Text>
                </Card.Body>
            </Card>
        </div>
    );
};

export default ListeCompetitions;
