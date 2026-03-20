import axios from "axios";

const API_BASE = `${import.meta.env.VITE_API_URL}/public`;

export const getCompetitionsByChampionship = (championshipId) => {
    return axios
        .get(`${API_BASE}/championship/${championshipId}/comp`)
        .then(res => res.data);
};

export const getCompetitionById = (championshipId, idComp) => {
    return axios
        .get(`${API_BASE}/championship/${championshipId}/comp/${idComp}`)
        .then(res => res.data);
};

export const subscribeToCompetition = (championshipId, competitionId, userId) => {
    return axios
        .post(`${API_BASE}/championship/${championshipId}/comp/${competitionId}/subscribe`, null, {
            params: { userId }
        })
        .then(res => res.data);
};

export const getObservers = (userId) => {
    return axios
        .get(`${import.meta.env.VITE_API_URL}/api/notifications/stream/observers`, {
            params: { userId }
        })
        .then(res => res.data)
        .catch(error => {
            // 404 = pas d'observer, retourne un tableau vide ou null
            if (error.response?.status === 404) {
                return [];
            }
            // Autres erreurs : relance pour que le composant gère
            throw error;
        });
};