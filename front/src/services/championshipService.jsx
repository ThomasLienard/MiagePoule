import axios from "axios";

const API_BASE = `${import.meta.env.VITE_API_URL}/public`;

export const getChampionships = () => {
    return axios.get(`${API_BASE}/championship`).then(res => res.data);
};

export const getChampionshipById = (id) => {
    return axios.get(`${API_BASE}/championship/${id}`).then(res => res.data);
};
