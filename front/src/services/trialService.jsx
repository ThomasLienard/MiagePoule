import axios from "axios";

const API_BASE = `${import.meta.env.VITE_API_URL}/public`;

export const getTrial = (trialId) => {
    return axios.get(`${API_BASE}/trials/${trialId}`);
};
