import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { Container, Row, Col, Card, Button, Spinner, Alert, Badge, Modal, ButtonGroup } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import participantService from '../../services/participantService';

// Définition de la forme des props de participant
const participantShape = PropTypes.shape({
    id: PropTypes.number,
    name: PropTypes.string,
    type: PropTypes.string,
    country: PropTypes.string,
    forfeit: PropTypes.bool,
});

// Sous-composant pour l'état de chargement
const LoadingState = () => (
    <Container className="py-4 text-center">
        <Spinner animation="border" role="status">
            <span className="visually-hidden">Chargement...</span>
        </Spinner>
    </Container>
);

// Sous-composant pour l'état d'erreur
const ErrorState = ({ error, onDismiss, onBack }) => (
    <Container className="py-4">
        <Alert variant="danger" dismissible onClose={onDismiss}>
            {error}
        </Alert>
        <Button variant="outline-secondary" onClick={onBack}>
            ← Retour
        </Button>
    </Container>
);

// Sous-composant pour l'état "non trouvé"
const NotFoundState = ({ onBack }) => (
    <Container className="py-4">
        <Alert variant="warning">Épreuve non trouvée</Alert>
        <Button variant="outline-secondary" onClick={onBack}>
            ← Retour
        </Button>
    </Container>
);

// Sous-composant pour une carte de participant
const ParticipantCard = ({ participant, source, onDragStart, onDragEnd, actionLoading, onAction, actionLabel, actionVariant }) => (
    <Card 
        key={`${source}-${participant.type}-${participant.id}`} 
        className="shadow-sm"
        draggable
        onDragStart={(e) => onDragStart(e, participant, source)}
        onDragEnd={onDragEnd}
        style={{ cursor: 'grab' }}
    >
        <Card.Body className="py-2">
            <div className="d-flex justify-content-between align-items-center">
                <div className="d-flex align-items-center">
                    <span className="me-2 text-muted">⠿</span>
                    <div>
                        <div className="fw-semibold">{participant.name}</div>
                        {participant.country && (
                            <small className="text-muted">🌍 {participant.country}</small>
                        )}
                    </div>
                </div>
                <Button
                    variant={actionVariant}
                    size="sm"
                    disabled={actionLoading}
                    onClick={() => onAction(participant)}
                >
                    {actionLabel}
                </Button>
            </div>
        </Card.Body>
    </Card>
);

// Sous-composant pour une carte de participant inscrit
const RegisteredParticipantCard = ({ participant, onDragStart, onDragEnd, actionLoading, onRemove, onForfeit, onUnforfeit }) => (
    <Card 
        className={`shadow-sm ${participant.forfeit ? 'border-danger' : ''}`}
        draggable={!participant.forfeit}
        onDragStart={(e) => !participant.forfeit && onDragStart(e, participant, 'registered')}
        onDragEnd={onDragEnd}
        style={{ cursor: participant.forfeit ? 'default' : 'grab' }}
    >
        <Card.Body className="py-2">
            <div className="d-flex justify-content-between align-items-center">
                <div className="d-flex align-items-center">
                    {!participant.forfeit && <span className="me-2 text-muted">⠿</span>}
                    <div>
                        <div className="fw-semibold">
                            {participant.name}
                            {participant.forfeit && <Badge bg="danger" className="ms-2">Forfait</Badge>}
                        </div>
                        {participant.country && (
                            <small className="text-muted">🌍 {participant.country}</small>
                        )}
                    </div>
                </div>
                <div className="d-flex gap-1">
                    {!participant.forfeit ? (
                        <>
                            <Button variant="outline-secondary" size="sm" disabled={actionLoading} onClick={() => onRemove(participant)} title="Retirer">✕</Button>
                            <Button variant="outline-danger" size="sm" disabled={actionLoading} onClick={() => onForfeit(participant)}>Forfait</Button>
                        </>
                    ) : (
                        <Button variant="outline-success" size="sm" disabled={actionLoading} onClick={() => onUnforfeit(participant)}>Annuler forfait</Button>
                    )}
                </div>
            </div>
        </Card.Body>
    </Card>
);

