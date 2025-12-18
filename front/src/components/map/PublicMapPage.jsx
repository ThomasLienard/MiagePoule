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
import './PublicMapPage.css';

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
        <div className="public-map-page">
            <header className="page-header">
                <h1 className="page-title">Carte des Événements</h1>
                <p className="page-subtitle">
                    Découvrez les événements autour de vous
                </p>
            </header>

            <div className="map-container">
                <div className="map-wrapper">
                    <div className="map-controls-overlay">
                    </div>

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
                </div>
            </div>
        </div>
    );
};

export default PublicMapPage;