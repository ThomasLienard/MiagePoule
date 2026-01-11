import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {getChampionships} from "../services/championshipService.jsx";
import {Button, Card} from "react-bootstrap";


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
        <div className="d-flex justify-content-center pt-2">
            <Card className="p-3">
                <Card.Body>
                    <Card.Title className="text-center" as="h3">Liste des championnats</Card.Title>
                    <div className="d-flex justify-content-center">
                        <hr style={{width:"3rem"}}/>
                    </div>
                    <Card.Text>
                        <div className="d-flex flex-column gap-3">
                            {championships.map((c) => (
                                <Card key={c.id} className="p-2">
                                    <Card.Title className="text-center mt-2">{c.name}</Card.Title>
                                    <Link to={`/public/championship/${c.id}/comp`} className="text-center mb-2">
                                        <Button variant="secondary">Voir les détails</Button>
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

export default ListChampionships;
