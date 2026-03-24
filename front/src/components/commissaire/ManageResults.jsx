import React, { useState, useEffect, useCallback } from 'react';
import PropTypes from 'prop-types';
import {
    Container, Card, Button, Spinner, Alert, Badge,
    Form, InputGroup, Modal, Row, Col
} from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import resultService from '../../services/resultService';

    const allResultsRegistered = () => {
        for (const participant of results) {
            if (!participant.result) {
                return false;
            }
        }
        return true;
    }
// ── Helpers pour format TIME ──────────────────────────────────────────────────

// ms → {hours, minutes, seconds, milliseconds}
const msToTimeParts = (ms) => {
    if (!ms || ms <= 0) return { hours: 0, minutes: 0, seconds: 0, milliseconds: 0 };

    const hours = Math.floor(ms / 3600000);
    const minutes = Math.floor((ms % 3600000) / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    const milliseconds = ms % 1000;

    return { hours, minutes, seconds, milliseconds };
};

// {hours, minutes, seconds, milliseconds} → ms
const timePartsToMs = (parts) => {
    return (
        (parts.hours || 0) * 3600000 +
        (parts.minutes || 0) * 60000 +
        (parts.seconds || 0) * 1000 +
        (parts.milliseconds || 0)
    );
};

// ── Sous-composant : inputs TIME séparés (CORRIGÉ) ────────────────────────────
const TimeInputs = ({
    value,
    onChange,
    disabled
}) => {
    // Toujours avoir des parts valides
    const parts = value && value.ms >= 0
        ? msToTimeParts(value.ms)
        : { hours: 0, minutes: 0, seconds: 0, milliseconds: 0 };

    const handlePartChange = (part, newValue) => {
        if (disabled) return;

        // Gérer les valeurs vides et les flèches
        const numValue = newValue === '' ? 0 : parseInt(newValue) || 0;
        let clampedValue = numValue;

        switch (part) {
            case 'hours':
                clampedValue = Math.max(0, Math.min(99, numValue));
                break;
            case 'minutes':
            case 'seconds':
                clampedValue = Math.max(0, Math.min(59, numValue));
                break;
            case 'milliseconds':
                clampedValue = Math.max(0, Math.min(999, numValue));
                break;
        }

        const newParts = { ...parts, [part]: clampedValue };
        const newMs = timePartsToMs(newParts);
        onChange(newMs);
    };

    const isValid = parts.hours > 0 || parts.minutes > 0 || parts.seconds > 0 || parts.milliseconds > 0;

    return (
        <InputGroup size="sm" className="flex-nowrap">
            {/* Heures */}
            <InputGroup.Text className="bg-light px-1">H</InputGroup.Text>
            <Form.Control
                type="number"
                min="0"
                max="99"
                step="1"
                className="text-center fw-bold"
                style={{ width: '45px' }}
                value={parts.hours}
                disabled={disabled}
                onChange={(e) => handlePartChange('hours', e.target.value)}
                onWheel={(e) => {
                    e.preventDefault();
                    const delta = e.deltaY > 0 ? -1 : 1;
                    const current = parseInt(e.target.value) || 0;
                    handlePartChange('hours', Math.max(0, Math.min(99, current + delta)));
                }}
            />

            <InputGroup.Text className="bg-light px-1">:</InputGroup.Text>

            {/* Minutes */}
            <Form.Control
                type="number"
                min="0"
                max="59"
                step="1"
                className="text-center fw-bold"
                style={{ width: '45px' }}
                value={parts.minutes}
                disabled={disabled}
                onChange={(e) => handlePartChange('minutes', e.target.value)}
                onWheel={(e) => {
                    e.preventDefault();
                    const delta = e.deltaY > 0 ? -1 : 1;
                    const current = parseInt(e.target.value) || 0;
                    handlePartChange('minutes', Math.max(0, Math.min(59, current + delta)));
                }}
            />

            <InputGroup.Text className="bg-light px-1">:</InputGroup.Text>

            {/* Secondes */}
            <Form.Control
                type="number"
                min="0"
                max="59"
                step="1"
                className="text-center fw-bold"
                style={{ width: '45px' }}
                value={parts.seconds}
                disabled={disabled}
                onChange={(e) => handlePartChange('seconds', e.target.value)}
                onWheel={(e) => {
                    e.preventDefault();
                    const delta = e.deltaY > 0 ? -1 : 1;
                    const current = parseInt(e.target.value) || 0;
                    handlePartChange('seconds', Math.max(0, Math.min(59, current + delta)));
                }}
            />

            <InputGroup.Text className="bg-light px-1">.</InputGroup.Text>

            {/* Millisecondes */}
            <Form.Control
                type="number"
                min="0"
                max="999"
                step="1"
                className="text-center fw-bold"
                style={{ width: '55px' }}
                value={parts.milliseconds}
                disabled={disabled}
                onChange={(e) => handlePartChange('milliseconds', e.target.value)}
                onWheel={(e) => {
                    e.preventDefault();
                    const delta = e.deltaY > 0 ? -1 : 1;
                    const current = parseInt(e.target.value) || 0;
                    handlePartChange('milliseconds', Math.max(0, Math.min(999, current + delta)));
                }}
            />

            {!isValid && !disabled && (
                <InputGroup.Text className="bg-danger text-white fs-9 px-1">!</InputGroup.Text>
            )}
        </InputGroup>
    );
};

TimeInputs.propTypes = {
    value: PropTypes.shape({
        ms: PropTypes.number
    }),
    onChange: PropTypes.func.isRequired,
    disabled: PropTypes.bool
};

// ── Sous-composants inchangés ─────────────────────────────────────────────────
const LoadingState = () => (
    <Container className="py-4 text-center">
        <Spinner animation="border" role="status">
            <span className="visually-hidden">Chargement...</span>
        </Spinner>
    </Container>
);

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

const ScorePlaceholder = ({ scoreType, isForfeit }) => {
    if (isForfeit) return 'Forfait';
    return scoreType === 'SCORE' ? 'Saisir un résultat…' : '';
};

const ValidateAllModal = ({ show, trialName, loading, onHide, onConfirm }) => (
    <Modal show={show} onHide={onHide} centered>
        <Modal.Header closeButton>
            <Modal.Title>Valider tous les résultats</Modal.Title>
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
                {loading ? <><Spinner animation="border" size="sm" className="me-2" />Validation...</> : 'Valider tout'}
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

// ── Composant ligne de résultat ───────────────────────────────────────────────
const ResultRow = ({
    participant,
    editValue,
    scoreType,
    onChange,
    onSave,
    onValidate,
    onInvalidate,
    actionLoading,
    canEdit
}) => {
    const isDisabled = participant.isForfeit || actionLoading || !canEdit;

    const isValid = scoreType === 'TIME'
        ? editValue?.ms > 0
        : !!editValue;

    return (
        <Card className={`shadow-sm mb-2 ${participant.isValidated ? 'border-success' : ''} ${participant.isForfeit ? 'border-danger opacity-75' : ''}`}>
            <Card.Body className="py-2">
                <Row className="align-items-center g-2">
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

                    <Col xs={12} md={4}>
                        {scoreType === 'TIME' ? (
                            <TimeInputs
                                value={editValue}
                                onChange={(ms) => onChange(participant.participantId, ms)}
                                disabled={isDisabled}
                            />
                        ) : (
                            <InputGroup size="sm">
                                <Form.Control
                                    type="text"
                                    placeholder={ScorePlaceholder({ scoreType, isForfeit: participant.isForfeit })}
                                    value={editValue || ''}
                                    disabled={isDisabled}
                                    onChange={(e) => onChange(e.target.value)}
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

                    <Col xs={12} md={4} className="d-flex align-items-center gap-2 justify-content-md-end">
                        <StatusBadge isValidated={participant.isValidated} isForfeit={participant.isForfeit} />
                        {!participant.isForfeit && (
                            participant.isValidated ? (
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
                                    disabled={isDisabled || !isValid}
                                    onClick={() => onValidate(participant)}
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
    participant: PropTypes.shape({
        participantId: PropTypes.number.isRequired,
        participantName: PropTypes.string,
        participantType: PropTypes.oneOf(['ATHLETE', 'TEAM']),
        country: PropTypes.string,
        result: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
        isValidated: PropTypes.bool,
        isForfeit: PropTypes.bool,
    }).isRequired,
    scoreType: PropTypes.oneOf(['TIME', 'SCORE']).isRequired,
    editValue: PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.shape({ ms: PropTypes.number })
    ]),
    onChange: PropTypes.func.isRequired,
    onSave: PropTypes.func.isRequired,
    onValidate: PropTypes.func.isRequired,
    onInvalidate: PropTypes.func.isRequired,
    actionLoading: PropTypes.bool,
    canEdit: PropTypes.bool,
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

    const canEdit = trialData?.startTime
        ? new Date() >= new Date(trialData.startTime)
        : false;

    const fetchResults = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await resultService.getTrialResults(trialId);
            setTrialData(data);
            const init = {};
            (data.results || []).forEach(r => {
                if (data.scoreType === 'TIME' && r.result) {
                    init[r.participantId] = { ms: parseInt(r.result) || 0 };
                } else {
                    init[r.participantId] = r.result ?? '';
                }
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

    const handleChange = (participantId, msValue) => {
        setEditValues(prev => ({
            ...prev,
            [participantId]: { ms: Number(msValue) || 0 }
        }));
    };

    const handleSave = async (participant) => {
        setActionLoading(true);
        setError(null);
        setSuccess(null);
        try {
            const resultToSend = trialData?.scoreType === 'TIME'
                ? editValues[participant.participantId]?.ms ?? 0
                : editValues[participant.participantId];

            const updated = await resultService.setResult(
                trialId,
                participant.participantId,
                participant.participantType,
                resultToSend
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

    const handleSaveAll = async () => {
        setActionLoading(true);
        setError(null);
        setSuccess(null);
        try {
            const bulkPayload = (trialData.results || [])
                .filter(r => !r.isForfeit)
                .map(r => {
                    const resultValue = trialData?.scoreType === 'TIME'
                        ? editValues[r.participantId]?.ms ?? 0
                        : editValues[r.participantId];
                    return {
                        participantId: r.participantId,
                        participantType: r.participantType,
                        result: resultValue
                    };
                });
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

    const handleValidateAll = async () => {
        setActionLoading(true);
        setError(null);
        setSuccess(null);
        setShowValidateAllModal(false);
        try {
            const updated = await resultService.validateAllResults(trialId);
            setTrialData(updated);
            const init = {};
            (updated.results || []).forEach(r => {
                if (updated.scoreType === 'TIME' && r.result) {
                    init[r.participantId] = { ms: parseInt(r.result) || 0 };
                } else {
                    init[r.participantId] = r.result ?? '';
                }
            });
            setEditValues(init);
            setSuccess('Tous les résultats ont été validés.');
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    const allResultsRegistered = () => {
        const results = trialData?.results || [];
        for (const participant of results) {
            if (!participant.isForfeit) {
                const value = editValues[participant.participantId];
                if (trialData?.scoreType === 'TIME') {
                    if (!value?.ms || value.ms <= 0) return false;
                } else {
                    if (!value) return false;
                }
            }
        }
        return true;
    };

    if (loading) return <LoadingState />;

    if (!trialData) {
        return (
            <Container className="py-4">
                <Alert variant="warning">Épreuve non trouvée.</Alert>
                <Button variant="outline-secondary" onClick={() => navigate(-1)}>← Retour</Button>
            </Container>
        );
    }

    const { trialName, teamTrial, scoreType = 'SCORE', results = [] } = trialData;
    const validatedCount = results.filter(r => !r.isForfeit && r.isValidated).length;
    const totalCount = results.filter(r => !r.isForfeit).length;

        return (
            <Container className="py-4">
                {/* En-tête */}
                <div className="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-3">
                    <div>
                        <h1 className="mb-1">Résultats — {trialName}</h1>
                        <div className="d-flex gap-2 flex-wrap">
                            <Badge bg={teamTrial ? 'info' : 'success'}>
                                {teamTrial ? '👥 Épreuve Équipe' : '🏃 Épreuve Solo'}
                            </Badge>
                            <Badge bg={scoreType === 'TIME' ? 'warning' : 'primary'}>
                                {scoreType === 'TIME' ? '⏱️ Temps' : '🎯 Score'}
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
                            {actionLoading ? <Spinner animation="border" size="sm" /> : 'Tout enregistrer'}
                        </Button>
                        <Button
                            variant="success"
                            onClick={() => setShowValidateAllModal(true)}
                            disabled={actionLoading || results.length === 0 || !canEdit || !allResultsRegistered()}
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
                        {scoreType === 'TIME' && (
                            <small className="text-muted ms-2">
                                (H:MM:SS.mmm → ms au backend)
                            </small>
                        )}
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
                                    scoreType={scoreType}
                                    editValue={editValues[participant.participantId]}
                                    onChange={handleChange}
                                    onSave={handleSave}
                                    onValidate={handleValidate}
                                    onInvalidate={handleInvalidate}
                                    actionLoading={actionLoading}
                                    canEdit={canEdit}
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
