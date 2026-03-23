import axios from "axios";

const API_BASE = import.meta.env.VITE_API_URL;

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

export const uploadAgendas = async (agendas) => {
    try {
        const response = await axios.post(
            `${API_BASE}/admin/agenda/upload`,
            agendas,
            { headers: getAuthHeaders() }
        );
        return response.data;
    } catch (error) {
        throw new Error(error.response?.data?.message || "Erreur lors du téléversement des agendas, vérifiez que tout les evenements sont prevues pour le lendemain et que les emails des volontaires sont corrects");
    }
};

