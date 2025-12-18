const API_BASE_URL = 'http://localhost:8080';

/**
 * Récupère tous les événements publics
 */
export const fetchAllEvents = async () => {
    const response = await fetch(`${API_BASE_URL}/public/events`);
    if (!response.ok) throw new Error('Erreur chargement événements');
    return response.json();
};

/**
 * Récupère toutes les épreuves sportives
 */
export const fetchAllTrials = async () => {
    const response = await fetch(`${API_BASE_URL}/public/trials`);
    if (!response.ok) throw new Error('Erreur chargement épreuves');
    return response.json();
};

/**
 * Récupère les détails d'un événement
 * @param {number} id - L'ID de l'événement
 */
export const fetchEventDetails = async (id) => {
    const response = await fetch(`${API_BASE_URL}/public/events/${id}`);
    if (!response.ok) throw new Error('Événement non trouvé');
    return response.json();
};

/**
 * Récupère les détails d'une épreuve
 * @param {number} id - L'ID de l'épreuve
 */
export const fetchTrialDetails = async (id) => {
    const response = await fetch(`${API_BASE_URL}/public/trials/${id}`);
    if (!response.ok) throw new Error('Épreuve non trouvée');
    return response.json();
};

/**
 * Récupère les événements et les épreuves, puis filtre pour exclure les trials des events
 */
export const fetchEventAndTrialsData = async () => {
    try {
        const [eventsData, trialsData] = await Promise.all([
            fetchAllEvents(),
            fetchAllTrials()
        ]);

        const trialEventIds = new Set(trialsData.map(trial => trial.idEvent));

        const nonTrialEvents = eventsData.filter(event => !trialEventIds.has(event.id));

        return {
            events: nonTrialEvents,
            trials: trialsData
        };
    } catch (err) {
        console.error('Error fetching events and trials:', err);
        throw err;
    }
};