// Sous-composant pour le modal d'ajout
const AddParticipantModal = ({ show, target, trialName, actionLoading, onHide, onConfirm }) => (
    <Modal show={show} onHide={onHide} centered>
        <Modal.Header closeButton>
            <Modal.Title>📋 Détails - {target?.type === 'TEAM' ? 'Équipe' : 'Athlète'}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
            <div className="mb-3"><strong>Nom :</strong> {target?.name}</div>
            {target?.country && <div className="mb-3"><strong>Pays :</strong> 🌍 {target?.country}</div>}
            <div className="mb-3">
                <strong>Type :</strong>{' '}
                <Badge bg={target?.type === 'TEAM' ? 'info' : 'success'}>
                    {target?.type === 'TEAM' ? '👥 Équipe' : '🏃 Athlète'}
                </Badge>
            </div>
            <hr />
            <p className="text-muted">
                Voulez-vous inscrire {target?.type === 'TEAM' ? 'cette équipe' : 'cet athlète'} à l'épreuve <strong>{trialName}</strong> ?
            </p>
        </Modal.Body>
        <Modal.Footer>
            <Button variant="secondary" onClick={onHide} disabled={actionLoading}>Annuler</Button>
            <Button variant="secondary" onClick={onConfirm} disabled={actionLoading}>
                {actionLoading ? <><Spinner animation="border" size="sm" className="me-2" />Inscription...</> : 'Inscrire le participant'}
            </Button>
        </Modal.Footer>
    </Modal>
);

// Sous-composant pour le modal de forfait
const ForfeitModal = ({ show, target, actionLoading, onHide, onConfirm }) => (
    <Modal show={show} onHide={onHide} centered>
        <Modal.Header closeButton>
            <Modal.Title>⚠️ Confirmer le forfait</Modal.Title>
        </Modal.Header>
        <Modal.Body>
            <p>Êtes-vous sûr de vouloir déclarer forfait pour <strong>{target?.name}</strong> ?</p>
            <p className="text-muted small">
                {target?.type === 'TEAM' ? 'L\'équipe' : 'Le sportif'} sera marqué(e) comme forfait pour cette épreuve.
            </p>
        </Modal.Body>
        <Modal.Footer>
            <Button variant="secondary" onClick={onHide} disabled={actionLoading}>Annuler</Button>
            <Button variant="danger" onClick={onConfirm} disabled={actionLoading}>
                {actionLoading ? <><Spinner animation="border" size="sm" className="me-2" />Traitement...</> : 'Confirmer le forfait'}
            </Button>
        </Modal.Footer>
    </Modal>
);

// Sous-composant pour le modal de suppression
const RemoveModal = ({ show, target, actionLoading, onHide, onConfirm }) => (
    <Modal show={show} onHide={onHide} centered>
        <Modal.Header closeButton>
            <Modal.Title>🗑️ Retirer le participant</Modal.Title>
        </Modal.Header>
        <Modal.Body>
            <p>Êtes-vous sûr de vouloir retirer <strong>{target?.name}</strong> de cette épreuve ?</p>
            <p className="text-muted small">
                {target?.type === 'TEAM' ? 'L\'équipe' : 'L\'athlète'} sera désinscrit(e) de l'épreuve et pourra être réinscrit(e) ultérieurement.
            </p>
        </Modal.Body>
        <Modal.Footer>
            <Button variant="secondary" onClick={onHide} disabled={actionLoading}>Annuler</Button>
            <Button variant="warning" onClick={onConfirm} disabled={actionLoading}>
                {actionLoading ? <><Spinner animation="border" size="sm" className="me-2" />Traitement...</> : 'Retirer le participant'}
            </Button>
        </Modal.Footer>
    </Modal>
);

// PropTypes definitions
ErrorState.propTypes = {
    error: PropTypes.string,
    onDismiss: PropTypes.func.isRequired,
    onBack: PropTypes.func.isRequired,
};

NotFoundState.propTypes = {
    onBack: PropTypes.func.isRequired,
};

ParticipantCard.propTypes = {
    participant: participantShape.isRequired,
    source: PropTypes.string.isRequired,
    onDragStart: PropTypes.func.isRequired,
    onDragEnd: PropTypes.func.isRequired,
    actionLoading: PropTypes.bool,
    onAction: PropTypes.func.isRequired,
    actionLabel: PropTypes.string.isRequired,
    actionVariant: PropTypes.string.isRequired,
};

