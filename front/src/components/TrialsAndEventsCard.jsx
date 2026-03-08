import {Card, Button} from "react-bootstrap";
import {useNavigate} from "react-router-dom";
import {formatDate} from "../utils/dateFormatter.js";
import React from "react";
import RankingFormat from "./common/RankingFormat.jsx";

const TrialsAndEventsCard = ({trials, events, title, showForfeitButton, onForfeitClick, rankingMap}) => {
    const navigate = useNavigate();

    const handleEventClick = (id) => {
        navigate(`/public/events/${id}`);
    };

    const handleTrialClick = (id) => {
        navigate(`/public/trials/${id}`);
    };

    const handleForfeitClick = (e, trial) => {
        e.stopPropagation(); // Empêcher la navigation vers les détails
        if (onForfeitClick) {
            onForfeitClick(trial);
        }
    };

    return (
        <Card className="overflow-y-auto" style={{"height": "70vh"}}>
            <Card.Header>
                <Card.Title className="text-center">{title}</Card.Title>
            </Card.Header>
            <Card.Body>
                {trials.length > 0 && (
                    <>
                        <div className="d-flex align-items-center flex-column">
                            <span>🏆 Compétition</span>
                            <hr style={{width: "21rem"}}/>
                        </div>
                        {trials
                            .map(trial => (
                                <Card
                                    key={`trial-${trial.id}`}
                                    className='mb-1'
                                    onClick={() => handleTrialClick(trial.id)}
                                    style={{cursor: "pointer"}}
                                >
                                    <Card.Body className="text-center">
                                        <Card.Title>{trial.name}</Card.Title>
                                        <Card.Subtitle></Card.Subtitle>
                                        <div>
                                            {trial.timeSlot?.start && (
                                                <div
                                                    className="text-body-tertiary">{formatDate(trial.timeSlot.start, trial.timeSlot.end)}</div>
                                            )}
                                            {rankingMap?.get(trial.id) && (
                                                <span>
                                                    <RankingFormat rank={rankingMap.get(trial.id).rank}/>
                                                    {rankingMap.get(trial.id).result
                                                        ? <span>{rankingMap.get(trial.id).result} </span>
                                                        : <span>Forfait</span>}
                                                </span>
                                            )}
                                            {(trial.rankings?.length > 0 && (!rankingMap || !rankingMap.get(trial.id))) && (
                                                <div
                                                    className="text-success text-end">Résultats disponibles !</div>
                                            )}
                                            {showForfeitButton && (
                                                <div className="mt-2">
                                                    {trial.isForfeit ? (
                                                        <span className="badge bg-warning text-dark">Forfait déclaré</span>
                                                    ) : (
                                                        <Button 
                                                            variant="outline-danger" 
                                                            size="sm"
                                                            onClick={(e) => handleForfeitClick(e, trial)}
                                                        >
                                                            Déclarer forfait
                                                        </Button>
                                                    )}
                                                </div>
                                            )}
                                        </div>
                                    </Card.Body>
                                </Card>
                            ))}
                    </>
                )}
                {trials.length > 0 && events.length > 0 && (
                    <div className="pt-3">
                        <hr/>
                    </div>
                )}
                {events.length > 0 && (
                    <>
                        <div className="d-flex align-items-center flex-column">
                            <span>📅 Extra-compétition</span>
                            <hr style={{width: "21rem"}}/>
                        </div>
                        {events.map(event => (
                            <Card
                                key={`event-${event.id}`}
                                className='mb-1'
                                onClick={() => handleEventClick(event.id)}
                                style={{cursor: "pointer"}}
                            >
                                <Card.Body className="text-center">
                                    <Card.Title>{event.name}</Card.Title>
                                    <Card.Subtitle></Card.Subtitle>
                                    <div>
                                        {event.timeSlot?.start && (
                                            <div
                                                className="text-body-tertiary">{formatDate(event.timeSlot.start, event.timeSlot.end)}</div>
                                        )}
                                        {event.rankings?.length > 0 && (
                                            <div
                                                className="text-success text-end">Résultats disponibles !</div>
                                        )}
                                    </div>
                                </Card.Body>
                            </Card>
                        ))}
                    </>)}
            </Card.Body>
        </Card>
    )
}
export default TrialsAndEventsCard;
