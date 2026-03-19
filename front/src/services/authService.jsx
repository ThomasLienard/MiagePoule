import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL;

const authService = {
    // Connexion
    login: async (email, password) => {
        try {
            const response = await axios.post(`${API_BASE_URL}/auth/login`, {
                email,
                password
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });
            return response.data;
        } catch (error) {
            if (error.response?.status === 401) {
                throw new Error('Email ou mot de passe incorrect');
            } else if (error.response?.status === 400) {
                throw new Error('Format de données invalide');
            } else if (error.response?.status === 403) {
                throw new Error('Accès refusé');
            } else {
                throw new Error('Erreur serveur. Veuillez réessayer plus tard.');
            }
        }
    },

    // Inscription
    register: async (userData) => {
        try {
            const response = await axios.post(`${API_BASE_URL}/auth/signup`, {
                name: userData.firstName,
                lastname: userData.lastName,
                email: userData.email,
                password: userData.password,
                countryCode: userData.countryCode || 'FR',
                roleName: 'SPECTATEUR'
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });
            return response.data;
        } catch (error) {
            if (error.response?.status === 400) {
                throw new Error(error.response?.data?.message || 'Email déjà utilisé');
            } else {
                throw new Error("Erreur lors de l'inscription");
            }
        }
    },

    // Déconnexion
    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    },

    // Vérifier si l'utilisateur est connecté
    isAuthenticated: () => {
        const token = localStorage.getItem('token');
        if (!token) return false;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const isExpired = payload.exp * 1000 < Date.now();
            return !isExpired;
        } catch (error) {
            console.error('Error decoding token:', error);
            return false;
        }
    },

    // Récupérer le token
    getToken: () => {
        return localStorage.getItem('token');
    },

    // Récupérer les informations de l'utilisateur
    getUser: () => {
        const userStr = localStorage.getItem('user');
        if (!userStr) return null;

        try {
            return JSON.parse(userStr);
        } catch (error) {
            console.error('Error parsing user data:', error);
            return null;
        }
    },

    // Vérifier si l'utilisateur a un rôle spécifique
    hasRole: (role) => {
        const user = authService.getUser();
        if (!user) return false;
        return user.roles?.includes(role) || false;
    },

    // Vérifier si l'utilisateur a au moins un des rôles
    hasAnyRole: (roles) => {
        const user = authService.getUser();
        if (!user) return false;
        return roles.some(role => user.roles?.includes(role));
    },

    // Décode le token JWT
    decodeToken: (token) => {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload;
        } catch (error) {
            console.error('Error decoding token:', error);
            return null;
        }
    }
};

// Intercepteur axios pour ajouter le token aux requêtes
axios.interceptors.request.use(
    (config) => {
        const token = authService.getToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Intercepteur pour gérer les erreurs d'authentification
axios.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401 || error.response?.status === 403) {
            authService.logout();
            globalThis.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default authService;