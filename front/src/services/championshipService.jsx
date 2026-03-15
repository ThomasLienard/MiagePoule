import axios from "axios";

const API_BASE = "http://localhost:8084/public";

export const getChampionships = () => {
    return axios.get(`${API_BASE}/championship`).then(res => res.data);
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
