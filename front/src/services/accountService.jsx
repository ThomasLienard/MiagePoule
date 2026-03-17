import axios from "axios";

const API_BASE = import.meta.env.VITE_API_URL;

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

export const getAccount = () => {
    return axios.get(`${API_BASE}/account`, { headers: getAuthHeaders() });
};

export const updateSettings = (data) => {
    return axios.put(`${import.meta.env.VITE_API_URL}/account/settings`,
        data,
        { headers: getAuthHeaders() }
    );
}

export const signChart = () => {
    return axios.post(`${import.meta.env.VITE_API_URL}/account/sign-charter`,
        {},
        { headers: getAuthHeaders() }
    );
}