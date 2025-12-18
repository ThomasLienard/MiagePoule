const API_BASE_URL = "http://localhost:8083/public";

class EventService {
    async getAll() {
        const response = await fetch(`${API_BASE_URL}/events`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: Failed to fetch events`);
        }
        return response.json();
    }

    async getById(id) {
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
                    const eventDetails = await this.getById(basicEvent.id);
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
}

export const eventService = new EventService();