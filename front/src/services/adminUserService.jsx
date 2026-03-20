import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL;

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

const adminUserService = {
    // Créer un nouvel utilisateur
    createUser: async (userData) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/admin/users`,
                userData,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la création du compte');
        }
    },

    // Créer plusieurs utilisateurs en masse
    bulkCreateUsers: async (usersData) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/admin/users/bulk`,
                usersData,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la création en masse des utilisateurs');
        }
    },

    // Récupérer tous les utilisateurs
    getAllUsers: async () => {
        try {
            const response = await axios.get(
                `${API_BASE_URL}/admin/users`,
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
                `${API_BASE_URL}/admin/users?role=${role}`,
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
                `${API_BASE_URL}/admin/users/${id}`,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Utilisateur non trouvé');
        }
    },

    // Mettre à jour un utilisateur
    updateUser: async (id, userData) => {
        try {
            const response = await axios.put(
                `${API_BASE_URL}/admin/users/${id}`,
                userData,
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la mise à jour');
        }
    },

    // Désactiver un utilisateur
    deactivateUser: async (id, reason) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/admin/users/${id}/deactivate`,
                { reason },
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la désactivation');
        }
    },

    // Réactiver un utilisateur
    reactivateUser: async (id) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/admin/users/${id}/reactivate`,
                {},
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la réactivation');
        }
    },

    // Réinitialiser le mot de passe
    resetPassword: async (id) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/admin/users/${id}/reset-password`,
                {},
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la réinitialisation');
        }
    },

    // Activer le compte (première connexion)
    activateAccount: async (email, newPassword) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/auth/activate?email=${encodeURIComponent(email)}`,
                { newPassword },
                { headers: { 'Content-Type': 'application/json' } }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de l\'activation');
        }
    },

    // Valider un compte utilisateur (change isAccountValidated à true)
    validateUserAccount: async (id) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/admin/users/${id}/validate-account`,
                {},
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de la validation du compte');
        }
    },

    // Invalider un compte utilisateur (change isAccountValidated à false)
    invalidateUserAccount: async (id) => {
        try {
            const response = await axios.post(
                `${API_BASE_URL}/admin/users/${id}/invalidate-account`,
                {},
                { headers: getAuthHeaders() }
            );
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Erreur lors de l\'invalidation du compte');
        }
    }
};

export default adminUserService;
