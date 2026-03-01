import React, { useState, useEffect } from 'react';
import { Form, Button, Row, Col, Card, Container, Alert, Spinner } from 'react-bootstrap';
import axios from 'axios';

const EditEventPage = () => {
    const [allEvents, setAllEvents] = useState([]);
    const [championships, setChampionships] = useState([]);
    const [competitions, setCompetitions] = useState([]);
    const [selectedEventId, setSelectedEventId] = useState('');
    const [loadingEvents, setLoadingEvents] = useState(true);
    const [loadingDetails, setLoadingDetails] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [status, setStatus] = useState({ type: '', message: '' });
    const [selectedChampionshipId, setSelectedChampionshipId] = useState('');
    const [validated, setValidated] = useState(false);

    const [compLimits, setCompLimits] = useState({ start: '', end: '' });

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
        parking: false,
        latitude: null,
        longitude: null
    });

    const formatDateTime = (dateStr) => {
        if (!dateStr) return '';
        return dateStr.length === 10 ? `${dateStr}T00:00` : dateStr.substring(0, 16);
    };

    useEffect(() => {
        const fetchInitialData = async () => {
            try {
                const [eventsRes, champsRes] = await Promise.all([
                    axios.get('http://localhost:8084/public/events'),
                    axios.get('http://localhost:8084/public/championship')
                ]);
                setAllEvents(eventsRes.data);
                setChampionships(champsRes.data);
                setLoadingEvents(false);
            } catch (err) {
                setLoadingEvents(false);
                setStatus({ type: 'danger', message: "Erreur serveur de chargement." });
            }
        };
        fetchInitialData();
    }, []);

    const handleEventSelection = async (e) => {
        const eventId = e.target.value;
        setSelectedEventId(eventId);
        if (!eventId) return;

        setLoadingDetails(true);
        setStatus({ type: '', message: '' });

        try {
            const eventRes = await axios.get(`http://localhost:8084/public/events/${eventId}`);
            const event = eventRes.data;

            for (const champ of championships) {
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
                        parking: event.place?.parking || false,
                        latitude: event.place?.latitude || null,
                        longitude: event.place?.longitude || null,
                    });
                    break;
                }
            }
        } catch (err) {
            console.error(err);
            setStatus({ type: 'danger', message: "Impossible de charger les détails de l'événement." });
        }
        setLoadingDetails(false);
    };

    const handleChampionshipChange = async (e) => {
        const champId = e.target.value;
        setSelectedChampionshipId(champId);
        if (champId) {
            const res = await axios.get(`http://localhost:8084/public/championship/${champId}/comp`);
            setCompetitions(res.data);
        }
    };

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        const val = type === 'checkbox' ? checked : value;
        setFormData(prev => ({ ...prev, [name]: val }));

        if (name === "competitionId") {
            const comp = competitions.find(c => c.id === parseInt(value));
            if (comp) {
                setCompLimits({
                    start: formatDateTime(comp.start),
                    end: formatDateTime(comp.end)
                });
            }
        }
    };

    const handleSubmit = async (e) => {
        const form = e.currentTarget;
        e.preventDefault();

        if (form.checkValidity() === false) {
            e.stopPropagation();
            setValidated(true);
            return;
        }

        setSubmitting(true);
        setStatus({ type: '', message: '' });

        const dataToSubmit = {
            id: parseInt(selectedEventId),
            name: formData.name,
            description: formData.description,
            typeEventName: formData.typeEventName,
            competitionId: parseInt(formData.competitionId),
            timeSlot: {
                start: formData.startTime.length === 16 ? formData.startTime + ":00" : formData.startTime,
                end: formData.endTime.length === 16 ? formData.endTime + ":00" : formData.endTime
            },
            place: {
                name: formData.placeName,
                description: formData.descriptionPlace,
                street: formData.street,
                number: formData.number,
                city: formData.city,
                zip: formData.zip,
                parking: formData.parking,
                latitude: formData.latitude,
                longitude: formData.longitude
            },
            rankings: []
        };

        try {
            await axios.put(
                `http://localhost:8084/admin/events/${selectedEventId}`,
                dataToSubmit
            );

            setStatus({ type: 'success', message: 'Évènement et lieu mis à jour avec succès !' });

            // Rafraîchir la liste globale
            const refreshRes = await axios.get('http://localhost:8084/public/events');
            setAllEvents(refreshRes.data);

        } catch (error) {
            console.error("Erreur PUT:", error.response?.data);
            setStatus({
                type: 'danger',
                message: error.response?.data?.message || "Erreur de sauvegarde. Vérifiez les dates de la compétition."
            });
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Container className="py-5">
            <h2 className="mb-4 text-center">⚙️ Modifier l'Évènement</h2>

            {status.message && <Alert variant={status.type} dismissible onClose={() => setStatus({type:'', message:''})}>{status.message}</Alert>}

            <Card className="shadow-sm mb-4">
                <Card.Body>
                    <Form.Group>
                        <Form.Label className="fw-bold text-primary">Sélectionner l'événement</Form.Label>
                        {loadingEvents ? <Spinner animation="border" size="sm" /> : (
                            <Form.Select value={selectedEventId} onChange={handleEventSelection}>
                                <option value="">--- Choisir un événement ---</option>
                                {allEvents.map(ev => <option key={ev.id} value={ev.id}>{ev.name} | {ev.competitionName}</option>)}
                            </Form.Select>
                        )}
                    </Form.Group>
                </Card.Body>
            </Card>

            {!loadingDetails && selectedEventId && (
                <Form noValidate validated={validated} onSubmit={handleSubmit}>

                    {/* SECTION DATES */}
                    <Card className="mb-4 shadow-sm">
                        <Card.Body>
                            <h5 className="text-primary mb-3">Contexte & Calendrier</h5>
                            <Row className="mb-3">
                                <Col md={6}>
                                    <Form.Label>Championnat</Form.Label>
                                    <Form.Select value={selectedChampionshipId} onChange={handleChampionshipChange}>
                                        {championships.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                    </Form.Select>
                                </Col>
                                <Col md={6}>
                                    <Form.Label>Compétition</Form.Label>
                                    <Form.Select name="competitionId" value={formData.competitionId} onChange={handleChange} required>
                                        {competitions.map(comp => <option key={comp.id} value={comp.id}>{comp.name}</option>)}
                                    </Form.Select>
                                </Col>
                            </Row>
                            <Row>
                                <Col md={6}>
                                    <Form.Label>Début (Min: {compLimits.start || 'N/A'})</Form.Label>
                                    <Form.Control type="datetime-local" name="startTime" value={formData.startTime} onChange={handleChange} min={compLimits.start} max={compLimits.end} required />
                                </Col>
                                <Col md={6}>
                                    <Form.Label>Fin (Max: {compLimits.end || 'N/A'})</Form.Label>
                                    <Form.Control type="datetime-local" name="endTime" value={formData.endTime} onChange={handleChange} min={formData.startTime} max={compLimits.end} required />
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>

                    {/* SECTION LIEU */}
                    <Card className="mb-4 shadow-sm">
                        <Card.Body>
                            <h5 className="text-primary mb-3">Informations & Lieu</h5>
                            <Row className="mb-3">
                                <Col md={8}>
                                    <Form.Label>Nom de l'évènement</Form.Label>
                                    <Form.Control name="name" value={formData.name} onChange={handleChange} required />
                                </Col>
                                <Col md={4}>
                                    <Form.Label>Type d'événement</Form.Label>
                                    <Form.Select name="typeEventName" value={formData.typeEventName} onChange={handleChange}>
                                        <option value="MEETING">MEETING</option>
                                        <option value="TRAINING">TRAINING</option>
                                        <option value="TRIAL">TRIAL</option>
                                    </Form.Select>
                                </Col>
                            </Row>
                            <Form.Group className="mb-3">
                                <Form.Label>Description de l'événement</Form.Label>
                                <Form.Control as="textarea" rows={2} name="description" value={formData.description} onChange={handleChange} />
                            </Form.Group>
                            <hr />
                            <Form.Group className="mb-3">
                                <Form.Label className="fw-bold">Nom du site (Place)</Form.Label>
                                <Form.Control name="placeName" value={formData.placeName} onChange={handleChange} required />
                            </Form.Group>
                            <Row className="mb-3">
                                <Col md={3}><Form.Control placeholder="N°" name="number" value={formData.number} onChange={handleChange} /></Col>
                                <Col md={9}><Form.Control placeholder="Rue" name="street" value={formData.street} onChange={handleChange} /></Col>
                            </Row>
                            <Row className="mb-3">
                                <Col md={4}><Form.Control placeholder="Zip" name="zip" value={formData.zip} onChange={handleChange} /></Col>
                                <Col md={8}><Form.Control placeholder="Ville" name="city" value={formData.city} onChange={handleChange} /></Col>
                            </Row>
                            <Form.Group className="mb-3">
                                <Form.Label>Description du lieu</Form.Label>
                                <Form.Control name="descriptionPlace" value={formData.descriptionPlace} onChange={handleChange} />
                            </Form.Group>
                            <Form.Check type="switch" label="Parking disponible" name="parking" checked={formData.parking} onChange={handleChange} />
                        </Card.Body>
                    </Card>

                    <Button variant="secondary" size="lg" type="submit" className="w-100 shadow" disabled={submitting}>
                        {submitting ? <Spinner animation="border" size="sm" className="me-2" /> : "Sauvegarder les modifications"}
                    </Button>
                </Form>
            )}
        </Container>
    );
};

export default EditEventPage;