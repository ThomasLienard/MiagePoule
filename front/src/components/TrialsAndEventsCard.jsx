import {Card} from "react-bootstrap";
import {useNavigate} from "react-router-dom";
import {formatDate} from "../utils/dateFormatter.js";
import React from "react";

const TrialsAndEventsCard = ({trials, events, title}) => {
    const navigate = useNavigate();

    const handleEventClick = (id) => {
        navigate(`/public/events/${id}`);
    };

    const handleTrialClick = (id) => {
        navigate(`/public/trials/${id}`);
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
                                            {trial.rankings?.length > 0 && (
                                                <div
                                                    className="text-success text-end">Résultats disponibles !</div>
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
