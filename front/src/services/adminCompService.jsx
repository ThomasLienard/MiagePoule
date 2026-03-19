import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL;

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

const adminCompService = {
    createComp: async (compData) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/admin/comps`,
                compData,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la création du compte');
        }
    },

    updateComp: async (compId, compData) => {
        try {
            const response = await axios.put(
                `${API_BASE_URL}/admin/comps/${compId}`,
                compData,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la création du compte');
        }
    }
};

export default adminCompService;
