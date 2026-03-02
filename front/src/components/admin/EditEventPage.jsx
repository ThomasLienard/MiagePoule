import React, { useState, useEffect, useCallback } from 'react';
import { Form, Button, Row, Col, Card, Container, Alert, Spinner } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';

const EditEventPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const queryParams = new URLSearchParams(location.search);
    const eventIdFromUrl = queryParams.get('id');

    const [allEvents, setAllEvents] = useState([]);
    const [championships, setChampionships] = useState([]);
    const [competitions, setCompetitions] = useState([]);
    const [selectedEventId, setSelectedEventId] = useState(eventIdFromUrl || '');
    const [loadingInitial, setLoadingInitial] = useState(true);
    const [loadingDetails, setLoadingDetails] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [status, setStatus] = useState({ type: '', message: '' });
    const [selectedChampionshipId, setSelectedChampionshipId] = useState('');
    const [compLimits, setCompLimits] = useState({ start: '', end: '' });

    // --- CORRECTION LOGIQUE RÔLE ---
    // On parse l'objet "user" (ou la clé qui contient ton JSON dans le localStorage)
    const storedUser = JSON.parse(localStorage.getItem('user')) || {};
    const isCommissaire = storedUser.roles?.includes('COMMISSAIRE');
    const userRoleDisplay = storedUser.roles?.join(', ') || 'INVITÉ';
    // -------------------------------

    const [formData, setFormData] = useState({
        name: '',
        description: '',
        typeEventName: 'MEETING',
        competitionId: '',
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
            const eventRes = await axios.get(`http://localhost:8084/public/events/${eventId}`);
            const event = eventRes.data;

            for (const champ of currentChamps) {
                const compsRes = await axios.get(`http://localhost:8084/public/championship/${champ.id}/comp`);
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
                        typeEventName: event.typeEventName || 'MEETING',
                        competitionId: match.id,
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
            setStatus({ type: 'danger', message: "Erreur de chargement." });
        } finally {
            setLoadingDetails(false);
        }
    }, []);

    useEffect(() => {
        const initPage = async () => {
            try {
                const [champsRes, eventsRes] = await Promise.all([
                    axios.get('http://localhost:8084/public/championship'),
                    axios.get('http://localhost:8084/public/events')
                ]);
                setChampionships(champsRes.data);
                setAllEvents(eventsRes.data);

                if (eventIdFromUrl) {
                    await fetchEventDetails(eventIdFromUrl, champsRes.data);
                }
            } catch (err) {
                setStatus({ type: 'danger', message: "Erreur serveur." });
            } finally {
                setLoadingInitial(false);
            }
        };
        initPage();
    }, [eventIdFromUrl, fetchEventDetails]);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        // BLOQUAGE STRICT : Le commissaire ne peut changer QUE les dates.
        if (isCommissaire && !["startTime", "endTime"].includes(name)) return;

        const val = type === 'checkbox' ? checked : value;
        setFormData(prev => ({ ...prev, [name]: val }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        const finalId = parseInt(selectedEventId || eventIdFromUrl);

        const dataToSubmit = {
            id: finalId,
            // On envoie tout, le backend gérera.
            // Mais le commissaire n'aura modifié que les dates dans le formulaire.
            name: formData.name,
            competitionId: parseInt(formData.competitionId),
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
            }
        };

        // CHOIX DE L'URL SELON LE RÔLE
        const url = isCommissaire
            ? `http://localhost:8084/commissaire/events/${finalId}`
            : `http://localhost:8084/admin/events/${finalId}`;

        try {
            await axios.put(url, dataToSubmit);
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

            {/* Sélecteur masqué pour le commissaire */}
            {!isCommissaire && (
                <Card className="shadow-sm mb-4 bg-light">
                    <Card.Body>
                        <Form.Label className="fw-bold">Épreuve à gérer</Form.Label>
                        <Form.Select value={selectedEventId} onChange={(e) => {
                            setSelectedEventId(e.target.value);
                            fetchEventDetails(e.target.value, championships);
                        }}>
                            <option value="">--- Choisir ---</option>
                            {allEvents.map(ev => <option key={ev.id} value={ev.id}>{ev.name} ({ev.competitionName})</option>)}
                        </Form.Select>
                    </Card.Body>
                </Card>
            )}

            {loadingDetails ? (
                <div className="text-center py-5"><Spinner animation="grow" /></div>
            ) : (selectedEventId || eventIdFromUrl) && (
                <Form onSubmit={handleSubmit}>

                    <Card className="mb-4 shadow-sm border-primary">
                        <Card.Body>
                            <h5 className="text-primary mb-3">Dates de l'épreuve : {formData.name}</h5>

                            {!isCommissaire && (
                                <Row className="mb-3">
                                    <Col md={6}>
                                        <Form.Label>Championnat</Form.Label>
                                        <Form.Select
                                            value={selectedChampionshipId}
                                            onChange={async (e) => {
                                                const champId = e.target.value;
                                                setSelectedChampionshipId(champId);
                                                const res = await axios.get(`http://localhost:8084/public/championship/${champId}/comp`);
                                                setCompetitions(res.data);
                                            }}
                                        >
                                            {championships.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                        </Form.Select>
                                    </Col>
                                    <Col md={6}>
                                        <Form.Label>Compétition</Form.Label>
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
                                    </Col>
                                </Row>
                            )}

                            <Row>
                                <Col md={6} className="mb-3">
                                    <Form.Label className="fw-bold">Début</Form.Label>
                                    <Form.Control type="datetime-local" name="startTime" value={formData.startTime} onChange={handleChange} min={compLimits.start} max={compLimits.end} required />
                                </Col>
                                <Col md={6} className="mb-3">
                                    <Form.Label className="fw-bold">Fin</Form.Label>
                                    <Form.Control type="datetime-local" name="endTime" value={formData.endTime} onChange={handleChange} min={formData.startTime} max={compLimits.end} required />
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>

                    {!isCommissaire && (
                        <Card className="mb-4 shadow-sm">
                            <Card.Body>
                                <h5 className="text-muted mb-3">📍 Informations & Lieu</h5>
                                <Form.Group className="mb-3">
                                    <Form.Label>Nom</Form.Label>
                                    <Form.Control name="name" value={formData.name} onChange={handleChange} />
                                </Form.Group>
                                <Row className="mb-3">
                                    <Col md={3}><Form.Label>N° Rue</Form.Label><Form.Control name="number" value={formData.number} onChange={handleChange} /></Col>
                                    <Col md={9}><Form.Label>Rue</Form.Label><Form.Control name="street" value={formData.street} onChange={handleChange} /></Col>
                                </Row>
                                <Row className="mb-3">
                                    <Col md={4}><Form.Label>CP</Form.Label><Form.Control name="zip" value={formData.zip} onChange={handleChange} /></Col>
                                    <Col md={8}><Form.Label>Ville</Form.Label><Form.Control name="city" value={formData.city} onChange={handleChange} /></Col>
                                </Row>
                                <Form.Check type="switch" label="Parking" name="parking" checked={formData.parking} onChange={handleChange} />
                            </Card.Body>
                        </Card>
                    )}

                    <Button variant="secondary" size="lg" type="submit" className="w-100 shadow" disabled={submitting}>
                        {submitting ? "Enregistrement..." : "Enregistrer les modifications"}
                    </Button>
                </Form>
            )}
        </Container>
    );
};

export default EditEventPage;