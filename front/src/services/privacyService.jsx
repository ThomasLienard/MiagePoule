import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_URL

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

const privacyService = {

    getPrivacy: async () => {
        try {
            return await axios.get(
                `${API_BASE_URL}/account/privacy`,
                {headers: getAuthHeaders()}
            );
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération');
        }
    },

    updateCategory: async (categoryName, categoryData) => {
        try {
            return await axios.put(
                `${API_BASE_URL}/account/privacy/${categoryName}`,
                categoryData,
                {headers: getAuthHeaders()}
            );
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la mise à jour');
        }
    },

    changePassword: async (passwords) => {
        try {
            return await axios.put(
                `${API_BASE_URL}/account/password`,
                passwords,
                {headers: getAuthHeaders()}
            );
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la mise à jour du mot de passe');
        }
    }
};

export default privacyService;
