import axios from "axios";

const API_BASE = "http://localhost:8084/public";

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
