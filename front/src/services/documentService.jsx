import axios from 'axios';

const API_URL = 'http://localhost:8084/api/documents';

// Créer une instance axios avec interceptor pour ajouter le token
const getAxiosInstance = () => {
    const instance = axios.create({
        baseURL: API_URL,
        headers: {
            'Content-Type': 'application/json',
        },
    });

    // Interceptor pour ajouter le token d'authentification
    instance.interceptors.request.use(
        (config) => {
            const token = localStorage.getItem('authToken');
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
            return config;
        },
        (error) => {
            return Promise.reject(error);
        }
    );

    return instance;
};

export const documentService = {
    // Uploader un ticket
    uploadTicket: async (file, typeId, description) => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('typeId', typeId);
        if (description) {
            formData.append('description', description);
        }

        const response = await getAxiosInstance().post('/tickets/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    },

    // Récupérer tous les tickets de l'utilisateur
    getUserTickets: async () => {
        const response = await getAxiosInstance().get('/tickets');
        return response.data;
    },

    // Récupérer un ticket spécifique
    getTicketById: async (ticketId) => {
        const response = await getAxiosInstance().get(`/tickets/${ticketId}`);
        return response.data;
    },

    // Télécharger un ticket
    downloadTicket: async (ticketId) => {
        const response = await getAxiosInstance().get(`/tickets/${ticketId}/download`, {
            responseType: 'blob',
        });
        return response.data;
    },

    // Supprimer un ticket
    deleteTicket: async (ticketId) => {
        await getAxiosInstance().delete(`/tickets/${ticketId}`);
    },

    // Obtenir le nombre de documents
    getDocumentCount: async () => {
        const response = await getAxiosInstance().get('/count');
        return response.data;
    },
};

export default documentService;