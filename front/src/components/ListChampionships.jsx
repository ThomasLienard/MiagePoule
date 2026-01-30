import React, {useEffect, useState} from "react";
import {getChampionships} from "../services/championshipService.jsx";
import {Col, Container, Row} from "react-bootstrap";
import Championship from "./Championship.jsx";


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
        <Container>
            <Row className="g-0">
                {championships.map((championship) => (
                    <Col xs={12} md={6} key={`championship-${championship.id}`} className="py-3">
                        <Container>
                            <Championship championship={championship}/>
                        </Container>
                    </Col>
                ))}
            </Row>
        </Container>
    );
};

export default ListChampionships;
