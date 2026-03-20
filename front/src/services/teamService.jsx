import axios from "axios";

const API_BASE = `${import.meta.env.VITE_API_URL}/commissaire`;

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

export const getAllTeams = () => {
    return axios.get(`${API_BASE}/teams`, { headers: getAuthHeaders() }).then(res => res.data);
};

export const getTeamById = (id) => {
    return axios.get(`${API_BASE}/teams/${id}`, { headers: getAuthHeaders() }).then(res => res.data);
};

export const createTeam = (teamData) => {
    return axios.post(`${API_BASE}/teams`, teamData, { headers: getAuthHeaders() }).then(res => res.data);
};

export const updateTeam = (id, teamData) => {
    return axios.put(`${API_BASE}/teams/${id}`, teamData, { headers: getAuthHeaders() }).then(res => res.data);
};

export const deleteTeam = (id) => {
    return axios.delete(`${API_BASE}/teams/${id}`, { headers: getAuthHeaders() }).then(res => res.data);
};
