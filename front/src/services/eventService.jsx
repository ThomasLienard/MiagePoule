import axios from "axios";

const API_BASE_URL = `${import.meta.env.VITE_API_URL}/public`;

class EventService {
    async getAll() {
        const response = await fetch(`${API_BASE_URL}/events`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: Failed to fetch events`);
        }
        return response.json();
    }

    async getJustEvent() {
        const response = await fetch(`${API_BASE_URL}/otherEvent`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: Failed to fetch events`);
        }
        return response.json();
    }

    async getTrials() {
        const response = await fetch(`${API_BASE_URL}/trials`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: Failed to fetch events`);
        }
        return response.json();
    }

    async getTrialById(id) {
        if (!id) throw new Error('Trial ID is required');

        const response = await fetch(`${API_BASE_URL}/trials/${id}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: Failed to fetch trial ${id}`);
        }
        return response.json();
    }

    async getEventById(id) {
        if (!id) throw new Error('Event ID is required');

        const response = await fetch(`${API_BASE_URL}/events/${id}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: Failed to fetch event ${id}`);
        }
        return response.json();
    }

    async getAllWithDetails() {
        try {
            // Récupérer la liste basique des événements
            const basicEvents = await this.getAll();

            if (!basicEvents || basicEvents.length === 0) {
                return [];
            }

            console.log(`${basicEvents.length} basic events loaded from API`);

            // Pour chaque événement, charger ses détails complémentaires
            const detailedEvents = [];

            for (const basicEvent of basicEvents) {
                try {
                    const eventDetails = await this.getEventById(basicEvent.id);
                    detailedEvents.push(eventDetails);
                    console.log(`Loaded details for event ${basicEvent.id}: ${basicEvent.name}`);
                } catch (error) {
                    console.warn(`Failed to load details for event ${basicEvent.id}:`, error);
                    detailedEvents.push(basicEvent);
                }
            }

            console.log(`${detailedEvents.length} detailed events loaded`);

            return detailedEvents;
        } catch (error) {
            console.error('Error fetching events with details:', error);
            throw error;
        }
    }

    async getEventsByCompetition(championshipId, competitionId) {
        const response = await fetch(`${API_BASE_URL}/championships/${championshipId}/comp/${competitionId}/events`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: Failed to fetch events for competition`);
        }
        return response.json();
    }

    async getJustEventsByCompetition(championshipId, competitionId) {
        const response = await fetch(`${API_BASE_URL}/championships/${championshipId}/comp/${competitionId}/otherEvent`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: Failed to fetch events for competition`);
        }
        return response.json();
    }

    async getTrialsByCompetition(championshipId, competitionId) {
        const response = await fetch(`${API_BASE_URL}/championships/${championshipId}/comp/${competitionId}/trials`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: Failed to fetch trials for competition`);
        }
        return response.json();
    }

    async cancelEvent(eventId, data) {
        try {
            return await axios.patch(
                `${import.meta.env.VITE_API_URL}/commissaire/events/${eventId}/cancel`,
                data);
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la mise à jour');
        }
    }

    async createEvent(eventData) {
        return axios.post(`${import.meta.env.VITE_API_URL}/admin/events`, eventData);
    }
}

export const eventService = new EventService();