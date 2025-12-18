import { useState, useEffect, useCallback } from 'react';
import { eventService } from '../services/eventService';

// Hook pour la liste des événements
export const useEvents = () => {
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchEvents = useCallback(async () => {
        try {
            console.log('Starting to fetch events...');
            setLoading(true);
            setError(null);

            const data = await eventService.getAllWithDetails();

            setEvents(data);
        } catch (err) {
            const errorMsg = err.message || 'Failed to load events';
            console.error('Error in fetchEvents:', err);
            setError(errorMsg);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchEvents();
    }, [fetchEvents]);

    const refreshEvents = () => {
        fetchEvents();
    };

    const getEventsWithLocation = useCallback(() => {
        const eventsWithCoords = events.filter(event => {
            const hasCoords = event?.place?.latitude != null && event?.place?.longitude != null;
            return hasCoords;
        });

        return eventsWithCoords;
    }, [events]);

    return {
        events,
        eventsWithLocation: getEventsWithLocation(),
        loading,
        error,
        refreshEvents,
    };
};

// Hook pour les détails d'un événement
export const useEventDetails = () => {
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [loadingDetails, setLoadingDetails] = useState(false);

    const fetchEventDetails = useCallback(async (eventId) => {
        if (!eventId) return null;

        try {
            setLoadingDetails(true);
            const details = await eventService.getById(eventId);
            return details;
        } catch (error) {
            console.error('Error fetching event details:', error);
            throw error;
        } finally {
            setLoadingDetails(false);
        }
    }, []);

    const handleSelectEvent = useCallback(async (eventId, existingEvents = []) => {
        const existingEvent = existingEvents.find(e => e.id === eventId);
        if (existingEvent?.place) {
            setSelectedEvent(existingEvent);
            return;
        }

        try {
            setLoadingDetails(true);
            const details = await fetchEventDetails(eventId);
            if (details?.place) {
                setSelectedEvent(details);
            }
        } catch {
            // Keep existing selection or null
        }
    }, [fetchEventDetails]);

    const clearSelection = useCallback(() => {
        setSelectedEvent(null);
        setLoadingDetails(false);
    }, []);

    return {
        selectedEvent,
        loadingDetails,
        handleSelectEvent,
        clearSelection,
    };
};

// Export par défaut pour compatibilité
const useEventsHooks = {
    useEvents,
    useEventDetails
};

export default useEventsHooks;