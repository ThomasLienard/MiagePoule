import PropTypes from 'prop-types';
import { Marker } from '@react-google-maps/api';

const EventMarker = ({
                         event,
                         onMarkerClick,
                         cluster = false,
                         clusterCount = 1
                     }) => {
    const { id, name, place } = event;

    if (!place?.latitude || !place?.longitude) {
        console.warn(`Event ${id} missing coordinates`);
        return null;
    }

    const position = {
        lat: place.latitude,
        lng: place.longitude,
    };

    const markerOptions = cluster
        ? {
            label: {
                text: clusterCount.toString(),
                color: "white",
                fontWeight: "bold",
                fontSize: "14px",
            },
            icon: {
                url: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png',
                scaledSize: new window.google.maps.Size(45, 45),
            }
        }
        : {
            animation: window.google.maps.Animation.DROP,
            icon: {
                url: 'http://maps.google.com/mapfiles/ms/icons/red-dot.png',
                scaledSize: new window.google.maps.Size(32, 32),
            }
        };

    return (
        <Marker
            position={position}
            onClick={() => onMarkerClick(id)}
            title={cluster ? `${clusterCount} événements - ${name}` : name}
            {...markerOptions}
        />
    );
};

EventMarker.propTypes = {
    event: PropTypes.shape({
        id: PropTypes.number.isRequired,
        name: PropTypes.string.isRequired,
        place: PropTypes.shape({
            latitude: PropTypes.number.isRequired,
            longitude: PropTypes.number.isRequired,
        }),
    }).isRequired,
    onMarkerClick: PropTypes.func.isRequired,
    cluster: PropTypes.bool,
    clusterCount: PropTypes.number,
};

EventMarker.defaultProps = {
    cluster: false,
    clusterCount: 1,
};

export default EventMarker;