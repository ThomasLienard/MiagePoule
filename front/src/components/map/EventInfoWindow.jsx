import PropTypes from 'prop-types';
import { InfoWindow } from '@react-google-maps/api';
import { formatDate } from '../../utils/dateFormatter';
import './EventInfoWindow.css';
import {Button, Card, Spinner} from "react-bootstrap";

const EventInfoWindow = ({ event, loading, onClose, onViewDetails }) => {
    const { id, name, description, place, timeSlot } = event;

    if (!place) {
        console.warn(`Event ${id} missing place information`);
        return null;
    }

    const position = {
        lat: place.latitude,
        lng: place.longitude,
    };

    const handleOpenDirections = () => {
        const destination = `${place.latitude}, ${place.longitude}`;
        const url = `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(
            destination
        )}`;
        window.open(url, '_blank');
    };

    return (
        <InfoWindow
            position={position}
            onCloseClick={onClose}
        >
            <div className="event-info-window">
                {loading ? (
                    <Spinner animation="border" role="status">
                        <span className="visually-hidden">Loading details...</span>
                    </Spinner>
                ) : (
                    <Card>
                        <Card.Body>
                            <Card.Title>{name}</Card.Title>
                            <Card.Text>
                                <hr/>

                                {description && (
                                    <p className="fst-italic fw-bolder">{description}</p>
                                )}

                                <h6 className="text-center">Localisation</h6>
                                <p className="fw-bolder">{place.name}</p>
                                <p>
                                    {place.street} {place.number}
                                    <br />
                                    {place.zip} {place.city}
                                </p>
                                {place.parking && (
                                    <p className="fw-bolder">🚗 Parking disponible</p>
                                )}
                                {!place.parking && (
                                    <p className="fw-bolder">❌ Parking indisponible</p>
                                )}

                                {timeSlot?.start && (
                                    <div>
                                        <h6 className="text-center">Date & Heure</h6>
                                        <p>{formatDate(timeSlot.start, timeSlot.end)}</p>
                                    </div>
                                )}
                            </Card.Text>
                            <div className="d-flex justify-content-center">
                                <Button
                                    onClick={() => onViewDetails(id)}
                                    variant="secondary"
                                    aria-label={`Détails pour ${name}`}
                                >
                                    Détails
                                </Button>

                                <Button
                                    onClick={handleOpenDirections}
                                    variant="outline-secondary"
                                    aria-label={`Itinéraire vers ${name}`}
                                >
                                    Itinéraire
                                </Button>
                            </div>
                        </Card.Body>
                    </Card>
                )}
            </div>
        </InfoWindow>
    );
};


EventInfoWindow.propTypes = {
    event: PropTypes.shape({
        id: PropTypes.number.isRequired,
        name: PropTypes.string.isRequired,
        description: PropTypes.string,
        place: PropTypes.shape({
            latitude: PropTypes.number.isRequired,
            longitude: PropTypes.number.isRequired,
            name: PropTypes.string.isRequired,
            street: PropTypes.string.isRequired,
            number: PropTypes.string.isRequired,
            city: PropTypes.string.isRequired,
            zip: PropTypes.string.isRequired,
            parking: PropTypes.bool,
        }),
        timeSlot: PropTypes.shape({
            start: PropTypes.string,
            end: PropTypes.string,
        }),
    }).isRequired,
    loading: PropTypes.bool,
    onClose: PropTypes.func.isRequired,
    onViewDetails: PropTypes.func.isRequired,
};

EventInfoWindow.defaultProps = {
    loading: false,
};

export default EventInfoWindow;