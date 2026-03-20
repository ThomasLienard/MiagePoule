import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL;

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

const resultService = {
    /**
     * Récupère tous les résultats d'une épreuve
     */
    getTrialResults: async (trialId) => {
        try {
            const response = await axios.get(
                `${API_BASE_URL}/commissaire/trials/${trialId}/results`,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des résultats');
        }
    },

    /**
     * Saisit ou modifie le résultat d'un participant (athlète ou équipe)
     */
    setResult: async (trialId, participantId, participantType, result) => {
        try {
            const response = await axios.put(
                `${API_BASE_URL}/commissaire/trials/${trialId}/results`,
                { participantId, participantType, result },
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la saisie du résultat');
        }
    },

    /**
     * Modifie plusieurs résultats en une seule opération
     */
    setBulkResults: async (trialId, results) => {
        try {
            const response = await axios.put(
                `${API_BASE_URL}/commissaire/trials/${trialId}/results/bulk`,
                { results },
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la sauvegarde en masse des résultats');
        }
    },

    /**
     * Valide le résultat d'un participant individuellement
     */
    validateResult: async (trialId, participantId, participantType) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/commissaire/trials/${trialId}/results/validate`,
                { participantId, participantType },
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la validation du résultat');
        }
    },

    /**
     * Valide tous les résultats d'une épreuve d'un seul coup
     */
    validateAllResults: async (trialId) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/commissaire/trials/${trialId}/results/validate-all`,
                {},
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la validation de tous les résultats');
        }
    },

    /**
     * Invalide le résultat d'un participant
     */
    invalidateResult: async (trialId, participantId, participantType) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/commissaire/trials/${trialId}/results/invalidate`,
                { participantId, participantType },
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de l\'invalidation du résultat');
        }
    }
};

export default resultService;
