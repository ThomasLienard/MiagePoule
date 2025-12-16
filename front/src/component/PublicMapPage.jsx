// PublicMapPage.jsx
import { GoogleMap, Marker, InfoWindow, useJsApiLoader } from '@react-google-maps/api';
import { useEffect, useState } from 'react';

const containerStyle = { width: '100%', height: '600px' };
const defaultCenter = { lat: 46.5, lng: 2.5 };

function PublicMapPage() {
    const [events, setEvents] = useState([]);
    const [selectedEvent, setSelectedEvent] = useState(null);

    // Appel via l’API Gateway : même origin que le front, chemin /public/map
    useEffect(() => {
        const fetchEvents = () => {
            fetch('/public/map')   // <-- passe par la gateway
                .then(res => res.json())
                .then(data => setEvents(data));
        };

        fetchEvents();
        const interval = setInterval(fetchEvents, 30000);
        return () => clearInterval(interval);
    }, []);

    const { isLoaded } = useJsApiLoader({
        id: 'google-map-script',
        googleMapsApiKey: 'AIzaSyA3efzW0xg7YQY9CbCSsJsFOp4On2daNPI',
    });

    if (!isLoaded) return <div>Chargement de la carte...</div>;

    return (
        <GoogleMap
            mapContainerStyle={containerStyle}
            center={defaultCenter}
            zoom={6}
        >
            {events.map(evt => (
                <Marker
                    key={evt.id}
                    position={{ lat: Number(evt.latitude), lng: Number(evt.longitude) }}
                    onClick={() => setSelectedEvent(evt)}
                />
            ))}

            {selectedEvent && (
                <InfoWindow
                    position={{
                        lat: Number(selectedEvent.latitude),
                        lng: Number(selectedEvent.longitude),
                    }}
                    onCloseClick={() => setSelectedEvent(null)}
                >
                    <div>
                        <h3>{selectedEvent.eventName}</h3>
                        <p>Compétition : {selectedEvent.competitionName}</p>
                        <p>Lieu : {selectedEvent.placeName}, {selectedEvent.street}, {selectedEvent.city}</p>
                        <p>Horaire : {selectedEvent.startTime} - {selectedEvent.endTime}</p>
                    </div>
                </InfoWindow>
            )}
        </GoogleMap>
    );
}

export default PublicMapPage;
