import {useParams} from "react-router-dom";
import React, {useEffect, useState} from "react";
import participantService from "../services/participantService.jsx";
import {isPastEvent} from "../utils/dateFormatter.js";
import TrialsAndEventsCard from "./TrialsAndEventsCard.jsx";
import {eventService} from "../services/eventService.jsx";
import {Modal, Button, Spinner, Alert} from "react-bootstrap";
import {useAuth} from "../contexts/AuthContext.jsx";

const TrialsByAthlete = () => {
    const {athleteId} = useParams();
    const {user} = useAuth();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [trials, setTrials] = useState([]);
    const [showForfeitModal, setShowForfeitModal] = useState(false);
    const [selectedTrial, setSelectedTrial] = useState(null);
    const [actionLoading, setActionLoading] = useState(false);
    const [successMessage, setSuccessMessage] = useState(null);

    // Vérifier si l'utilisateur connecté est celui dont on affiche les épreuves
    const isOwnProfile = user?.id === athleteId;

    useEffect(() => {
        fetchTrials();
    }, [athleteId]);

    const fetchTrials = async () => {
        try {
            setLoading(true);

            const data = await participantService.getTrialsByAthleteId(athleteId);
            const trialsTmp = data.soloTrials.concat(data.teamTrials);
            const detailedTrials = await Promise.all(
                trialsTmp.map(async (trialSummary) => {
                    try {
                        const eventDetails = await eventService.getById(trialSummary.id);
                        // Préserver le champ isForfeit du summary
                        return {
                            ...eventDetails,
                            isForfeit: trialSummary.isForfeit
                        };
                    } catch (error) {
                        console.warn(`Failed to load details for event ${trialSummary.id}:`, error);
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

    const handleOpenForfeitModal = (trial) => {
        setSelectedTrial(trial);
        setShowForfeitModal(true);
    };

    const handleCloseForfeitModal = () => {
        setShowForfeitModal(false);
        setSelectedTrial(null);
    };

    const handleConfirmForfeit = async () => {
        if (!selectedTrial) return;

        try {
            setActionLoading(true);
            await participantService.athleteDeclareWithdrawal(selectedTrial.id);
            
            // Mettre à jour l'état local pour refléter le forfait
            setTrials(prevTrials => 
                prevTrials.map(trial => 
                    trial.id === selectedTrial.id 
                        ? { ...trial, isForfeit: true }
                        : trial
                )
            );
            
            setSuccessMessage('Forfait déclaré avec succès');
            handleCloseForfeitModal();
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    const pastTrials = () => trials.filter(trial => isPastEvent(trial))

    const futurTrials = () => trials.filter(trial => !isPastEvent(trial))


    if (loading) return <div className="loading">Chargement des épreuves...</div>;
    if (error) return <div className="error">Erreur: {error}</div>;
    if (!trials) return <div className="error">Aucune donnée disponible</div>;

    return (
        <>
            {successMessage && (
                <Alert 
                    variant="success" 
                    onClose={() => setSuccessMessage(null)} 
                    dismissible
                    className="mx-3"
                >
                    {successMessage}
                </Alert>
            )}
            
            {error && (
                <Alert 
                    variant="danger" 
                    onClose={() => setError(null)} 
                    dismissible
                    className="mx-3"
                >
                    {error}
                </Alert>
            )}
            
            <div className="d-flex justify-content-center flex-md-row flex-column">
                <div className="d-flex flex-column w-100 mx-md-3 p-3">
                    <TrialsAndEventsCard 
                        trials={futurTrials()} 
                        events={[]} 
                        title={"A venir"} 
                        showForfeitButton={isOwnProfile}
                        onForfeitClick={handleOpenForfeitModal}
                    />
                </div>
                <div className="vr d-none d-md-inline"></div>
                <div className="d-flex flex-column w-100 mx-md-3 p-3">
                    <TrialsAndEventsCard trials={pastTrials()} events={[]} title={"Passés"}/>
                </div>
            </div>

            {/* Modal de confirmation de forfait */}
            <Modal show={showForfeitModal} onHide={handleCloseForfeitModal} centered>
                <Modal.Header closeButton>
                    <Modal.Title>⚠️ Confirmer le forfait</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p>Êtes-vous sûr de vouloir déclarer forfait pour l'épreuve <strong>{selectedTrial?.name}</strong> ?</p>
                    <p className="text-muted small">
                        Cette action marquera votre participation comme forfait pour cette épreuve.
                    </p>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleCloseForfeitModal} disabled={actionLoading}>
                        Annuler
                    </Button>
                    <Button variant="danger" onClick={handleConfirmForfeit} disabled={actionLoading}>
                        {actionLoading ? (
                            <>
                                <Spinner animation="border" size="sm" className="me-2" />
                                Traitement...
                            </>
                        ) : (
                            'Confirmer le forfait'
                        )}
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    )
};
export default TrialsByAthlete;
