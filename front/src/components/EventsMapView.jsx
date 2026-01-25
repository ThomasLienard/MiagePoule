import React, { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { GoogleMap, LoadScript } from '@react-google-maps/api';
import { fetchEventAndTrialsData } from '../services/eventService';
import ManualMarkerCluster from './map/ManualMarkerCluster';
import EventInfoWindow from './map/EventInfoWindow';
import {
    DEFAULT_CENTER,
    DEFAULT_ZOOM,
    GOOGLE_MAPS_OPTIONS,
} from '../constants/mapSettings';
import '../styles/EventsMapView.css';

const EventsMapView = () => {
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

    // Configuration Google Maps
    const GOOGLE_MAPS_API_KEY = "AIzaSyA3efzW0xg7YQY9CbCSsJsFOp4On2daNPI";
    const API_BASE_URL = 'http://localhost:8081';

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
            const { events: basicEvents, trials: basicTrials } = await fetchEventAndTrialsData();

            const trialEventIds = new Set(
                basicTrials
                    .map(trial => trial.idEvent)
                    .filter(id => id != null)
            );

            // Utilisez toujours le gateway (8081)
            const detailedEvents = await Promise.all(
                basicEvents.map(async (event) => {
                    try {
                        const response = await fetch(`${API_BASE_URL}/public/events/${event.id}`, {
                            headers: {
                                'Accept': 'application/json',
                                'Content-Type': 'application/json'
                            }
                        });
                        if (response.ok) {
                            const detailed = await response.json();
                            return { ...detailed, _isTrial: false };
                        }
                        return { ...event, _isTrial: false };
                    } catch {
                        return { ...event, _isTrial: false };
                    }
                })
            );

            // Utilisez toujours le gateway (8081)
            const detailedTrials = await Promise.all(
                basicTrials.map(async (basicTrial) => {
                    try {
                        const response = await fetch(`${API_BASE_URL}/public/trials/${basicTrial.id}`, {
                            headers: {
                                'Accept': 'application/json',
                                'Content-Type': 'application/json'
                            }
                        });
                        if (response.ok) {
                            const detailed = await response.json();
                            return { ...detailed, idEvent: basicTrial.idEvent, _isTrial: true };
                        }
                        return { ...basicTrial, _isTrial: true };
                    } catch {
                        return { ...basicTrial, _isTrial: true };
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
            navigate(`${API_BASE_URL}/public/trials/${eventId}`);
        } else {
            navigate(`${API_BASE_URL}/public/events/${eventId}`);
        }
    }, [navigate, selectedEvent, API_BASE_URL]);

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
                return { trials: filteredTrials, events: [] };
            case 'extra-competition':
                return { trials: [], events: filteredEvents };
            default:
                return { trials: filteredTrials, events: filteredEvents };
        }
    };

    const getFilteredEventsForMap = () => {
        const { trials: displayedTrials, events: displayedEvents } = getDisplayedItems();
        const allDisplayed = [...displayedTrials, ...displayedEvents];

        return allDisplayed.filter(item =>
            item?.place?.latitude != null && item?.place?.longitude != null
        );
    };

    const filteredEventsForMap = getFilteredEventsForMap();

    const { trials: displayedTrials, events: displayedEvents } = getDisplayedItems();
    const totalResults = displayedTrials.length + displayedEvents.length;

    if (loading) {
        return (
            <div className="events-map-view">
                <div className="loading-overlay">
                    <div className="loading-spinner"></div>
                    <p>Chargement des événements...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="events-map-view">
                <div className="error-overlay">
                    <p>Erreur: {error}</p>
                </div>
            </div>
        );
    }

    return (
        <div className="events-map-view">
            <aside className="events-sidebar">
                <div className="search-section">
                    <div className="search-box">
                        <span className="search-icon">🔍</span>
                        <input
                            type="text"
                            placeholder="Rechercher un événement..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="search-input"
                        />
                        {searchTerm && (
                            <button
                                className="clear-search"
                                onClick={() => setSearchTerm('')}
                            >
                                ✕
                            </button>
                        )}
                    </div>
                </div>

                <div className="filter-section">
                    <button
                        className={`filter-btn ${activeFilter === 'all' ? 'active' : ''}`}
                        onClick={() => setActiveFilter('all')}
                    >
                        Tous
                    </button>
                    <button
                        className={`filter-btn ${activeFilter === 'competition' ? 'active' : ''}`}
                        onClick={() => setActiveFilter('competition')}
                    >
                        🏆 Compétition
                    </button>
                    <button
                        className={`filter-btn ${activeFilter === 'extra-competition' ? 'active' : ''}`}
                        onClick={() => setActiveFilter('extra-competition')}
                    >
                        📅 Extra-compétition
                    </button>
                </div>

                <div className="date-filter-section">
                    <div className="date-filter-wrapper">
                        <label htmlFor="date-filter" className="date-filter-label">
                            📆 Filtrer par date :
                        </label>
                        <input
                            type="date"
                            id="date-filter"
                            className="date-filter-input"
                            value={selectedDate}
                            onChange={(e) => setSelectedDate(e.target.value)}
                        />
                        {selectedDate && (
                            <button
                                className="clear-date-btn"
                                onClick={() => setSelectedDate('')}
                                title="Effacer le filtre date"
                            >
                                ✕
                            </button>
                        )}
                    </div>
                </div>

                <div className="results-header">
                    <span className="results-count">{totalResults} résultat{totalResults > 1 ? 's' : ''}</span>
                </div>

                <div className="events-list">
                    {displayedTrials.length > 0 && (
                        <>
                            {activeFilter === 'all' && (
                                <div className="section-divider">
                                    <span>🏆 Compétition</span>
                                </div>
                            )}
                            {displayedTrials.map(trial => (
                                <div
                                    key={`trial-${trial.id}`}
                                    className="event-item trial-item"
                                    onClick={() => handleEventClick(trial)}
                                    onMouseEnter={() => handleEventHover(trial)}
                                >
                                    <div className="event-item-content">
                                        <div className="event-item-badge">Compétition</div>
                                        <h3 className="event-item-title">{trial.name}</h3>
                                        <p className="event-item-description">
                                            {trial.description || 'Aucune description'}
                                        </p>
                                        {trial.place?.address && (
                                            <p className="event-item-location">
                                                📍 {trial.place.address}
                                            </p>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </>
                    )}

                    {displayedEvents.length > 0 && (
                        <>
                            {activeFilter === 'all' && (
                                <div className="section-divider">
                                    <span>📅 Extra-compétition</span>
                                </div>
                            )}
                            {displayedEvents.map(event => (
                                <div
                                    key={`event-${event.id}`}
                                    className="event-item"
                                    onClick={() => handleEventClick(event)}
                                    onMouseEnter={() => handleEventHover(event)}
                                >
                                    <div className="event-item-content">
                                        <h3 className="event-item-title">{event.name}</h3>
                                        <p className="event-item-description">
                                            {event.description || 'Aucune description'}
                                        </p>
                                        {event.place?.address && (
                                            <p className="event-item-location">
                                                📍 {event.place.address}
                                            </p>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </>
                    )}

                    {totalResults === 0 && (
                        <div className="no-results">
                            <p>Aucun événement trouvé</p>
                            {(searchTerm || selectedDate || activeFilter !== 'all') && (
                                <button
                                    className="clear-filters-btn"
                                    onClick={() => {
                                        setSearchTerm('');
                                        setActiveFilter('all');
                                        setSelectedDate('');
                                    }}
                                >
                                    Effacer tous les filtres
                                </button>
                            )}
                        </div>
                    )}
                </div>
            </aside>

            <main className="map-section">
                {GOOGLE_MAPS_API_KEY ? (
                    <LoadScript
                        googleMapsApiKey={GOOGLE_MAPS_API_KEY}
                        libraries={["places"]}
                        onError={(error) => console.error("Google Maps error:", error)}
                        onLoad={() => console.log("Google Maps loaded")}
                    >
                        <GoogleMap
                            mapContainerClassName="google-map-container"
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
                    </LoadScript>
                ) : (
                    <div className="map-error">
                        <p>Clé API Google Maps manquante</p>
                        <p>Veuillez définir VITE_GOOGLE_MAPS_API_KEY dans votre fichier .env</p>
                    </div>
                )}
            </main>
        </div>
    );
};

export default EventsMapView;