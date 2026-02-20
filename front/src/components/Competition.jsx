import React, {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import {getCompetitionById, subscribeToCompetition, getObservers} from "../services/competitionService.jsx";
import {eventService} from "../services/eventService.jsx";
import TrialsAndEventsCard from "./TrialsAndEventsCard.jsx";
import {isPastEvent} from "../utils/dateFormatter.js";
import {Button} from "react-bootstrap";
import {useAuth} from "../contexts/AuthContext.jsx";

const Competition = () => {
    const {id: idChampionship, idComp: idCompetition} = useParams();
    const [events, setEvents] = useState([]);
    const [trials, setTrials] = useState([]);
    const [competition, setCompetition] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [subscribing, setSubscribing] = useState(false);
    const [subscribeSuccess, setSubscribeSuccess] = useState(false);
    const navigate = useNavigate();
    const { user, isAuthenticated } = useAuth();


    const isSubscribed = async () =>  {
        const observers = await getObservers(user.id);

        if(observers.length > 0) {
            for (const observer of observers) {
                if(observer.competitionId === competition.id) {
                    setSubscribeSuccess(true);
                }
            }
        }
    }
    isSubscribed()

    useEffect(() => {
        fetchAllData();
    }, [idChampionship, idCompetition]);

    const fetchAllData = async () => {
        try {
            setLoading(true);
            const [eventsData, trialsData, competitionData] = await Promise.all([
                eventService.getJustEventsByCompetition(idChampionship, idCompetition),
                eventService.getTrialsByCompetition(idChampionship, idCompetition),
                getCompetitionById(idChampionship, idCompetition)
            ]);

            const detailedEvents = await Promise.all(
                eventsData.map(async (event) => {
                    try {
                        return await eventService.getEventById(event.id);
                    } catch (error) {
                        console.warn(`Failed to load details for event ${event.id}:`, error);
                        return {...event, _isTrial: false};
                    }
                })
            ).then(events => events.map(e => ({...e, _isTrial: false})));

            const detailedTrials = await Promise.all(
                trialsData.map(async (trial) => {
                    try {
                        const response = await fetch(`http://localhost:8084/public/trials/${trial.id}`);
                        if (response.ok) {
                            const detailed = await response.json();
                            return {...detailed, idEvent: trial.idEvent, _isTrial: true};
                        }
                        return {...trial, _isTrial: true};
                    } catch {
                        return {...trial, _isTrial: true};
                    }
                })
            );

            setEvents(detailedEvents);
            setTrials(detailedTrials);
            setCompetition(competitionData)
        } catch (err) {
            console.error('Fetch error:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const pastTrials = () => trials.filter(trial => isPastEvent(trial))


    const pastEvents = () => events.filter(event => isPastEvent(event))


    const futurTrials = () => trials.filter(trial => !isPastEvent(trial))


    const futurEvents = () => events.filter(event => !isPastEvent(event))

    const handleSubscribe = async () => {

        try {
            setSubscribing(true);
            // Utiliser user.id qui est l'ID numérique depuis le JWT
            await subscribeToCompetition(idChampionship, idCompetition, user.id);
            setSubscribeSuccess(true);
        } catch (err) {
            console.error('Erreur lors de l\'abonnement:', err);
            alert("Erreur lors de l'abonnement à la compétition");
        } finally {
            setSubscribing(false);
            console.log(`user ${user.id} subscribed to competition ${idCompetition}`);
        }
    };

    if (loading) return <div className="loading">Chargement des événements...</div>;
    if (error) return <div className="error">Erreur: {error}</div>;
    return (
        <>
            <h2 className="text-center">{competition.name}</h2>
            <h5 className="text-center text-body-tertiary">{competition.description}</h5>

            {isAuthenticated() && (
                <div className="d-flex justify-content-center mb-3">
                    <Button
                        onClick={handleSubscribe}
                        disabled={subscribeSuccess}
                        variant={subscribeSuccess ? "success" : "secondary"}
                        className="m-2"
                    >
                        {subscribing ? "Abonnement en cours..." : subscribeSuccess ? "✓ Abonné!" : "📌 S'abonner à cette compétition"}
                    </Button>
                </div>
            )}

            <div className="d-flex justify-content-center flex-md-row flex-column">
                <div className="d-flex flex-column w-100 mx-md-3 p-3">
                    <TrialsAndEventsCard trials={futurTrials()} events={futurEvents()} title={"A venir"}/>
                </div>
                <div className="vr d-none d-md-inline"></div>
                <div className="d-flex flex-column w-100 mx-md-3 p-3" id="sse">
                    <TrialsAndEventsCard trials={pastTrials()} events={pastEvents()} title={"Passés"}/>
                </div>
            </div>
            <Button onClick={() => navigate(-1)}
                    variant="outline-secondary" className="m-2">
                ← Retour
            </Button>
        </>

    );
};


export default Competition;
