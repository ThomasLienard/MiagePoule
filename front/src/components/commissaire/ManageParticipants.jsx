import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Spinner, Alert, Badge, Modal, ButtonGroup } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import participantService from '../../services/participantService';

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
    
    // Modal de détails pour l'inscription
    const [showAddModal, setShowAddModal] = useState(false);
    const [addTarget, setAddTarget] = useState(null);

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

    const openAddModal = (participant) => {
        setAddTarget(participant);
        setShowAddModal(true);
    };

    const handleConfirmAdd = async () => {
        if (!addTarget) return;
        
        try {
            setActionLoading(true);
            if (addTarget.type === 'ATHLETE') {
                await participantService.addAthlete(trialId, addTarget.id);
            } else {
                await participantService.addTeam(trialId, addTarget.id);
            }
            setShowAddModal(false);
            setAddTarget(null);
            await fetchTrialParticipants();
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
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

    // Obtenir les participants potentiels selon le mode
    const getPotentialParticipants = () => {
        if (!trialData) return [];
        return viewMode === 'team' ? trialData.potentialTeams : trialData.potentialAthletes;
    };

    if (loading) {
        return (
            <Container className="py-4 text-center">
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">Chargement...</span>
                </Spinner>
            </Container>
        );
    }

    if (error) {
        return (
            <Container className="py-4">
                <Alert variant="danger" dismissible onClose={() => setError(null)}>
                    {error}
                </Alert>
                <Button variant="outline-secondary" onClick={() => navigate(-1)}>
                    ← Retour
                </Button>
            </Container>
        );
    }

    if (!trialData) {
        return (
            <Container className="py-4">
                <Alert variant="warning">Épreuve non trouvée</Alert>
                <Button variant="outline-secondary" onClick={() => navigate(-1)}>
                    ← Retour
                </Button>
            </Container>
        );
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
                            variant={viewMode === 'solo' ? 'primary' : 'outline-primary'}
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
                    <Card className="h-100">
                        <Card.Header as="h5" className="text-center bg-light">
                            {viewMode === 'team' ? 'Équipes potentielles' : 'Participants potentiels'}
                        </Card.Header>
                        <Card.Body className="overflow-auto" style={{ maxHeight: '60vh' }}>
                            {potentialParticipants?.length === 0 ? (
                                <p className="text-center text-muted">
                                    Aucun {viewMode === 'team' ? 'équipe' : 'athlète'} disponible
                                </p>
                            ) : (
                                <div className="d-flex flex-column gap-2">
                                    {potentialParticipants?.map((participant) => (
                                        <Card key={`potential-${participant.type}-${participant.id}`} className="shadow-sm">
                                            <Card.Body className="py-2">
                                                <div className="d-flex justify-content-between align-items-center">
                                                    <div>
                                                        <div className="fw-semibold">{participant.name}</div>
                                                        {participant.country && (
                                                            <small className="text-muted">
                                                                🌍 {participant.country}
                                                            </small>
                                                        )}
                                                    </div>
                                                    <Button
                                                        variant="outline-primary"
                                                        size="sm"
                                                        disabled={actionLoading}
                                                        onClick={() => openAddModal(participant)}
                                                    >
                                                        Détails
                                                    </Button>
                                                </div>
                                            </Card.Body>
                                        </Card>
                                    ))}
                                </div>
                            )}
                        </Card.Body>
                    </Card>
                </Col>

                {/* Flèche centrale */}
                <Col md={2} className="d-flex align-items-center justify-content-center">
                    <div className="text-center">
                        <Button
                            variant="outline-secondary"
                            size="lg"
                            className="rounded-circle"
                            style={{ width: '60px', height: '60px' }}
                            disabled
                        >
                            &gt;
                        </Button>
                        <div className="mt-2 text-muted small">
                            Cliquez sur "Détails" pour inscrire
                        </div>
                    </div>
                </Col>

                {/* Colonne des participants inscrits */}
                <Col md={5}>
                    <Card className="h-100">
                        <Card.Header as="h5" className="text-center bg-light">
                            Participants
                        </Card.Header>
                        <Card.Body className="overflow-auto" style={{ maxHeight: '60vh' }}>
                            {trialData.participants?.length === 0 ? (
                                <p className="text-center text-muted">
                                    Aucun participant inscrit
                                </p>
                            ) : (
                                <div className="d-flex flex-column gap-2">
                                    {trialData.participants?.map((participant) => (
                                        <Card 
                                            key={`participant-${participant.type}-${participant.id}`} 
                                            className={`shadow-sm ${participant.forfeit ? 'border-danger' : ''}`}
                                        >
                                            <Card.Body className="py-2">
                                                <div className="d-flex justify-content-between align-items-center">
                                                    <div>
                                                        <div className="fw-semibold">
                                                            {participant.name}
                                                            {participant.forfeit && (
                                                                <Badge bg="danger" className="ms-2">
                                                                    Forfait
                                                                </Badge>
                                                            )}
                                                        </div>
                                                        {participant.country && (
                                                            <small className="text-muted">
                                                                🌍 {participant.country}
                                                            </small>
                                                        )}
                                                        {participant.result && (
                                                            <div>
                                                                <small className="text-success">
                                                                    ⏱️ {participant.result}
                                                                </small>
                                                            </div>
                                                        )}
                                                    </div>
                                                    {!participant.forfeit && (
                                                        <Button
                                                            variant="outline-danger"
                                                            size="sm"
                                                            disabled={actionLoading}
                                                            onClick={() => openForfeitModal(participant)}
                                                        >
                                                            Forfait
                                                        </Button>
                                                    )}
                                                    {participant.forfeit && (
                                                        <Button
                                                            variant="outline-success"
                                                            size="sm"
                                                            disabled={actionLoading}
                                                            onClick={() => handleUnforfeit(participant)}
                                                        >
                                                            Annuler forfait
                                                        </Button>
                                                    )}
                                                </div>
                                            </Card.Body>
                                        </Card>
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
                onClick={() => navigate('/commissaire/epreuves')}
            >
                ← Retour aux épreuves
            </Button>

            {/* Modal de détails et inscription */}
            <Modal show={showAddModal} onHide={() => setShowAddModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>
                        📋 Détails - {addTarget?.type === 'TEAM' ? 'Équipe' : 'Athlète'}
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <div className="mb-3">
                        <strong>Nom :</strong> {addTarget?.name}
                    </div>
                    {addTarget?.country && (
                        <div className="mb-3">
                            <strong>Pays :</strong> 🌍 {addTarget?.country}
                        </div>
                    )}
                    <div className="mb-3">
                        <strong>Type :</strong>{' '}
                        <Badge bg={addTarget?.type === 'TEAM' ? 'info' : 'success'}>
                            {addTarget?.type === 'TEAM' ? '👥 Équipe' : '🏃 Athlète'}
                        </Badge>
                    </div>
                    <hr />
                    <p className="text-muted">
                        Voulez-vous inscrire {addTarget?.type === 'TEAM' ? 'cette équipe' : 'cet athlète'} à l'épreuve <strong>{trialData?.trialName}</strong> ?
                    </p>
                </Modal.Body>
                <Modal.Footer>
                    <Button 
                        variant="secondary" 
                        onClick={() => setShowAddModal(false)}
                        disabled={actionLoading}
                    >
                        Annuler
                    </Button>
                    <Button 
                        variant="primary" 
                        onClick={handleConfirmAdd}
                        disabled={actionLoading}
                    >
                        {actionLoading ? (
                            <>
                                <Spinner animation="border" size="sm" className="me-2" />
                                Inscription...
                            </>
                        ) : (
                            'Inscrire le participant'
                        )}
                    </Button>
                </Modal.Footer>
            </Modal>

            {/* Modal de confirmation de forfait */}
            <Modal show={showForfeitModal} onHide={() => setShowForfeitModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>⚠️ Confirmer le forfait</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p>
                        Êtes-vous sûr de vouloir déclarer forfait pour{' '}
                        <strong>{forfeitTarget?.name}</strong> ?
                    </p>
                    <p className="text-muted small">
                        Cette action est irréversible. {forfeitTarget?.type === 'TEAM' ? 'L\'équipe' : 'Le sportif'}{' '}
                        sera marqué(e) comme forfait pour cette épreuve.
                    </p>
                </Modal.Body>
                <Modal.Footer>
                    <Button 
                        variant="secondary" 
                        onClick={() => setShowForfeitModal(false)}
                        disabled={actionLoading}
                    >
                        Annuler
                    </Button>
                    <Button 
                        variant="danger" 
                        onClick={handleConfirmForfeit}
                        disabled={actionLoading}
                    >
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
        </Container>
    );
};

export default ManageParticipants;
