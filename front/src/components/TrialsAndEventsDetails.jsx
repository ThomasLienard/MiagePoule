import React, {useState, useEffect} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import {Accordion, Badge, Button, ListGroup, ListGroupItem} from "react-bootstrap";
import {eventService} from "../services/eventService.jsx";
import RankingFormat from "./common/RankingFormat.jsx";

const TrialsAndEventsDetails = () => {
    const {id} = useParams();
    const [eventData, setEventData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isTrial, setIsTrial] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        fetchDetails();
    }, [id]);

    const fetchDetails = async () => {
        try {
            setLoading(true);

            // Déterminer si c'est un trial ou un event
            const currentPath = window.location.pathname;
            const isTrialPath = currentPath.includes('/trials/');
            setIsTrial(isTrialPath);

            let data;
            if (isTrialPath) {
                data = await eventService.getTrialById(id);
            } else {
                data = await eventService.getEventById(id);
            }

            setEventData(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="loading">Chargement des détails...</div>;
    if (error) return <div className="error">Erreur: {error}</div>;
    if (!eventData) return <div className="error">Aucune donnée disponible</div>;

    return (
        <>
            <h2 className="text-center">{eventData.name}</h2>
            <div className="d-flex justify-content-center pt-1 pb-3">
                {
                    isTrial
                        ? <Badge bg="warning" className="text-center">🏆 Épreuve</Badge>
                        : <Badge bg="info" className="text-center">📅 Événements</Badge>
                }
            </div>
            <div className="d-flex justify-content-center flex-md-row flex-column">
                <div className="d-flex flex-column w-100 border rounded mx-md-3 p-3">
                    <h5 className="text-center">⏰ Horaires</h5>
                    <span>
                        <span className="fw-semibold">Début : </span>
                        {new Date(eventData.timeSlot.start).toLocaleString('fr-FR', {
                            dateStyle: 'full',
                            timeStyle: 'short'
                        })}
                    </span>
                    <span>
                        <span className="fw-semibold">Fin : </span>
                        {new Date(eventData.timeSlot.end).toLocaleString('fr-FR', {
                            dateStyle: 'full',
                            timeStyle: 'short'
                        })}
                    </span>
                    <span>
                        <span className="fw-semibold">Durée : </span>
                        {Math.round(
                            (new Date(eventData.timeSlot.end) - new Date(eventData.timeSlot.start))
                            / (1000 * 60)
                        )} minutes
                    </span>
                    <div className="d-flex justify-content-center">
                        <hr style={{width: "12rem"}}/>
                    </div>
                    <h5 className="text-center">📍 Lieu : {eventData.place.name} </h5>
                    {eventData.place.description && (
                        <span className="fst-italic">{eventData.place.description}</span>
                    )}

                    <span className="fw-semibold pt-2">Adresse : </span>
                    <span>{eventData.place.number} {eventData.place.street}</span>
                    <span className="pb-3">{eventData.place.zip} {eventData.place.city}</span>
                    {
                        eventData.place.parking
                            ? <span>🅿️ Parking disponible</span>
                            : <span>❌ Parking indisponible</span>

                    }

                </div>
                <div className="vr d-none d-md-inline"></div>
                <div className="d-flex flex-column w-100 mx-md-3 pt-4 ps-3">
                    {eventData.competitionName && (
                        <span className="pb-2">
                            <span className="fw-semibold">🏅 Compétition : </span>
                            {eventData.competitionName}
                        </span>
                    )}
                    <span>
                        <span className="fw-semibold">📋 Description : </span>
                        {eventData.description || 'Aucune description disponible'}
                    </span>
                    {eventData.rankings && eventData.rankings.length > 0 && (
                        <div className="border rounded p-3 m-3">
                            <h5 className="text-center">🏊‍♀️ Participants</h5>
                            <div className="d-flex flexrow gap-2">
                                {eventData.rankings.some(r => r.participantType === 'TEAM') && (
                                    <div className="w-100">
                                        <div className="text-center fw-semibold">Équipes</div>
                                        <Accordion>
                                            {eventData.rankings
                                                .filter(r => r.participantType === 'TEAM')
                                                .map((ranking, index) => (
                                                    <Accordion.Item eventKey={index}
                                                                    key={`team-${ranking.participantId}-${index}`}>
                                                        <Accordion.Header>
                                                            <div className="w-100 d-flex justify-content-between me-4">
                                                                <RankingFormat rank={ranking.rank}/>
                                                                <span>{ranking.participantName}</span>
                                                                {ranking.result
                                                                    ? <span>{ranking.result}</span>
                                                                    : <span>Forfait</span>}
                                                            </div>
                                                        </Accordion.Header>
                                                        <Accordion.Body className="p-0">
                                                            <ListGroup variant={"flush"}>
                                                                {eventData.teamParticipants
                                                                    .find((team) => team.name === ranking.participantName)
                                                                    .members.map((athelete, index) => (
                                                                        <ListGroupItem onClick={() => navigate(`/public/athlete-trials/${athelete.id}`)}
                                                                            key={`team-${ranking.participantId}-${index}-${athelete.id}`}
                                                                            style={{"cursor": "pointer"}}>
                                                                            {athelete.fullName}
                                                                        </ListGroupItem>
                                                                    ))
                                                                }
                                                            </ListGroup>
                                                        </Accordion.Body>
                                                    </Accordion.Item>
                                                ))}
                                        </Accordion>
                                    </div>
                                )}
                                {eventData.rankings.some(r => r.participantType === 'ATHLETE') && (
                                    <div className="w-100">
                                        <div className="text-center fw-semibold">Athlètes</div>
                                        <ListGroup as="ol">
                                            {eventData.rankings
                                                .filter(r => r.participantType === 'ATHLETE')
                                                .map((ranking, index) => (
                                                    <ListGroup.Item as="li"
                                                                    key={`athlete-${ranking.participantId}-${index}`}
                                                                    onClick={() => navigate(`/public/athlete-trials/${ranking.participantId}`)}
                                                                    style={{"cursor": "pointer"}} >
                                                        <div className="d-flex justify-content-between">
                                                            <RankingFormat rank={ranking.rank}/>
                                                            <span>{ranking.participantName}</span>
                                                            {ranking.result
                                                                ? <span>{ranking.result}</span>
                                                                : <span>Forfait</span>}
                                                        </div>
                                                    </ListGroup.Item>
                                                ))}
                                        </ListGroup>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                    {(eventData.rankings.length === 0 && (eventData.soloParticipants || eventData.teamParticipants) ) && (
                        <div className="border rounded p-3 m-3">
                            <h5 className="text-center">‍🏊‍♀️ Participants</h5>
                            <div className="d-flex flex-row gap-2">
                                {(eventData.teamParticipants && eventData.teamParticipants.length >0) && (
                                    <div className="w-100">
                                        <div className="text-center fw-semibold">Équipes</div>
                                        <Accordion>
                                            {eventData.teamParticipants
                                                .map((team, index) => (
                                                    <Accordion.Item eventKey={index}
                                                                    key={`team-${team.id}-${index}`}>
                                                        <Accordion.Header>
                                                            <div className="w-100 d-flex justify-content-between me-4">
                                                                <span>{team.name}</span>
                                                            </div>
                                                        </Accordion.Header>
                                                        <Accordion.Body className="p-0">
                                                            <ListGroup variant={"flush"}>
                                                                {team.members
                                                                    .map((athelete, index) => (
                                                                        <ListGroupItem
                                                                            key={`team-${team.id}-${index}-${athelete.id}`}
                                                                            onClick={() => navigate(`/public/athlete-trials/${athelete.id}`)}
                                                                            style={{"cursor": "pointer"}}
                                                                        >
                                                                            {athelete.fullName}
                                                                        </ListGroupItem>
                                                                    ))
                                                                }
                                                            </ListGroup>
                                                        </Accordion.Body>
                                                    </Accordion.Item>
                                                ))}
                                        </Accordion>
                                    </div>
                                )}
                                {(eventData.soloParticipants && eventData.soloParticipants.length >0) && (
                                    <div className="w-100">
                                        <div className="text-center fw-semibold">Athlètes</div>
                                        <ListGroup as="ol">
                                            {eventData.soloParticipants
                                                .map((participant, index) => (
                                                    <ListGroup.Item as="li"
                                                                    key={`athlete-${participant.id}-${index}`}
                                                                    onClick={() => navigate(`/public/athlete-trials/${participant.id}`)}
                                                                    style={{"cursor": "pointer"}}>
                                                        <div className="d-flex justify-content-between">
                                                            <span>{participant.fullName}</span>
                                                        </div>
                                                    </ListGroup.Item>
                                                ))}
                                        </ListGroup>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                </div>
            </div>
            <Button onClick={() => navigate(-1)}
                    variant="outline-secondary" className="m-3">
                ← Retour
            </Button>
        </>
    );
};

export default TrialsAndEventsDetails;
