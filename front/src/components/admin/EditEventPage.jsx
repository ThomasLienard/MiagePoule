import React, { useState, useEffect, useCallback } from 'react';
import { Form, Button, Row, Col, Card, Container, Alert, Spinner } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import {getChampionshipCompetition, getChampionships} from "../../services/championshipService.jsx";
import {eventService} from "../../services/eventService.jsx";
import commissaireUserService from "../../services/commissaireUserService.jsx";

const EditEventPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const queryParams = new URLSearchParams(location.search);
    const eventIdFromUrl = queryParams.get('id');

    const [allEvents, setAllEvents] = useState([]);
    const [championships, setChampionships] = useState([]);
    const [competitions, setCompetitions] = useState([]);
    const [commissaires, setCommissaires] = useState([]);
    const [selectedEventId, setSelectedEventId] = useState(eventIdFromUrl || '');
    const [loadingInitial, setLoadingInitial] = useState(true);
    const [loadingDetails, setLoadingDetails] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [status, setStatus] = useState({ type: '', message: '' });
    const [selectedChampionshipId, setSelectedChampionshipId] = useState('');
    const [compLimits, setCompLimits] = useState({ start: '', end: '' });

    const storedUser = JSON.parse(localStorage.getItem('user')) || {};
    const isCommissaire = storedUser.roles?.includes('COMMISSAIRE');
    storedUser.roles?.join(', ') || 'INVITÉ';


    const [formData, setFormData] = useState({
        name: '',
        description: '',
        typeEventName: '',
        competitionId: '',
        commissaireId: '',
        startTime: '',
        endTime: '',
        placeName: '',
        number: '',
        street: '',
        zip: '',
        city: '',
        descriptionPlace: '',
        parking: false
    });

    const formatDateTime = (dateStr) => {
        if (!dateStr) return '';
        return dateStr.length === 10 ? `${dateStr}T00:00` : dateStr.substring(0, 16);
    };

    const fetchEventDetails = useCallback(async (eventId, currentChamps) => {
        if (!eventId) return;
        setLoadingDetails(true);
        try {
            const event = await eventService.getEventById(eventId);

            for (const champ of currentChamps) {
                const compsRes = await getChampionshipCompetition(champ.id)
                const comps = compsRes.data;
                const match = comps.find(c => c.name === event.competitionName);

                if (match) {
                    setSelectedChampionshipId(champ.id);
                    setCompetitions(comps);
                    setCompLimits({
                        start: formatDateTime(match.start),
                        end: formatDateTime(match.end)
                    });

                    setFormData({
                        name: event.name || '',
                        description: event.description || '',
                        typeEventName: event.typeEvent || 'MEETING',
                        competitionId: match.id,
                        commissaireId: event.commissaireId || '',
                        startTime: event.timeSlot?.start ? formatDateTime(event.timeSlot.start) : '',
                        endTime: event.timeSlot?.end ? formatDateTime(event.timeSlot.end) : '',
                        placeName: event.place?.name || '',
                        number: event.place?.number || '',
                        street: event.place?.street || '',
                        zip: event.place?.zip || '',
                        city: event.place?.city || '',
                        descriptionPlace: event.place?.description || '',
                        parking: event.place?.parking || false
                    });
                    break;
                }
            }
        } catch (err) {
            console.log(err)
            setStatus({ type: 'danger', message: "Erreur de chargement des détails." });
        } finally {
            setLoadingDetails(false);
        }
    }, []);

    useEffect(() => {
        const initPage = async () => {
            try {
                const [champsRes, eventsRes, commsRes] = await Promise.all([
                    getChampionships(),
                    eventService.getAll(),
                    commissaireUserService.getUsersByRole("COMMISSAIRE")
                ]);
                setChampionships(champsRes);
                setAllEvents(eventsRes);
                setCommissaires(commsRes);

                if (eventIdFromUrl) {
                    await fetchEventDetails(eventIdFromUrl, champsRes);
                }
            } catch (err) {
                setStatus({ type: 'danger', message: "Erreur serveur lors de l'initialisation." });
            } finally {
                setLoadingInitial(false);
            }
        };
        initPage();
    }, [eventIdFromUrl, fetchEventDetails]);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        if (isCommissaire && !["startTime", "endTime"].includes(name)) return;

        const val = type === 'checkbox' ? checked : value;

        setFormData(prev => {
            const newData = { ...prev, [name]: val };
            // Si le type change et n'est plus TRIAL, on vide le commissaireId
            if (name === 'typeEventName' && val !== 'TRIAL') {
                newData.commissaireId = '';
            }
            return newData;
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        const finalId = parseInt(selectedEventId || eventIdFromUrl);

        const dataToSubmit = {
            id: finalId,
            name: formData.name,
            competitionId: parseInt(formData.competitionId),
            commissaireId: formData.typeEventName === 'TRIAL' && formData.commissaireId ? parseInt(formData.commissaireId) : null,
            timeSlot: {
                start: formData.startTime.length === 16 ? formData.startTime + ":00" : formData.startTime,
                end: formData.endTime.length === 16 ? formData.endTime + ":00" : formData.endTime
            },
            place: {
                name: formData.placeName,
                street: formData.street,
                number: formData.number,
                city: formData.city,
                zip: formData.zip,
                parking: formData.parking
            },
            description: formData.description,
            typeEventName: formData.typeEventName
        };

        try {
            if (isCommissaire) {
                await eventService.editEventCommissaire(finalId, dataToSubmit);
            } else {
                await eventService.editEvent(finalId, dataToSubmit);
            }

            setStatus({ type: 'success', message: 'Mise à jour réussie !' });
            setTimeout(() => navigate(-1), 1500);
        } catch (error) {
            setStatus({ type: 'danger', message: "Erreur lors de la sauvegarde." });
        } finally {
            setSubmitting(false);
        }
    };

    if (loadingInitial) return <Container className="py-5 text-center"><Spinner animation="border" /></Container>;

    return (
        <Container className="py-5">
            <h2 className="mb-4 text-center">
                {isCommissaire ? '⏱️ Modification des Horaires' : "⚙️ Gestion de l'Évènement"}
            </h2>

            {status.message && <Alert variant={status.type} dismissible onClose={() => setStatus({type:'', message:''})}>{status.message}</Alert>}

            {!isCommissaire && (
                <Card className="shadow-sm mb-4 bg-light border-0">
                    <Card.Body>
                        <Form.Group controlId="selectEvent">
                            <Form.Label className="fw-bold">Épreuve à gérer</Form.Label>
                            <Form.Select value={selectedEventId} onChange={(e) => {
                                setSelectedEventId(e.target.value);
                                fetchEventDetails(e.target.value, championships);
                            }}>
                                <option value="">--- Choisir une épreuve ---</option>
                                {allEvents.map(ev => <option key={ev.id} value={ev.id}>{ev.name} ({ev.competitionName})</option>)}
                            </Form.Select>
                        </Form.Group>
                    </Card.Body>
                </Card>
            )}

            {loadingDetails ? (
                <div className="text-center py-5"><Spinner animation="grow" /></div>
            ) : (selectedEventId || eventIdFromUrl) && (
                <Form onSubmit={handleSubmit}>

                    <Card className="mb-4 shadow-sm border">
                        <Card.Body>
                            <h5 className="text-primary fw-bold mb-3">1. Contexte</h5>
                            {!isCommissaire && (
                                <Row className="mb-3">
                                    <Col md={6}>
                                        <Form.Group controlId="selectChampionat">
                                            <Form.Label className="fw-bold">Championnat</Form.Label>
                                            <Form.Select
                                                value={selectedChampionshipId}
                                                onChange={async (e) => {
                                                    const champId = e.target.value;
                                                    setSelectedChampionshipId(champId);
                                                    const res = await getChampionshipCompetition(champId);
                                                    setCompetitions(res.data);
                                                }}
                                            >
                                                {championships.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                            </Form.Select>
                                        </Form.Group>
                                    </Col>
                                    <Col md={6}>
                                        <Form.Group controlId="selectCompetition">
                                            <Form.Label className="fw-bold">Compétition</Form.Label>
                                            <Form.Select
                                                name="competitionId"
                                                value={formData.competitionId}
                                                onChange={(e) => {
                                                    const compId = e.target.value;
                                                    const comp = competitions.find(c => c.id === parseInt(compId));
                                                    setFormData(p => ({...p, competitionId: compId}));
                                                    if (comp) setCompLimits({ start: formatDateTime(comp.start), end: formatDateTime(comp.end) });
                                                }}
                                            >
                                                {competitions.map(comp => <option key={comp.id} value={comp.id}>{comp.name}</option>)}
                                            </Form.Select>
                                        </Form.Group>
                                    </Col>
                                </Row>
                            )}

                            <Row>
                                <Col md={6} className="mb-3">
                                    <Form.Group controlId="selectDateDebut">
                                        <Form.Label className="fw-bold">Début</Form.Label>
                                        <Form.Control
                                            type="datetime-local"
                                            name="startTime"
                                            value={formData.startTime}
                                            onChange={handleChange}
                                            min={compLimits.start}
                                            max={compLimits.end}
                                            required
                                        />
                                    </Form.Group>
                                </Col>
                                <Col md={6} className="mb-3">
                                    <Form.Group controlId="selectDateFin">
                                        <Form.Label className="fw-bold">Fin</Form.Label>
                                        <Form.Control
                                            type="datetime-local"
                                            name="endTime"
                                            value={formData.endTime}
                                            onChange={handleChange}
                                            min={formData.startTime || compLimits.start}
                                            max={compLimits.end}
                                            required
                                        />
                                    </Form.Group>
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>

                    {!isCommissaire && (
                        <>
                            <Card className="mb-4 shadow-sm">
                                <Card.Body>
                                    <h5 className="text-primary fw-bold mb-3">2. Détails de l'évènement</h5>
                                    <Row className="mb-3">
                                        <Form.Group as={Col} md={8} controlId="eventName">
                                            <Form.Label className="fw-bold">Nom</Form.Label>
                                            <Form.Control name="name" value={formData.name} onChange={handleChange} required />
                                        </Form.Group>
                                        <Form.Group as={Col} md={4} controlId="eventType">
                                            <Form.Label className="fw-bold">Type</Form.Label>
                                            <Form.Select name="typeEventName" value={formData.typeEventName} onChange={handleChange}>
                                                <option value="MEETING">Réunion</option>
                                                <option value="TRAINING">Entraînement</option>
                                                <option value="TRIAL">Épreuve</option>
                                            </Form.Select>
                                        </Form.Group>
                                    </Row>
                                    {formData.typeEventName === 'TRIAL' && (
                                        <Row className="mb-3">
                                            <Form.Group as={Col} md={6}>
                                                <Form.Label className="fw-bold">👮 Commissaire Responsable</Form.Label>
                                                <Form.Select
                                                    name="commissaireId"
                                                    value={formData.commissaireId}
                                                    onChange={handleChange}
                                                    required={formData.typeEventName === 'TRIAL'}
                                                >
                                                    <option value="">-- Sélectionner un commissaire --</option>
                                                    {commissaires.map(c => (
                                                        <option key={c.id} value={c.id}>{c.name} {c.lastname} ({c.email})</option>
                                                    ))}
                                                </Form.Select>
                                            </Form.Group>
                                            <Form.Group as={Col} md={6}>
                                                <Form.Label className="fw-bold">Système de Score</Form.Label>
                                                <Form.Select name="typeScoreName" value={formData.typeScoreName} onChange={handleChange}>
                                                    <option value="TIME">Temps</option>
                                                    <option value="POINTS">Points</option>
                                                </Form.Select>
                                            </Form.Group>
                                        </Row>
                                    )}
                                    <Form.Group className="mb-3">
                                        <Form.Label className="fw-bold">Description</Form.Label>
                                        <Form.Control as="textarea" rows={2} name="description" value={formData.description} onChange={handleChange} />
                                    </Form.Group>
                                </Card.Body>
                            </Card>
                            <Card className="mb-4 shadow-sm">
                                <Card.Body>
                                    <h5 className="text-primary fw-bold mb-3">3. Lieu et Adresse</h5>
                                    <Form.Group className="mb-3">
                                        <Form.Label className="fw-bold">Nom du lieu</Form.Label>
                                        <Form.Control name="placeName" value={formData.placeName} onChange={handleChange} />
                                    </Form.Group>
                                    <Row className="mb-3">
                                        <Col md={3}>
                                            <Form.Group controlId="textNumeroRue">
                                                <Form.Label className="fw-bold">N°</Form.Label>
                                                <Form.Control name="number" value={formData.number} onChange={handleChange} required />
                                            </Form.Group>
                                        </Col>
                                        <Col md={9}>
                                            <Form.Group controlId="textNomRue">
                                                <Form.Label className="fw-bold">Rue</Form.Label>
                                                <Form.Control name="street" value={formData.street} onChange={handleChange} required />
                                            </Form.Group>
                                        </Col>
                                    </Row>
                                    <Row className="mb-3">
                                        <Col md={4}>
                                            <Form.Group controlId="textZip">
                                                <Form.Label className="fw-bold">CP</Form.Label>
                                                <Form.Control name="zip" value={formData.zip} onChange={handleChange} required />
                                            </Form.Group>
                                        </Col>
                                        <Col md={8}>
                                            <Form.Group controlId="textVille">
                                                <Form.Label className="fw-bold">Ville</Form.Label>
                                                <Form.Control name="city" value={formData.city} onChange={handleChange} required />
                                            </Form.Group>
                                        </Col>
                                    </Row>
                                    <Form.Group className="mb-3">
                                        <Form.Label className="fw-bold">Description du lieu</Form.Label>
                                        <Form.Control as="textarea" rows={2} name="descriptionPlace" value={formData.descriptionPlace} onChange={handleChange} />
                                    </Form.Group>
                                    <Form.Check type="switch" label="Parking disponible" name="parking" checked={formData.parking} onChange={handleChange} />
                                </Card.Body>
                            </Card>
                        </>
                    )}

                    <Button variant="secondary" size="lg" type="submit" className="w-100 shadow mb-5" disabled={submitting}>
                        {submitting ? <Spinner animation="border" size="sm" className="me-2" /> : null}
                        {submitting ? "Enregistrement..." : "Enregistrer les modifications"}
                    </Button>
                </Form>
            )}
        </Container>
    );
};

export default EditEventPage;