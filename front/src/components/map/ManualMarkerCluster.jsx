import { useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import EventMarker from './EventMarker';
import {Button} from "react-bootstrap";

const ManualMarkerCluster = ({ events, onMarkerClick }) => {
    const [selectedGroup, setSelectedGroup] = useState(null);

    const groupedEvents = useMemo(() => {
        const groups = new Map();

        events.forEach(event => {
            if (!event.place?.latitude || !event.place?.longitude) return;


            const key = `${event.place.latitude.toFixed(5)}_${event.place.longitude.toFixed(5)}`;

            if (!groups.has(key)) {
                groups.set(key, {
                    position: {
                        lat: event.place.latitude,
                        lng: event.place.longitude
                    },
                    events: []
                });
            }
            groups.get(key).events.push(event);
        });

        return Array.from(groups.values());
    }, [events]);

    const handleGroupClick = (group) => {
        if (group.events.length === 1) {
            onMarkerClick(group.events[0].id);
        } else {
            setSelectedGroup(group);
        }
    };

    return (
        <>

            {groupedEvents.map((group, index) => (
                <EventMarker
                    key={`group-${index}`}
                    event={group.events[0]}
                    onMarkerClick={() => handleGroupClick(group)}
                    cluster={group.events.length > 1}
                    clusterCount={group.events.length}
                />
            ))}

            {selectedGroup && (
                <div
                    style={{
                    position: 'absolute',
                    top: '50%',
                    left: '50%',
                    transform: 'translate(-50%, -50%)',
                    zIndex: 1000,
                    backgroundColor: 'white',
                    padding: '20px',
                    borderRadius: '10px',
                    boxShadow: '0 4px 20px rgba(0,0,0,0.2)',
                    minWidth: '300px',
                    maxHeight: '400px',
                    overflowY: 'auto'
                }}
                    className="d-flex flex-column gap-2"
                >
                    <h3 className="text-center">
                        {selectedGroup.events.length} évènements à cet endroit
                    </h3>
                    <div className="d-flex flex-column gap-2">
                        {selectedGroup.events.map(event => (
                            <Button
                                key={`${event.id}-${event._isTrial ? 'trial' : 'event'}`}
                                size="lg"
                                variant="outline-secondary"
                                onClick={() => {
                                    onMarkerClick(event);
                                    setSelectedGroup(null);
                                }}
                                onMouseEnter={e => e.currentTarget.style.backgroundColor = '#e9ecef'}
                                onMouseLeave={e => e.currentTarget.style.backgroundColor = '#f8f9fa'}
                            >
                                <div className="fw-semibold text-black">
                                    {event.name}
                                </div>
                                {event.description && (
                                    <div className="text-body-secondary">
                                        {event.description.substring(0, 50)}...
                                    </div>
                                )}
                            </Button>
                        ))}
                    </div>
                    <Button
                        onClick={() => setSelectedGroup(null)}
                        variant="secondary"
                        className="text-center"
                    >
                        Fermer
                    </Button>
                </div>
            )}
        </>
    );
};

ManualMarkerCluster.propTypes = {
    events: PropTypes.array.isRequired,
    onMarkerClick: PropTypes.func.isRequired,
};

export default ManualMarkerCluster;