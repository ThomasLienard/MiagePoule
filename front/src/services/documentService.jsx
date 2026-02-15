import axios from "axios";

const API_URL = "http://localhost:8084/api/documents";

// Fonction pour récupérer le token
const getAuthToken = () => {
  // Essayez plusieurs méthodes pour récupérer le token
  const token =
    localStorage.getItem("authToken") ||
    localStorage.getItem("token") ||
    sessionStorage.getItem("authToken") ||
    sessionStorage.getItem("token");
  return token;
};

// Créer une instance axios avec interceptor pour ajouter le token
const getAxiosInstance = () => {
  const instance = axios.create({
    baseURL: API_URL,
    headers: {
      "Content-Type": "application/json",
    },
  });

  // Interceptor pour ajouter le token d'authentification
  instance.interceptors.request.use(
    (config) => {
      const token = getAuthToken();
      if (token) {
        // Selon votre configuration, essayez les deux formats courants
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    },
  );

  // Interceptor pour gérer les erreurs
  instance.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        // Rediriger vers la page de login si non authentifié
        localStorage.removeItem("authToken");
        localStorage.removeItem("token");
        window.location.href = "/login";
      }
      return Promise.reject(error);
    },
  );

  return instance;
};

export const documentService = {
  // Uploader un document (CEN_ACCREDITATION, PASSPORT, MEDICAL_CERTIFICATE)
  uploadDocument: async (file, typeId, description = "") => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("typeId", typeId);
    if (description) {
      formData.append("description", description);
    }

    const response = await getAxiosInstance().post("/upload", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  },

  // Uploader un ticket (typeId n'est pas nécessaire, mettez une valeur par défaut)
  uploadTicket: async (file, description) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("typeId", "3");
    // Pas besoin de typeId si c'est toujours "TICKET"
    if (description) {
      formData.append("description", description);
    }

    const response = await getAxiosInstance().post(
      "/tickets/upload",
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      },
    );
    return response.data;
  },

  // Récupérer tous les documents de l'utilisateur
  getUserDocuments: async () => {
    try {
      const response = await getAxiosInstance().get("");
      return response.data;
    } catch (error) {
      console.error("Erreur lors de la récupération des documents:", error);
      throw error;
    }
  },

  // Récupérer les documents par type
  getUserDocumentsByType: async (typeName) => {
    try {
      const response = await getAxiosInstance().get(`/type/${typeName}`);
      return response.data;
    } catch (error) {
      console.error(
        "Erreur lors de la récupération des documents par type:",
        error,
      );
      throw error;
    }
  },

  // Récupérer un ticket spécifique
  getDocumentById: async (documentId) => {
    const response = await getAxiosInstance().get(`/${documentId}`);
    return response.data;
  },

  // Télécharger un document
  downloadDocument: async (documentId) => {
    const response = await getAxiosInstance().get(`/${documentId}/download`, {
      responseType: "blob",
    });
    return response.data;
  },

  // Supprimer un document
  deleteDocument: async (documentId) => {
    await getAxiosInstance().delete(`/${documentId}`);
  },

  // Obtenir le nombre de documents
  getDocumentCount: async () => {
    const response = await getAxiosInstance().get("/count");
    return response.data;
  },
};

export default documentService;
