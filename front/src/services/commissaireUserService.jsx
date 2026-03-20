import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL;

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

const commissaireUserService = {
    // Récupérer tous les utilisateurs
    getAllUsers: async () => {
        try {
            const response = await axios.get(
                `${API_BASE_URL}/commissaire/users`,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des utilisateurs');
        }
    },

    // Récupérer les utilisateurs par rôle
    getUsersByRole: async (role) => {
        try {
            const response = await axios.get(
                `${API_BASE_URL}/commissaire/users?role=${role}`,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération des utilisateurs');
        }
    },

    // Récupérer un utilisateur par ID
    getUserById: async (id) => {
        try {
            const response = await axios.get(
                `${API_BASE_URL}/commissaire/users/${id}`,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la récupération de l\'utilisateur');
        }
    }
};

export default commissaireUserService;
