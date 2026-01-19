import { useCallback, useState, useRef } from 'react';
import { GoogleMap, useJsApiLoader } from '@react-google-maps/api';
import { useEvents, useEventDetails } from '../../hooks/useEvents';
import ManualMarkerCluster from './ManualMarkerCluster';
import EventInfoWindow from './EventInfoWindow';
import {
    MAP_CONTAINER_STYLE,
    DEFAULT_CENTER,
    DEFAULT_ZOOM,
    GOOGLE_MAPS_OPTIONS,
} from '../../constants/mapSettings';
import {Card, Container, Nav, Navbar, NavDropdown} from "react-bootstrap";
import {Link} from "react-router-dom";

const PublicMapPage = () => {
    const { isLoaded, loadError } = useJsApiLoader({
        id: "google-map-script",
        googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || "AIzaSyA3efzW0xg7YQY9CbCSsJsFOp4On2daNPI",
        libraries: ["places"],
    });

    const {
        events,
        eventsWithLocation,
    } = useEvents();

    const {
        selectedEvent,
        loadingDetails,
        handleSelectEvent,
        clearSelection,
    } = useEventDetails();

    const [mapType] = useState('roadmap');
    const mapRef = useRef(null);

    const handleViewDetails = useCallback((eventId) => {
        window.location.href = `/public/events/${eventId}`;
    }, []);

    const handleMarkerClick = useCallback((eventId) => {
        handleSelectEvent(eventId, events);
    }, [handleSelectEvent, events]);

    const onMapLoad = useCallback((map) => {
        mapRef.current = map;

        if (eventsWithLocation.length > 0) {
            const bounds = new window.google.maps.LatLngBounds();
            eventsWithLocation.forEach(event => {
                if (event.place?.latitude && event.place?.longitude) {
                    bounds.extend({
                        lat: event.place.latitude,
                        lng: event.place.longitude
                    });
                }
            });

            setTimeout(() => {
                map.fitBounds(bounds);
                map.panToBounds(bounds);
            }, 1000);
        }
    }, [eventsWithLocation]);

    if (!isLoaded) {
        return "";
    }

    if (loadError) {
        return "";
    }

    return (
        <>
            <div className="d-flex justify-content-center">
                <Card className="mt-3" style={{ width: '38rem'}}>
                    <Card.Body>
                        <Card.Title as="h1" className="text-center">Carte des Évènements</Card.Title>
                        <Card.Subtitle as="h4" className="mb-2 text-body-secondary text-center">
                            Découvrez les évènements autour de vous
                        </Card.Subtitle>

                        <Card.Text>
                            <GoogleMap
                                mapContainerStyle={MAP_CONTAINER_STYLE}
                                center={DEFAULT_CENTER}
                                zoom={DEFAULT_ZOOM}
                                options={{
                                    ...GOOGLE_MAPS_OPTIONS,
                                    mapTypeId: mapType,
                                }}
                                onLoad={onMapLoad}
                            >
                                <ManualMarkerCluster
                                    events={eventsWithLocation}
                                    onMarkerClick={handleMarkerClick}
                                />

                                {selectedEvent && selectedEvent.place && (
                                    <EventInfoWindow
                                        event={selectedEvent}
                                        loading={loadingDetails}
                                        onClose={clearSelection}
                                        onViewDetails={handleViewDetails}
                                    />
                                )}
                            </GoogleMap>
                        </Card.Text>
                    </Card.Body>
                </Card>
            </div>
        </>
    );
};

export default PublicMapPage;