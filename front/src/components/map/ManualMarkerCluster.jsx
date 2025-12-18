import { useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import EventMarker from './EventMarker';

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
                <div style={{
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
                }}>
                    <h3 style={{ margin: '0 0 15px 0', color: '#333' }}>
                        {selectedGroup.events.length} événements à cet endroit
                    </h3>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                        {selectedGroup.events.map(event => (
                            <div
                                key={event.id}
                                style={{
                                    padding: '10px',
                                    border: '1px solid #ddd',
                                    borderRadius: '5px',
                                    cursor: 'pointer',
                                    transition: 'all 0.2s',
                                    backgroundColor: '#f8f9fa'
                                }}
                                onClick={() => {
                                    onMarkerClick(event.id);
                                    setSelectedGroup(null);
                                }}
                                onMouseEnter={e => e.currentTarget.style.backgroundColor = '#e9ecef'}
                                onMouseLeave={e => e.currentTarget.style.backgroundColor = '#f8f9fa'}
                            >
                                <div style={{ fontWeight: 'bold', marginBottom: '5px', color:'black' }}>
                                    {event.name}
                                </div>
                                {event.description && (
                                    <div style={{ fontSize: '0.9em', color: '#666' }}>
                                        {event.description.substring(0, 50)}...
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                    <button
                        onClick={() => setSelectedGroup(null)}
                        style={{
                            marginTop: '15px',
                            padding: '8px 16px',
                            backgroundColor: '#6c757d',
                            color: 'white',
                            border: 'none',
                            borderRadius: '5px',
                            cursor: 'pointer',
                            width: '100%'
                        }}
                    >
                        Fermer
                    </button>
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