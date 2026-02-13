import {useParams} from "react-router-dom";
import React, {useEffect, useState} from "react";
import participantService from "../services/participantService.jsx";
import {isPastEvent} from "../utils/dateFormatter.js";
import TrialsAndEventsCard from "./TrialsAndEventsCard.jsx";
import {eventService} from "../services/eventService.jsx";

const TrialsByAthlete = () => {
    const {athleteId} = useParams();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [trials, setTrials] = useState([])

    useEffect(() => {
        fetchTrials();
    }, [athleteId]);

    const fetchTrials = async () => {
        try {
            setLoading(true);

            const data = await participantService.getTrialsByAthleteId(athleteId);
            const trialsTmp = data.soloTrials.concat(data.teamTrials);
            const detailedTrials = await Promise.all(
                trialsTmp.map(async (event) => {
                    try {
                        return await eventService.getById(event.id);
                    } catch (error) {
                        console.warn(`Failed to load details for event ${event.id}:`, error);
                    }
                })
            );

            setTrials(detailedTrials);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const pastTrials = () => trials.filter(trial => isPastEvent(trial))

    const futurTrials = () => trials.filter(trial => !isPastEvent(trial))


    if (loading) return <div className="loading">Chargement des épreuves...</div>;
    if (error) return <div className="error">Erreur: {error}</div>;
    if (!trials) return <div className="error">Aucune donnée disponible</div>;

    return (
        <>
            <div className="d-flex justify-content-center flex-md-row flex-column">
                <div className="d-flex flex-column w-100 mx-md-3 p-3">
                    <TrialsAndEventsCard trials={futurTrials()} events={[]} title={"A venir"}/>
                </div>
                <div className="vr d-none d-md-inline"></div>
                <div className="d-flex flex-column w-100 mx-md-3 p-3">
                    <TrialsAndEventsCard trials={pastTrials()} events={[]} title={"Passés"}/>
                </div>
            </div>
        </>
    )
};
export default TrialsByAthlete;
