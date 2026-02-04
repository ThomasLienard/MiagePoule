import axios from 'axios';

const API_BASE_URL = 'http://localhost:8084';

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

const participantService = {
    /**
     * Récupère toutes les épreuves avec leurs participants
     */
    getAllTrials: async () => {
        try {
            const response = await axios.get(
                `${API_BASE_URL}/commissaire/trials`,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des épreuves');
        }
    },

    /**
     * Récupère les participants d'une épreuve spécifique
     */
    getTrialParticipants: async (trialId) => {
        try {
            const response = await axios.get(
                `${API_BASE_URL}/commissaire/trials/${trialId}/participants`,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des participants');
        }
    },

    /**
     * Récupère les participants avec tous les potentiels (athlètes ET équipes)
     */
    getTrialParticipantsFull: async (trialId) => {
        try {
            const response = await axios.get(
                `${API_BASE_URL}/commissaire/trials/${trialId}/participants/full`,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des participants');
        }
    },

    /**
     * Inscrit un athlète à une épreuve
     */
    addAthlete: async (trialId, athleteId) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/commissaire/trials/${trialId}/participants`,
                {
                    participantId: athleteId,
                    participantType: 'ATHLETE'
                },
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de l\'inscription de l\'athlète');
        }
    },

    /**
     * Inscrit une équipe à une épreuve
     */
    addTeam: async (trialId, teamId) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/commissaire/trials/${trialId}/participants`,
                {
                    participantId: teamId,
                    participantType: 'TEAM'
                },
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de l\'inscription de l\'équipe');
        }
    },

    /**
     * Déclare un athlète forfait
     */
    forfeitAthlete: async (trialId, athleteId) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/commissaire/trials/${trialId}/forfeit`,
                {
                    participantId: athleteId,
                    participantType: 'ATHLETE'
                },
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la déclaration de forfait');
        }
    },

    /**
     * Déclare une équipe forfait
     */
    forfeitTeam: async (trialId, teamId) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/commissaire/trials/${trialId}/forfeit`,
                {
                    participantId: teamId,
                    participantType: 'TEAM'
                },
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la déclaration de forfait');
        }
    },

    /**
     * Annule le forfait d'un athlète
     */
    unforfeitAthlete: async (trialId, athleteId) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/commissaire/trials/${trialId}/unforfeit`,
                {
                    participantId: athleteId,
                    participantType: 'ATHLETE'
                },
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de l\'annulation du forfait');
        }
    },

    /**
     * Annule le forfait d'une équipe
     */
    unforfeitTeam: async (trialId, teamId) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/commissaire/trials/${trialId}/unforfeit`,
                {
                    participantId: teamId,
                    participantType: 'TEAM'
                },
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de l\'annulation du forfait');
        }
    }
};

export default participantService;
