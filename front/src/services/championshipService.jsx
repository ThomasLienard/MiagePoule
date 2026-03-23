import axios from "axios";

const API_BASE = `${import.meta.env.VITE_API_URL}/public`;

export const getChampionships = () => {
    return axios.get(`${API_BASE}/championship`)
        .then(res => res.data)
        .catch(err => console.error("Erreur championnats", err));
};

export const getChampionshipById = (id) => {
    return axios.get(`${API_BASE}/championship/${id}`).then(res => res.data);
};

export const getAllCompetitions = async () => {
    try {
        const championships = await getChampionships();
        const allComps = [];
        for (const champ of championships) {
            const comps = await axios.get(`${API_BASE}/championship/${champ.id}/comp`);
            comps.data.forEach(comp => {
                allComps.push({
                    ...comp,
                    championshipName: champ.name
                });
            });
        }
        return allComps;
    } catch (err) {
        console.warn('Impossible de récupérer les compétitions', err);
        throw err;
    }
};
export const getChampionshipCompetition = (championshipId) => {
    return axios.get(`${import.meta.env.VITE_API_URL}/public/championship/${championshipId}/comp`);
};

export const createChampionship = (data) => {
    return axios.post(`${import.meta.env.VITE_API_URL}/admin/champs`, data);
};

export const updateChampionship = (championshipId, data) => {
    return axios.put(`${import.meta.env.VITE_API_URL}/admin/champs/${championshipId}`, data);
};
