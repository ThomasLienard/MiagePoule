import React, { useEffect, useMemo, useState } from 'react';
import { Modal, Form, Button, Row, Col, Alert, Spinner } from 'react-bootstrap';
import adminIncidentService from '../../services/adminIncidentService';
import { eventService } from '../../services/eventService';
import { getAllCompetitions } from '../../services/championshipService.jsx';

const severityOptions = [
    { value: 'LOW', label: 'LOW' },
    { value: 'MEDIUM', label: 'MEDIUM' },
    { value: 'HIGH', label: 'HIGH' },
    { value: 'CRITICAL', label: 'CRITICAL' }
];

const audienceOptions = [
    { value: 'COMMISSAIRES', label: 'Commissaires' },
    { value: 'COMMISSAIRES_ATHLETES', label: 'Commissaires + Athlètes' },
    { value: 'TOUS', label: 'Tous' }
];

const incidentModeOptions = [
    { value: 'EVENT', label: "Événement" },
    { value: 'COMPETITION', label: "Compétition" },
    { value: 'PLACE', label: "Lieu" }
];

const CreateIncidentModal = ({ show, onClose, onCreated }) => {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        severity: 'LOW',
        incidentMode: 'EVENT',
        eventId: '',
        placeId: '',
        competitionId: '',
        audienceScope: 'TOUS'
    });
    const [errors, setErrors] = useState({});
    const [apiError, setApiError] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [events, setEvents] = useState([]);
    const [competitions, setCompetitions] = useState([]);

    useEffect(() => {
        if (!show) return;

        const loadEvents = async () => {
            try {
                const data = await eventService.getAllWithDetails();
                setEvents(data || []);
            } catch (err) {
                console.warn('Impossible de récupérer les événements', err);
            }
        };

        const loadCompetitions = async () => {
            try {
                const allComps = await getAllCompetitions();
                setCompetitions(allComps);
            } catch (err) {
                console.warn('Impossible de récupérer les compétitions', err);
            }
        };

        loadEvents();
        loadCompetitions();
    }, [show]);

    const placeOptions = useMemo(() => {
        const map = new Map();
        events.forEach(event => {
            if (event.place && event.place.id) {
                map.set(event.place.id, event.place.name || `Lieu ${event.place.id}`);
            }
        });
        return Array.from(map.entries()).map(([id, name]) => ({ value: id, label: name }));
    }, [events]);

    const validateForm = () => {
        const newErrors = {};
        if (!formData.title.trim()) {
            newErrors.title = 'Le titre est requis';
        }
        if (!formData.description.trim()) {
            newErrors.description = 'La description est requise';
        }
        if (!formData.severity) {
            newErrors.severity = 'La sévérité est requise';
        }
        if (formData.incidentMode === 'COMPETITION' && !formData.competitionId) {
            newErrors.competitionId = 'La compétition est requise pour un incident de compétition';
        }
        if (formData.incidentMode === 'COMPETITION' && !formData.audienceScope) {
            newErrors.audienceScope = 'Le niveau d’impact est requis';
        }
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: null }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setApiError(null);
        if (!validateForm()) return;

        setSubmitting(true);
        try {
            const payload = {
                title: formData.title,
                description: formData.description,
                severity: formData.severity,
                eventId: formData.incidentMode === 'EVENT' ? (formData.eventId ? Number(formData.eventId) : null) : null,
                placeId: formData.incidentMode === 'PLACE' ? (formData.placeId ? Number(formData.placeId) : null) : null,
                competitionId: formData.incidentMode === 'COMPETITION' ? (formData.competitionId ? Number(formData.competitionId) : null) : null,
                audienceScope: formData.incidentMode === 'COMPETITION' ? formData.audienceScope : 'TOUS'
            };
            const created = await adminIncidentService.createIncident(payload);
            onCreated(created);
            onClose();
        } catch (err) {
            setApiError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const handleClose = () => {
        setApiError(null);
        setErrors({});
        setFormData({
            title: '',
            description: '',
            severity: 'LOW',
            incidentMode: 'EVENT',
            eventId: '',
            placeId: '',
            competitionId: '',
            audienceScope: 'TOUS'
        });
        onClose();
    };

    return (
        <Modal show={show} onHide={handleClose} size="lg" centered>
            <Modal.Header closeButton>
                <Modal.Title>➕ Créer un incident</Modal.Title>
            </Modal.Header>

            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {apiError && <Alert variant="danger">{apiError}</Alert>}

                    <Form.Group className="mb-3">
                        <Form.Label>Titre *</Form.Label>
                        <Form.Control
                            type="text"
                            name="title"
                            value={formData.title}
                            onChange={handleChange}
                            isInvalid={!!errors.title}
                            placeholder="Ex: Panne d’électricité"
                        />
                        <Form.Control.Feedback type="invalid">
                            {errors.title}
                        </Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Description *</Form.Label>
                        <Form.Control
                            as="textarea"
                            rows={4}
                            name="description"
                            value={formData.description}
                            onChange={handleChange}
                            isInvalid={!!errors.description}
                            placeholder="Décrivez brièvement ce qui se passe..."
                        />
                        <Form.Control.Feedback type="invalid">
                            {errors.description}
                        </Form.Control.Feedback>
                    </Form.Group>

                    <Row className="mb-3">
                        <Col md={6}>
                            <Form.Group>
                                <Form.Label>Sévérité *</Form.Label>
                                <Form.Select
                                    name="severity"
                                    value={formData.severity}
                                    onChange={handleChange}
                                    isInvalid={!!errors.severity}
                                >
                                    {severityOptions.map(option => (
                                        <option key={option.value} value={option.value}>
                                            {option.label}
                                        </option>
                                    ))}
                                </Form.Select>
                                <Form.Control.Feedback type="invalid">
                                    {errors.severity}
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Col>

                        <Col md={6}>
                            <Form.Group>
                                <Form.Label>Cible *</Form.Label>
                                <div>
                                    {incidentModeOptions.map(option => (
                                        <Form.Check
                                            inline
                                            key={option.value}
                                            type="radio"
                                            label={option.label}
                                            name="incidentMode"
                                            id={`incidentMode-${option.value}`}
                                            value={option.value}
                                            checked={formData.incidentMode === option.value}
                                            onChange={handleChange}
                                        />
                                    ))}
                                </div>
                            </Form.Group>
                        </Col>
                    </Row>

                    {formData.incidentMode === 'EVENT' && (
                        <Form.Group className="mb-3">
                            <Form.Label>Épreuve (optionnel)</Form.Label>
                            <Form.Select
                                name="eventId"
                                value={formData.eventId}
                                onChange={handleChange}
                            >
                                <option value="">Aucune</option>
                                {events.map(event => (
                                    <option key={event.id} value={event.id}>
                                        {event.name} ({event.id})
                                    </option>
                                ))}
                            </Form.Select>
                        </Form.Group>
                    )}

                    {formData.incidentMode === 'PLACE' && (
                        <Form.Group className="mb-3">
                            <Form.Label>Lieu (optionnel)</Form.Label>
                            <Form.Select
                                name="placeId"
                                value={formData.placeId}
                                onChange={handleChange}
                            >
                                <option value="">Aucun</option>
                                {placeOptions.map(place => (
                                    <option key={place.value} value={place.value}>
                                        {place.label} ({place.value})
                                    </option>
                                ))}
                            </Form.Select>
                        </Form.Group>
                    )}

                    {formData.incidentMode === 'COMPETITION' && (
                        <> 
                            <Form.Group className="mb-3">
                                <Form.Label>Compétition *</Form.Label>
                                <Form.Select
                                    name="competitionId"
                                    value={formData.competitionId}
                                    onChange={handleChange}
                                    isInvalid={!!errors.competitionId}
                                >
                                    <option value="">Sélectionnez une compétition</option>
                                    {competitions.map(comp => (
                                        <option key={comp.id} value={comp.id}>
                                            {comp.championshipName} / {comp.name} ({comp.id})
                                        </option>
                                    ))}
                                </Form.Select>
                                <Form.Control.Feedback type="invalid">
                                    {errors.competitionId}
                                </Form.Control.Feedback>
                            </Form.Group>

                            <Form.Group className="mb-3">
                                <Form.Label>Niveau d’impact *</Form.Label>
                                <Form.Select
                                    name="audienceScope"
                                    value={formData.audienceScope}
                                    onChange={handleChange}
                                    isInvalid={!!errors.audienceScope}
                                >
                                    {audienceOptions.map(option => (
                                        <option key={option.value} value={option.value}>
                                            {option.label}
                                        </option>
                                    ))}
                                </Form.Select>
                                <Form.Control.Feedback type="invalid">
                                    {errors.audienceScope}
                                </Form.Control.Feedback>
                            </Form.Group>
                        </>
                    )}
                </Modal.Body>

                <Modal.Footer>
                    <Button variant="secondary" onClick={handleClose} disabled={submitting}>
                        Annuler
                    </Button>
                    <Button variant="secondary" type="submit" disabled={submitting}>
                        {submitting ? (
                            <>
                                <Spinner animation="border" size="sm" className="me-2" />
                                Création...
                            </>
                        ) : 'Créer l’incident'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
};

export default CreateIncidentModal;
