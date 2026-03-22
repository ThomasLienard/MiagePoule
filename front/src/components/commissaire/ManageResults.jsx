import React, { useState, useEffect, useCallback } from 'react';
import PropTypes from 'prop-types';
import {
    Container, Card, Button, Spinner, Alert, Badge,
    Form, InputGroup, Modal, Row, Col
} from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import resultService from '../../services/resultService';

// ── Utilitaires de conversion temps ───────────────────────────────────────────
/**
 * Convertit un format hh:mm:ss:ms en millisecondes
 * @param {string} timeStr - Format "hh:mm:ss:ms" ou "HH:MM:SS:MS"
 * @returns {number} Nombre de millisecondes
 */
const timeStringToMilliseconds = (timeStr) => {
    if (!timeStr || typeof timeStr !== 'string') return 0;
    const parts = timeStr.split(':');
    if (parts.length !== 4) return 0;
    
    const hours = parseInt(parts[0], 10) || 0;
    const minutes = parseInt(parts[1], 10) || 0;
    const seconds = parseInt(parts[2], 10) || 0;
    const milliseconds = parseInt(parts[3], 10) || 0;
    
    return hours * 3600000 + minutes * 60000 + seconds * 1000 + milliseconds;
};

/**
 * Convertit des millisecondes en format hh:mm:ss:ms
 * @param {number} ms - Nombre de millisecondes
 * @returns {string} Format "HH:MM:SS:MS"
 */
