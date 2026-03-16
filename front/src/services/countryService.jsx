import axios from "axios";

const API_BASE = import.meta.env.VITE_API_URL;

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

export const getAllCountries = () => {
    // Retourne les codes pays et les transforme en objets { code: "XX" } pour compatibilité avec les composants
    return axios.get(`${API_BASE}/countries`, { headers: getAuthHeaders() })
        .then(res => res.data.map(code => ({ code })));
};

export const getCountryCodes = () => {
    return axios.get(`${API_BASE}/countries`, { headers: getAuthHeaders() }).then(res => res.data);
};