RegisteredParticipantCard.propTypes = {
    participant: participantShape.isRequired,
    onDragStart: PropTypes.func.isRequired,
    onDragEnd: PropTypes.func.isRequired,
    actionLoading: PropTypes.bool,
    onRemove: PropTypes.func.isRequired,
    onForfeit: PropTypes.func.isRequired,
    onUnforfeit: PropTypes.func.isRequired,
};

AddParticipantModal.propTypes = {
    show: PropTypes.bool.isRequired,
    target: participantShape,
    trialName: PropTypes.string,
    actionLoading: PropTypes.bool,
    onHide: PropTypes.func.isRequired,
    onConfirm: PropTypes.func.isRequired,
};

ForfeitModal.propTypes = {
    show: PropTypes.bool.isRequired,
    target: participantShape,
    actionLoading: PropTypes.bool,
    onHide: PropTypes.func.isRequired,
    onConfirm: PropTypes.func.isRequired,
};

RemoveModal.propTypes = {
    show: PropTypes.bool.isRequired,
    target: participantShape,
    actionLoading: PropTypes.bool,
    onHide: PropTypes.func.isRequired,
    onConfirm: PropTypes.func.isRequired,
};

const ManageParticipants = () => {
    const { trialId } = useParams();
    const navigate = useNavigate();
    
    const [trialData, setTrialData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [actionLoading, setActionLoading] = useState(false);
    
    // Mode d'affichage (équipe ou solo)
    const [viewMode, setViewMode] = useState('solo'); // 'solo' ou 'team'
    
    // Modal de confirmation forfait
    const [showForfeitModal, setShowForfeitModal] = useState(false);
    const [forfeitTarget, setForfeitTarget] = useState(null);
    
    // Modal de confirmation suppression
    const [showRemoveModal, setShowRemoveModal] = useState(false);
    const [removeTarget, setRemoveTarget] = useState(null);
    
    // Modal de détails pour l'inscription
    const [showAddModal, setShowAddModal] = useState(false);
    const [addTarget, setAddTarget] = useState(null);
    
    // Drag and drop
    const [draggedParticipant, setDraggedParticipant] = useState(null);
    const [dropZoneActive, setDropZoneActive] = useState(null); // 'potential' ou 'registered'

    useEffect(() => {
        fetchTrialParticipants();
    }, [trialId]);

    const fetchTrialParticipants = async () => {
        try {
            setLoading(true);
            const data = await participantService.getTrialParticipantsFull(trialId);
            setTrialData(data);
            // Définir le mode d'affichage selon le type d'épreuve
            setViewMode(data.teamTrial ? 'team' : 'solo');
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    // Drag and drop handlers
    const handleDragStart = (e, participant, source) => {
        setDraggedParticipant({ ...participant, source });
        e.dataTransfer.effectAllowed = 'move';
    };

    const handleDragOver = (e, zone) => {
        e.preventDefault();
        e.stopPropagation();
        e.dataTransfer.dropEffect = 'move';
        if (dropZoneActive !== zone) {
            setDropZoneActive(zone);
        }
    };

    const handleDragLeave = (e) => {
        // Ne pas désactiver si on entre dans un élément enfant
        const rect = e.currentTarget.getBoundingClientRect();
        const x = e.clientX;
        const y = e.clientY;
        
        if (x < rect.left || x > rect.right || y < rect.top || y > rect.bottom) {
            setDropZoneActive(null);
        }
    };
    
    const handleDragEnd = () => {
        setDropZoneActive(null);
        setDraggedParticipant(null);
    };

    const handleDrop = async (e, targetZone) => {
        e.preventDefault();
        setDropZoneActive(null);
        
        if (!draggedParticipant) return;
        
        // Vérifier si le drag est valide
        if (draggedParticipant.source === 'potential' && targetZone === 'registered') {
            // Ajouter un participant
            await handleAddParticipant(draggedParticipant);
        } else if (draggedParticipant.source === 'registered' && targetZone === 'potential') {
            // Retirer un participant
            openRemoveModal(draggedParticipant);
        }
        
        setDraggedParticipant(null);
    };

    const handleAddParticipant = async (participant) => {
        try {
            setActionLoading(true);
            if (participant.type === 'ATHLETE') {
                await participantService.addAthlete(trialId, participant.id);
            } else {
                await participantService.addTeam(trialId, participant.id);
            }
            await fetchTrialParticipants();
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    const openAddModal = (participant) => {
        setAddTarget(participant);
        setShowAddModal(true);
    };

    const handleConfirmAdd = async () => {
        if (!addTarget) return;
        await handleAddParticipant(addTarget);
        setShowAddModal(false);
        setAddTarget(null);
    };

    const openForfeitModal = (participant) => {
        setForfeitTarget(participant);
        setShowForfeitModal(true);
    };

    const handleConfirmForfeit = async () => {
        if (!forfeitTarget) return;
        
        try {
            setActionLoading(true);
            if (forfeitTarget.type === 'ATHLETE') {
                await participantService.forfeitAthlete(trialId, forfeitTarget.id);
            } else {
                await participantService.forfeitTeam(trialId, forfeitTarget.id);
            }
            setShowForfeitModal(false);
            setForfeitTarget(null);
            await fetchTrialParticipants();
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    const handleUnforfeit = async (participant) => {
        try {
            setActionLoading(true);
            if (participant.type === 'ATHLETE') {
                await participantService.unforfeitAthlete(trialId, participant.id);
            } else {
                await participantService.unforfeitTeam(trialId, participant.id);
            }
            await fetchTrialParticipants();
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };
    
    const openRemoveModal = (participant) => {
        setRemoveTarget(participant);
        setShowRemoveModal(true);
    };
    
    const handleConfirmRemove = async () => {
        if (!removeTarget) return;
        
        try {
            setActionLoading(true);
            if (removeTarget.type === 'ATHLETE') {
                await participantService.removeAthlete(trialId, removeTarget.id);
            } else {
                await participantService.removeTeam(trialId, removeTarget.id);
            }
            setShowRemoveModal(false);
            setRemoveTarget(null);
            await fetchTrialParticipants();
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    // Obtenir les participants potentiels selon le mode
    const getPotentialParticipants = () => {
        if (!trialData) return [];
        return viewMode === 'team' ? trialData.potentialTeams : trialData.potentialAthletes;
    };

    if (loading) {
        return <LoadingState />;
    }

    if (error) {
        return <ErrorState error={error} onDismiss={() => setError(null)} onBack={() => navigate(-1)} />;
    }

    if (!trialData) {
        return <NotFoundState onBack={() => navigate(-1)} />;
    }

    const potentialParticipants = getPotentialParticipants();

    return (
        <Container className="py-4">
            <h2 className="text-center mb-2">Ajouter un participant</h2>
            <h4 className="text-center text-muted mb-4">{trialData.trialName}</h4>
            
            {/* Sélecteur de mode (équipe/solo) - seulement si pas encore de participants */}
            {trialData.canChangeType && (
                <div className="d-flex justify-content-center mb-4">
                    <ButtonGroup>
                        <Button 
                            variant={viewMode === 'solo' ? 'secondary' : 'outline-secondary'}
                            onClick={() => setViewMode('solo')}
                        >
                            🏃 Solo
                        </Button>
                        <Button 
                            variant={viewMode === 'team' ? 'info' : 'outline-info'}
                            onClick={() => setViewMode('team')}
                        >
                            👥 Équipe
                        </Button>
                    </ButtonGroup>
                </div>
            )}
            
            <div className="d-flex justify-content-center mb-4">
                <Badge bg={viewMode === 'team' ? 'info' : 'success'} className="px-3 py-2">
                    {viewMode === 'team' ? '👥 Épreuve en équipe' : '🏃 Épreuve individuelle'}
                    {!trialData.canChangeType && ' (verrouillé)'}
                </Badge>
            </div>

            <Row className="g-4">
                {/* Colonne des participants potentiels */}
                <Col md={5}>
                    <Card 
                        className={`h-100 ${dropZoneActive === 'potential' ? 'border-warning border-2' : ''}`}
                        onDragOver={(e) => handleDragOver(e, 'potential')}
                        onDragLeave={handleDragLeave}
                        onDrop={(e) => handleDrop(e, 'potential')}
                    >
                        <Card.Header as="h5" className="text-center bg-light position-relative">
                            {viewMode === 'team' ? 'Équipes potentielles' : 'Participants potentiels'}
                            {dropZoneActive === 'potential' && (
                                <div className="position-absolute top-100 start-50 translate-middle-x mt-2" style={{ zIndex: 10 }}>
                                    <Badge bg="warning" className="px-3 py-2">
                                        🗑️ Relâchez pour retirer
                                    </Badge>
                                </div>
                            )}
                        </Card.Header>
                        <Card.Body className="overflow-auto" style={{ maxHeight: '60vh' }}>
                            {potentialParticipants?.length === 0 ? (
                                <p className="text-center text-muted">
                                    Aucun {viewMode === 'team' ? 'équipe' : 'athlète'} disponible
                                </p>
                            ) : (
                                <div className="d-flex flex-column gap-2">
                                    {potentialParticipants?.map((participant) => (
                                        <ParticipantCard
                                            key={`potential-${participant.type}-${participant.id}`}
                                            participant={participant}
                                            source="potential"
                                            onDragStart={handleDragStart}
                                            onDragEnd={handleDragEnd}
                                            actionLoading={actionLoading}
                                            onAction={openAddModal}
                                            actionLabel="+ Ajouter"
                                            actionVariant="outline-secondary"
                                        />
                                    ))}
                                </div>
                            )}
                        </Card.Body>
                    </Card>
                </Col>

                {/* Flèche centrale */}
                <Col md={2} className="d-flex align-items-center justify-content-center">
                    <div className="text-center">
                        <div className="d-flex flex-column align-items-center gap-2">
                            <Badge bg="secondary" className="px-3 py-2">
                                ↔️ Glisser-déposer
                            </Badge>
                        </div>
                        <div className="mt-2 text-muted small">
                            ou utilisez les boutons
                        </div>
                    </div>
                </Col>

                {/* Colonne des participants inscrits */}
                <Col md={5}>
                    <Card 
                        className={`h-100 ${dropZoneActive === 'registered' ? 'border-success border-2' : ''}`}
                        onDragOver={(e) => handleDragOver(e, 'registered')}
                        onDragLeave={handleDragLeave}
                        onDrop={(e) => handleDrop(e, 'registered')}
                    >
                        <Card.Header as="h5" className="text-center bg-light position-relative">
                            Participants inscrits
                            {dropZoneActive === 'registered' && (
                                <div className="position-absolute top-100 start-50 translate-middle-x mt-2" style={{ zIndex: 10 }}>
                                    <Badge bg="success" className="px-3 py-2">
                                        ✅ Relâchez pour inscrire
                                    </Badge>
                                </div>
                            )}
                        </Card.Header>
                        <Card.Body className="overflow-auto" style={{ maxHeight: '60vh' }}>
                            {trialData.participants?.length === 0 ? (
                                <p className="text-center text-muted">
                                    Aucun participant inscrit
                                </p>
                            ) : (
                                <div className="d-flex flex-column gap-2">
                                    {trialData.participants?.map((participant) => (
                                        <RegisteredParticipantCard
                                            key={`participant-${participant.type}-${participant.id}`}
                                            participant={participant}
                                            onDragStart={handleDragStart}
                                            onDragEnd={handleDragEnd}
                                            actionLoading={actionLoading}
                                            onRemove={openRemoveModal}
                                            onForfeit={openForfeitModal}
                                            onUnforfeit={handleUnforfeit}
                                        />
                                    ))}
                                </div>
                            )}
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            <Button 
                variant="outline-secondary" 
                className="mt-4"
                onClick={() => navigate('/commissaire/trials')}
            >
                ← Retour aux épreuves
            </Button>

            <AddParticipantModal
                show={showAddModal}
                target={addTarget}
                trialName={trialData?.trialName}
                actionLoading={actionLoading}
                onHide={() => setShowAddModal(false)}
                onConfirm={handleConfirmAdd}
            />

            <ForfeitModal
                show={showForfeitModal}
                target={forfeitTarget}
                actionLoading={actionLoading}
                onHide={() => setShowForfeitModal(false)}
                onConfirm={handleConfirmForfeit}
            />

            <RemoveModal
                show={showRemoveModal}
                target={removeTarget}
                actionLoading={actionLoading}
                onHide={() => setShowRemoveModal(false)}
                onConfirm={handleConfirmRemove}
            />
        </Container>
    );
};

export default ManageParticipants;
