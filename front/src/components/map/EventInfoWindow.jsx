import PropTypes from 'prop-types';
import { InfoWindow } from '@react-google-maps/api';
import { formatDate } from '../../utils/dateFormatter';
import './EventInfoWindow.css';

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

    return (
        <InfoWindow
            position={position}
            onCloseClick={onClose}
        >
            <div className="event-info-window">
                {loading ? (
                    <div className="loading-indicator">
                        <div className="spinner"></div>
                        <p>Loading details...</p>
                    </div>
                ) : (
                    <>
                        <h3 className="event-title">{name}</h3>

                        {description && (
                            <p className="event-description">{description}</p>
                        )}

                        <div className="event-location">
                            <h4 className="section-title">Location</h4>
                            <p className="location-name">{place.name}</p>
                            <p className="location-address">
                                {place.street} {place.number}
                                <br />
                                {place.zip} {place.city}
                            </p>
                            {place.parking && (
                                <p className="parking-available">🚗 Parking available</p>
                            )}
                        </div>

                        {timeSlot?.start && (
                            <div className="event-time">
                                <h4 className="section-title">Date & Time</h4>
                                <p className="location-address">{formatDate(timeSlot.start, timeSlot.end)}</p>
                            </div>
                        )}

                        <div className="action-buttons">
                            <button
                                onClick={() => onViewDetails(id)}
                                className="btn-view-details"
                                aria-label={`View full details for ${name}`}
                            >
                                View Full Details
                            </button>
                        </div>
                    </>
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