const millisecondsToTimeString = (ms) => {
    if (!ms || ms < 0) return '00:00:00:000';
    
    const hours = Math.floor(ms / 3600000);
    const minutes = Math.floor((ms % 3600000) / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    const milliseconds = ms % 1000;
    
    return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}:${String(milliseconds).padStart(3, '0')}`;
};

// ── Sous-composant : état chargement ──────────────────────────────────────────
const LoadingState = () => (
    <Container className="py-4 text-center">
        <Spinner animation="border" role="status">
            <span className="visually-hidden">Chargement...</span>
        </Spinner>
    </Container>
);

// ── Sous-composant : badge de statut ──────────────────────────────────────────
const StatusBadge = ({ isValidated, isForfeit }) => {
    if (isForfeit) return <Badge bg="danger">Forfait</Badge>;
    if (isValidated) return <Badge bg="success">✔ Validé</Badge>;
    return <Badge bg="secondary">Non validé</Badge>;
};

StatusBadge.propTypes = {
    isValidated: PropTypes.bool,
    isForfeit:   PropTypes.bool,
};

StatusBadge.defaultProps = {
    isValidated: false,
    isForfeit:   false,
};

// ── Sous-composant : input temps (format hh:mm:ss:ms) ──────────────────────────
const TimeInput = ({ value, disabled, onChange }) => {
    const timeStr = value ? millisecondsToTimeString(value) : '00:00:00:000';
    const [hours, minutes, seconds, milliseconds] = timeStr.split(':');
    
    const handleChange = (type, val) => {
        // Limiter les valeurs en entrée
        let cleanVal = val.replace(/\D/g, ''); // Garder seulement les chiffres
        
        if (type === 'hours') {
            cleanVal = Math.max(0, Math.min(99, parseInt(cleanVal, 10) || 0)).toString();
        } else if (type === 'milliseconds') {
            cleanVal = Math.max(0, Math.min(999, parseInt(cleanVal, 10) || 0)).toString();
            cleanVal = cleanVal.padStart(3, '0');
        } else {
            cleanVal = Math.max(0, Math.min(59, parseInt(cleanVal, 10) || 0)).toString();
        }
        
        if (type !== 'milliseconds') {
            cleanVal = cleanVal.padStart(2, '0');
        }
        
        const newHours = type === 'hours' ? cleanVal : hours;
        const newMinutes = type === 'minutes' ? cleanVal : minutes;
        const newSeconds = type === 'seconds' ? cleanVal : seconds;
        const newMilliseconds = type === 'milliseconds' ? cleanVal : milliseconds;
        
        const newTimeStr = `${newHours}:${newMinutes}:${newSeconds}:${newMilliseconds}`;
        const ms = timeStringToMilliseconds(newTimeStr);
        onChange(ms);
    };
    
    return (
        <div className="d-flex gap-1 align-items-center">
            <Form.Control
                type="text"
                inputMode="numeric"
                maxLength="2"
                disabled={disabled}
                className="text-center fw-bold"
                value={hours}
                onChange={(e) => handleChange('hours', e.target.value)}
                style={{ width: '50px' }}
                placeholder="00"
                title="Heures"
            />
            <span className="fw-bold text-muted">:</span>
            <Form.Control
                type="text"
                inputMode="numeric"
                maxLength="2"
                disabled={disabled}
                className="text-center fw-bold"
                value={minutes}
                onChange={(e) => handleChange('minutes', e.target.value)}
                style={{ width: '50px' }}
                placeholder="00"
                title="Minutes"
            />
            <span className="fw-bold text-muted">:</span>
            <Form.Control
                type="text"
                inputMode="numeric"
                maxLength="2"
                disabled={disabled}
                className="text-center fw-bold"
                value={seconds}
                onChange={(e) => handleChange('seconds', e.target.value)}
                style={{ width: '50px' }}
                placeholder="00"
                title="Secondes"
            />
            <span className="fw-bold text-muted">:</span>
            <Form.Control
                type="text"
                inputMode="numeric"
                maxLength="3"
                disabled={disabled}
                className="text-center fw-bold"
                value={milliseconds}
                onChange={(e) => handleChange('milliseconds', e.target.value)}
                style={{ width: '60px' }}
                placeholder="000"
                title="Millisecondes"
            />
        </div>
    );
};

TimeInput.propTypes = {
    value: PropTypes.number,
    disabled: PropTypes.bool,
    onChange: PropTypes.func.isRequired,
};

TimeInput.defaultProps = {
    value: 0,
    disabled: false,
};

// ── Sous-composant : modal confirmation valider tout ──────────────────────────
const ValidateAllModal = ({ show, trialName, loading, onHide, onConfirm }) => (
    <Modal show={show} onHide={onHide} centered>
        <Modal.Header closeButton>
            <Modal.Title> Valider tous les résultats</Modal.Title>
        </Modal.Header>
        <Modal.Body>
            <p>
                Voulez-vous valider <strong>tous les résultats</strong> de l'épreuve{' '}
                <strong>{trialName}</strong> en une seule fois ?
            </p>
            <Alert variant="info" className="mb-0">
                Seuls les résultats non encore validés seront affectés.
            </Alert>
        </Modal.Body>
        <Modal.Footer>
            <Button variant="secondary" onClick={onHide} disabled={loading}>Annuler</Button>
            <Button variant="success" onClick={onConfirm} disabled={loading}>
                {loading
                    ? <><Spinner animation="border" size="sm" className="me-2" />Validation...</>
                    : ' Valider tout'}
            </Button>
        </Modal.Footer>
    </Modal>
);

ValidateAllModal.propTypes = {
    show:      PropTypes.bool.isRequired,
    trialName: PropTypes.string,
    loading:   PropTypes.bool,
    onHide:    PropTypes.func.isRequired,
    onConfirm: PropTypes.func.isRequired,
};

ValidateAllModal.defaultProps = {
    trialName: '',
    loading:   false,
};

// ── Sous-composant : une ligne de résultat ────────────────────────────────────
const ResultRow = ({
    participant, editValue, onChange, onSave,
    onValidate, onInvalidate, actionLoading, canEdit, scoreType
}) => {
    const isDisabled = participant.isForfeit || actionLoading || !canEdit;
    const isTimeFormat = scoreType === 'TIME';

    return (
        <Card className={`shadow-sm mb-2 ${participant.isValidated ? 'border-success' : ''} ${participant.isForfeit ? 'border-danger opacity-75' : ''}`}>
            <Card.Body className="py-2">
                <Row className="align-items-center g-2">
                    {/* Nom + pays + statut */}
                    <Col xs={12} md={4}>
                        <div className="fw-semibold">
                            {participant.participantType === 'TEAM'
                                ? <span>👥 {participant.participantName}</span>
                                : <span>🏃 {participant.participantName}</span>}
                        </div>
                        {participant.country && (
                            <small className="text-muted">🌍 {participant.country}</small>
                        )}
                    </Col>

                    {/* Saisie du résultat */}
                    <Col xs={12} md={4}>
                        {isTimeFormat ? (
                            <div className="d-flex gap-2 align-items-center">
                                <TimeInput
                                    value={editValue ? parseInt(editValue, 10) : 0}
                                    disabled={isDisabled}
                                    onChange={(ms) => onChange(participant.participantId, String(ms))}
                                />
                                <Button
                                    variant="outline-secondary"
                                    size="sm"
                                    disabled={isDisabled}
                                    onClick={() => onSave(participant)}
                                    title="Enregistrer ce résultat"
                                    className="flex-shrink-0"
                                >
                                    enregistrer
                                </Button>
                            </div>
                        ) : (
                            <InputGroup size="sm">
                                <Form.Control
                                    type="text"
                                    placeholder={participant.isForfeit ? 'Forfait' : 'Saisir un résultat…'}
                                    value={editValue ?? ''}
                                    disabled={isDisabled}
                                    onChange={(e) => onChange(participant.participantId, e.target.value)}
                                />
                                <Button
                                    variant="outline-secondary"
                                    size="sm"
                                    disabled={isDisabled}
                                    onClick={() => onSave(participant)}
                                    title="Enregistrer ce résultat"
                                >
                                    enregistrer
                                </Button>
                            </InputGroup>
                        )}
                    </Col>

                    {/* Statut + actions validation */}
                    <Col xs={12} md={4} className="d-flex align-items-center gap-2 justify-content-md-end">
                        <StatusBadge isValidated={participant.isValidated} isForfeit={participant.isForfeit} />
                        {!participant.isForfeit && (
                            participant.isValidated
                                ? (
                                    <Button
                                        variant="outline-warning"
                                        size="sm"
                                        disabled={actionLoading || !canEdit}
                                        onClick={() => onInvalidate(participant)}
                                        title="Invalider ce résultat"
                                    >
                                        ✕ Invalider
                                    </Button>
                                ) : (
                                    <Button
                                        variant="outline-success"
                                        size="sm"
                                        disabled={isDisabled || !participant.result}
                                        onClick={() => onSave(participant)}
                                        title="Valider ce résultat"
                                    >
                                        ✔ Valider
                                    </Button>
                                )
                        )}
                    </Col>
                </Row>
            </Card.Body>
        </Card>
    );
};

ResultRow.propTypes = {
    participant:   PropTypes.shape({
        participantId:   PropTypes.number.isRequired,
        participantName: PropTypes.string,
        participantType: PropTypes.oneOf(['ATHLETE', 'TEAM']),
        country:         PropTypes.string,
        result:          PropTypes.string,
        isValidated:     PropTypes.bool,
        isForfeit:       PropTypes.bool,
    }).isRequired,
    editValue:     PropTypes.string,
    onChange:      PropTypes.func.isRequired,
    onSave:        PropTypes.func.isRequired,
    onValidate:    PropTypes.func.isRequired,
    onInvalidate:  PropTypes.func.isRequired,
    actionLoading: PropTypes.bool,
    canEdit:       PropTypes.bool,
    scoreType:     PropTypes.string,
};

ResultRow.defaultProps = {
    editValue:     '',
    actionLoading: false,
    canEdit:       false,
    scoreType:     'POINTS',
};

// ── Composant principal ───────────────────────────────────────────────────────
const ManageResults = () => {
    const { trialId } = useParams();
    const navigate = useNavigate();

    const [trialData, setTrialData] = useState(null);
    const [editValues, setEditValues] = useState({});
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [showValidateAllModal, setShowValidateAllModal] = useState(false);

    // true si l'épreuve a démarré (en cours ou terminée)
    const canEdit = trialData?.startTime
        ? new Date() >= new Date(trialData.startTime)
        : false;

    // ── Chargement des résultats ───────────────────────────────────────────────
    const fetchResults = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await resultService.getTrialResults(trialId);
            setTrialData(data);
            // Initialiser les valeurs d'édition avec les résultats existants
            const init = {};
            (data.results || []).forEach(r => {
                init[r.participantId] = r.result ?? '';
            });
            setEditValues(init);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, [trialId]);

    useEffect(() => {
        fetchResults();
    }, [fetchResults]);

    // ── Gestion changement local d'un champ ──────────────────────────────────
    const handleChange = (participantId, value) => {
        setEditValues(prev => ({ ...prev, [participantId]: value }));
    };

    // ── Sauvegarde d'un résultat individuel ──────────────────────────────────
    const handleSave = async (participant) => {
        setActionLoading(true);
        setError(null);
        setSuccess(null);
        try {
            const updated = await resultService.setResult(
                trialId,
                participant.participantId,
                participant.participantType,
                editValues[participant.participantId] ?? ''
            );
            setTrialData(prev => ({
                ...prev,
                results: prev.results.map(r =>
                    r.participantId === updated.participantId ? updated : r
                )
            }));
            setSuccess(`Résultat de "${participant.participantName}" enregistré.`);
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    // ── Sauvegarde de tous les résultats modifiés (bulk) ─────────────────────
    const handleSaveAll = async () => {
        setActionLoading(true);
        setError(null);
        setSuccess(null);
        try {
            const bulkPayload = (trialData.results || [])
                .filter(r => !r.isForfeit)
                .map(r => ({
                    participantId: r.participantId,
                    participantType: r.participantType,
                    result: editValues[r.participantId] ?? ''
                }));
            const updated = await resultService.setBulkResults(trialId, bulkPayload);
            setTrialData(prev => {
                const updatedMap = {};
                updated.forEach(u => { updatedMap[u.participantId] = u; });
                return {
                    ...prev,
                    results: prev.results.map(r =>
                        updatedMap[r.participantId] ? updatedMap[r.participantId] : r
                    )
                };
            });
            setSuccess('Tous les résultats ont été enregistrés.');
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    // ── Validation individuelle ───────────────────────────────────────────────
    const handleValidate = async (participant) => {
        setActionLoading(true);
        setError(null);
        setSuccess(null);
        try {
            const updated = await resultService.validateResult(
                trialId,
                participant.participantId,
                participant.participantType
            );
            setTrialData(prev => ({
                ...prev,
                results: prev.results.map(r =>
                    r.participantId === updated.participantId ? updated : r
                )
            }));
            setSuccess(`Résultat de "${participant.participantName}" validé.`);
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    // ── Invalidation individuelle ─────────────────────────────────────────────
    const handleInvalidate = async (participant) => {
        setActionLoading(true);
        setError(null);
        setSuccess(null);
        try {
            const updated = await resultService.invalidateResult(
                trialId,
                participant.participantId,
                participant.participantType
            );
            setTrialData(prev => ({
                ...prev,
                results: prev.results.map(r =>
                    r.participantId === updated.participantId ? updated : r
                )
            }));
            setSuccess(`Résultat de "${participant.participantName}" invalidé.`);
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    // ── Validation de toute l'épreuve ────────────────────────────────────────
    const handleValidateAll = async () => {
        setActionLoading(true);
        setError(null);
        setSuccess(null);
        setShowValidateAllModal(false);
        try {
            const updated = await resultService.validateAllResults(trialId);
            setTrialData(updated);
            const init = {};
            (updated.results || []).forEach(r => { init[r.participantId] = r.result ?? ''; });
            setEditValues(init);
            setSuccess('Tous les résultats ont été validés.');
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    // ── Rendu ─────────────────────────────────────────────────────────────────
    if (loading) return <LoadingState />;

    if (!trialData) {
        return (
            <Container className="py-4">
                <Alert variant="warning">Épreuve non trouvée.</Alert>
                <Button variant="outline-secondary" onClick={() => navigate(-1)}>← Retour</Button>
            </Container>
        );
    }

    const { trialName, teamTrial, results = [] } = trialData;
    const validatedCount = results.filter(r => !r.isForfeit && r.isValidated).length;
    const totalCount = results.filter(r => !r.isForfeit).length;

    return (
        <Container className="py-4">
            {/* En-tête */}
            <div className="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-3">
                <div>
                    <h1 className="mb-1"> Résultats — {trialName}</h1>
                    <div className="d-flex gap-2 flex-wrap">
                        <Badge bg={teamTrial ? 'info' : 'success'}>
                            {teamTrial ? '👥 Épreuve Équipe' : '🏃 Épreuve Solo'}
                        </Badge>
                        <Badge bg={validatedCount === totalCount && totalCount > 0 ? 'success' : 'secondary'}>
                            {validatedCount}/{totalCount} validé(s)
                        </Badge>
                    </div>
                </div>
                <div className="d-flex gap-2">
                    <Button
                        variant="outline-secondary"
                        onClick={handleSaveAll}
                        disabled={actionLoading || results.length === 0 || !canEdit}
                    >
                        {actionLoading ? <Spinner animation="border" size="sm" /> : ' Tout enregistrer'}
                    </Button>
                    <Button
                        variant="success"
                        onClick={() => setShowValidateAllModal(true)}
                        disabled={actionLoading || results.length === 0 || !canEdit}
                    >
                         Valider tout
                    </Button>
                </div>
            </div>

            {/* Bannière épreuve pas encore commencée */}
            {!canEdit && trialData?.startTime && (
                <Alert variant="warning" className="d-flex align-items-center gap-2">
                    🔒 <span>
                        La saisie des résultats est <strong>verrouillée</strong> : l'épreuve commence le{' '}
                        <strong>{new Date(trialData.startTime).toLocaleString('fr-FR')}</strong>.
                    </span>
                </Alert>
            )}

            {/* Messages feedback */}
            {error && (
                <Alert variant="danger" dismissible onClose={() => setError(null)}>
                    {error}
                </Alert>
            )}
            {success && (
                <Alert variant="success" dismissible onClose={() => setSuccess(null)}>
                    {success}
                </Alert>
            )}

            {/* Liste des résultats */}
            <Card>
                <Card.Header as="h5">
                    {teamTrial ? 'Résultats par équipe' : 'Résultats par athlète'}
                </Card.Header>
                <Card.Body>
                    {results.length === 0 ? (
                        <p className="text-center text-muted py-3">
                            Aucun participant inscrit à cette épreuve.
                        </p>
                    ) : (
                        results.map(participant => (
                            <ResultRow
                                key={`${participant.participantType}-${participant.participantId}`}
                                participant={participant}
                                editValue={editValues[participant.participantId]}
                                onChange={handleChange}
                                onSave={handleSave}
                                onValidate={handleValidate}
                                onInvalidate={handleInvalidate}
                                actionLoading={actionLoading}
                                canEdit={canEdit}
                                scoreType={trialData.scoreType}
                            />
                        ))
                    )}
                </Card.Body>
            </Card>

            {/* Bouton retour */}
            <Button
                variant="outline-secondary"
                className="mt-3"
                onClick={() => navigate('/commissaire/trials')}
            >
                ← Retour aux épreuves
            </Button>

            {/* Modal confirmation valider tout */}
            <ValidateAllModal
                show={showValidateAllModal}
                trialName={trialName}
                loading={actionLoading}
                onHide={() => setShowValidateAllModal(false)}
                onConfirm={handleValidateAll}
            />
        </Container>
    );
};

export default ManageResults;
