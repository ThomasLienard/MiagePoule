import axios from 'axios';

const API_BASE_URL = 'http://localhost:8084';

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

const adminIncidentService = {
    createIncident: async (incidentData) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/admin/incident`,
                incidentData,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la création de l\'incident');
        }
    }
};

export default adminIncidentService;
