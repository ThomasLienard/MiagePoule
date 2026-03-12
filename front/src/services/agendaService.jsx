import axios from "axios";

const API_BASE = "http://localhost:8084";

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

export const getAgenda = () => {
    return axios.get(`${API_BASE}/volunteer/agenda`, { headers: getAuthHeaders() })
        .then(res => res.data);
};
