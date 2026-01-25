import React, {useState, useEffect, useCallback, useRef, useMemo} from 'react';
import {useNavigate} from 'react-router-dom';
import {GoogleMap, useJsApiLoader} from '@react-google-maps/api';
import {fetchEventAndTrialsData} from '../../services/eventService';
import EventInfoWindow from './EventInfoWindow.jsx';
import ManualMarkerCluster from "./ManualMarkerCluster.jsx";
import {
    DEFAULT_CENTER,
    DEFAULT_ZOOM,
    GOOGLE_MAPS_OPTIONS,
    MAP_CONTAINER_STYLE
} from '../../constants/mapSettings';
import {Card, Col, Row, Spinner, Form, FloatingLabel} from "react-bootstrap";
import {formatDate} from "../../utils/dateFormatter.js";

const PublicMapPage = () => {
    const [events, setEvents] = useState([]);
    const [trials, setTrials] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [activeFilter, setActiveFilter] = useState('all');
    const [selectedDate, setSelectedDate] = useState('');
    const [selectedEvent, setSelectedEvent] = useState(null);
    const navigate = useNavigate();
    const mapRef = useRef(null);

    const {isLoaded, loadError} = useJsApiLoader({
        id: "google-map-script",
        googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || "AIzaSyA3efzW0xg7YQY9CbCSsJsFOp4On2daNPI",
        libraries: ["places"],
    });

    const allItems = useMemo(() => {
        return [...trials, ...events];
    }, [trials, events]);

    const itemsWithLocation = useMemo(() => {
        return allItems.filter(item =>
            item?.place?.latitude != null && item?.place?.longitude != null
        );
    }, [allItems]);

    useEffect(() => {
        fetchAllData();
    }, []);

    const fetchAllData = async () => {
        try {
            setLoading(true);
            const {events: basicEvents, trials: basicTrials} = await fetchEventAndTrialsData();

            const trialEventIds = new Set(
                basicTrials
                    .map(trial => trial.idEvent)
                    .filter(id => id != null)
            );

            const detailedEvents = await Promise.all(
                basicEvents.map(async (event) => {
                    try {
                        const response = await fetch(`http://localhost:8081/public/events/${event.id}`);
                        if (response.ok) {
                            const detailed = await response.json();
                            return {...detailed, _isTrial: false};
                        }
                        return {...event, _isTrial: false};
                    } catch {
                        return {...event, _isTrial: false};
                    }
                })
            );


            const detailedTrials = await Promise.all(
                basicTrials.map(async (basicTrial) => {
                    try {
                        const response = await fetch(`http://localhost:8081/public/trials/${basicTrial.id}`);
                        if (response.ok) {
                            const detailed = await response.json();
                            return {...detailed, idEvent: basicTrial.idEvent, _isTrial: true};
                        }
                        return {...basicTrial, _isTrial: true};
                    } catch {
                        return {...basicTrial, _isTrial: true};
                    }
                })
            );


            const nonTrialEvents = detailedEvents.filter(event => !trialEventIds.has(event.id));

            setEvents(nonTrialEvents);
            setTrials(detailedTrials);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const clearSelection = useCallback(() => {
        setSelectedEvent(null);
    }, []);

    const handleEventClick = (eventOrTrial) => {
        setSelectedEvent(eventOrTrial);

        if (eventOrTrial?.place?.latitude && eventOrTrial?.place?.longitude && mapRef.current) {
            mapRef.current.panTo({
                lat: eventOrTrial.place.latitude,
                lng: eventOrTrial.place.longitude
            });
            mapRef.current.setZoom(14);
        }
    };

    const handleEventHover = (event) => {
        if (event?.place?.latitude && event?.place?.longitude && mapRef.current) {
            mapRef.current.panTo({
                lat: event.place.latitude,
                lng: event.place.longitude
            });
        }
    };

    const handleMarkerClick = useCallback((eventOrTrial) => {
        if (eventOrTrial) {
            setSelectedEvent(eventOrTrial);
        }
    }, []);

    const handleViewDetails = useCallback((eventId) => {
        const isTrial = selectedEvent?._isTrial === true;

        if (isTrial) {
            navigate(`/public/trials/${eventId}`);
        } else {
            navigate(`/public/events/${eventId}`);
        }
    }, [navigate, selectedEvent]);

    const onMapLoad = useCallback((map) => {
        mapRef.current = map;

        if (itemsWithLocation.length > 0) {
            const bounds = new window.google.maps.LatLngBounds();
            itemsWithLocation.forEach(item => {
                if (item.place?.latitude && item.place?.longitude) {
                    bounds.extend({
                        lat: item.place.latitude,
                        lng: item.place.longitude
                    });
                }
            });

            setTimeout(() => {
                map.fitBounds(bounds);
                map.panToBounds(bounds);
            }, 1000);
        }
    }, [itemsWithLocation]);

    const matchesDateFilter = (item) => {
        if (!selectedDate) return true;

        const itemDate = item.timeSlot?.start || item.date || item.startDate;
        if (!itemDate) return true;

        const itemDateStr = new Date(itemDate).toISOString().split('T')[0];
        return itemDateStr === selectedDate;
    };

    const filteredTrials = trials.filter(trial => {
        const matchesSearch = trial.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            trial.description?.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesDate = matchesDateFilter(trial);
        return matchesSearch && matchesDate;
    });

    const filteredEvents = events.filter(event => {
        const matchesSearch = event.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            event.description?.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesDate = matchesDateFilter(event);
        return matchesSearch && matchesDate;
    });

    const getDisplayedItems = () => {
        switch (activeFilter) {
            case 'competition':
                return {trials: filteredTrials, events: []};
            case 'extra-competition':
                return {trials: [], events: filteredEvents};
            default:
                return {trials: filteredTrials, events: filteredEvents};
        }
    };

    const getFilteredEventsForMap = () => {
        const {trials: displayedTrials, events: displayedEvents} = getDisplayedItems();
        const allDisplayed = [...displayedTrials, ...displayedEvents];

        return allDisplayed.filter(item =>
            item?.place?.latitude != null && item?.place?.longitude != null
        );
    };

    const filteredEventsForMap = getFilteredEventsForMap();

    const {trials: displayedTrials, events: displayedEvents} = getDisplayedItems();

    if (loading) {
        return (
            <>
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">Chargement des événements...</span>
                </Spinner>
                <p>Chargement des événements...</p>
            </>

        );
    }

    if (error) {
        return (
            <p>Erreur: {error}</p>
        );
    }
    return (
        <>
            <Card className="m-3">
                <Card.Body>
                    <div className="d-flex gap-3 flex-column flex-md-row">
                        <FloatingLabel
                            label="📆 Filtrer par date">
                            <Form.Control
                                type="date"
                                value={selectedDate}
                                onChange={(e) => setSelectedDate(e.target.value)}
                            />
                        </FloatingLabel>
                        <FloatingLabel
                            label="🔍 Rechercher..."
                        >
                            <Form.Control
                                type="text"
                                placeholder="🔍 Rechercher..."
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                        </FloatingLabel>
                        <Card className="f-flex align-content-center">
                            <Card.Body>
                                <Form.Check
                                    inline
                                    label="Tous"
                                    type="radio"
                                    name="typeFilter"
                                    onClick={() => setActiveFilter('all')}
                                    defaultChecked
                                />
                                <Form.Check
                                    inline
                                    label="🏆 Compétition"
                                    type="radio"
                                    name="typeFilter"
                                    onClick={() => setActiveFilter('competition')}
                                />
                                <Form.Check
                                    inline
                                    label="📅 Extra-compétition"
                                    type="radio"
                                    name="typeFilter"
                                    onClick={() => setActiveFilter('extra-competition')}
                                />
                            </Card.Body>
                        </Card>
                        <Card>
                            <Card.Body>
                                <Form.Check
                                    inline
                                    label="🏅 Évènements passés"
                                    type="switch"
                                    name="typeFilter"
                                />
                            </Card.Body>
                        </Card>
                    </div>
                </Card.Body>
            </Card>
            <Row className="m-1">
                <Col sm="12" md="6">
                    <Card style={MAP_CONTAINER_STYLE} className="overflow-y-auto">
                        <Card.Body>
                            {displayedTrials.length > 0 && (
                                <>
                                    <div className="d-flex align-items-center flex-column">
                                        <span>🏆 Compétition</span>
                                        <hr style={{width: "21rem"}}/>
                                    </div>
                                    {displayedTrials.map(trial => (
                                        <Card
                                            key={`trial-${trial.id}`}
                                            onClick={() => handleEventClick(trial)}
                                            onMouseEnter={() => handleEventHover(trial)}
                                            style={{cursor: "pointer"}}
                                            className="mb-1"
                                        >
                                            <Card.Body className="text-center">
                                                <Card.Title>{trial.name}</Card.Title>
                                                <Card.Subtitle></Card.Subtitle>
                                                <Card.Text>
                                                    {trial.timeSlot?.start && (
                                                        <span
                                                            className="text-body-tertiary">{formatDate(trial.timeSlot.start, trial.timeSlot.end)}</span>
                                                    )}
                                                </Card.Text>
                                            </Card.Body>
                                        </Card>
                                    ))}
                                </>
                            )}
                            {displayedTrials.length > 0 && displayedEvents.length > 0 && (
                                <div className="pt-3">
                                    <hr/>
                                </div>
                            )}
                            {displayedEvents.length > 0 && (
                                <>
                                    <div className="d-flex align-items-center flex-column">
                                        <span>📅 Extra-compétition</span>
                                        <hr style={{width: "21rem"}}/>
                                    </div>
                                    {displayedEvents.map(event => (
                                        <Card
                                            key={`event-${event.id}`}
                                            onClick={() => handleEventClick(event)}
                                            onMouseEnter={() => handleEventHover(event)}
                                            style={{cursor: "pointer"}}
                                            className="mb-1"
                                        >
                                            <Card.Body className="text-center">
                                                <Card.Title>{event.name}</Card.Title>
                                                <Card.Subtitle></Card.Subtitle>
                                                <Card.Text>
                                                    {event.timeSlot?.start && (
                                                        <span
                                                            className="text-body-tertiary">{formatDate(event.timeSlot.start, event.timeSlot.end)}</span>
                                                    )}
                                                </Card.Text>
                                            </Card.Body>
                                        </Card>
                                    ))}
                                </>
                            )}
                        </Card.Body>
                    </Card>
                </Col>
                <Col sm="12" md="6">
                    {isLoaded && !loadError ? (
                        <GoogleMap
                            mapContainerStyle={MAP_CONTAINER_STYLE}
                            center={DEFAULT_CENTER}
                            zoom={DEFAULT_ZOOM}
                            options={{
                                ...GOOGLE_MAPS_OPTIONS,
                                mapTypeId: 'roadmap',
                            }}
                            onLoad={onMapLoad}
                        >
                            <ManualMarkerCluster
                                events={filteredEventsForMap}
                                onMarkerClick={handleMarkerClick}
                            />

                            {selectedEvent?.place && (
                                <EventInfoWindow
                                    event={selectedEvent}
                                    loading={false}
                                    onClose={clearSelection}
                                    onViewDetails={handleViewDetails}
                                />
                            )}
                        </GoogleMap>
                    ) : loadError ? (
                        <p>Erreur de chargement de la carte</p>
                    ) : (
                        <>
                            <Spinner animation="border" role="status">
                                <span className="visually-hidden">Chargement de la carte...</span>
                            </Spinner>
                            <p>Chargement de la carte...</p
                            ></>
                    )}
                </Col>
            </Row>
        </>
    );
};

export default PublicMapPage;