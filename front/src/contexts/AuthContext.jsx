import React, { createContext, useState, useContext, useEffect } from "react";
import authService from "../services/authService";

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth doit être utilisé dans un AuthProvider");
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [mustChangePassword, setMustChangePassword] = useState(false);
  const [isAccountValidated, setIsAccountValidated] = useState(true);

  useEffect(() => {
    // Vérifier l'authentification au chargement
    const initAuth = () => {
      if (authService.isAuthenticated()) {
        const userData = authService.getUser();
        setUser(userData);
        // Vérifier si l'utilisateur doit changer son mot de passe
        const storedMustChange = localStorage.getItem("mustChangePassword");
        if (storedMustChange === "true") {
          setMustChangePassword(true);
        }
        // Vérifier si le compte est validé
        const storedValidated = localStorage.getItem("isAccountValidated");
        if (storedValidated === "false") {
          setIsAccountValidated(false);
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email, password) => {
    try {
      const response = await authService.login(email, password);

      // Stocker le token
      localStorage.setItem("token", response.token);

      // Décoder et stocker les informations utilisateur
      const decoded = authService.decodeToken(response.token);
      const userData = {
        id: decoded.sub,
        email: decoded.email,
        roles: decoded.roles,
      };
      localStorage.setItem("user", JSON.stringify(userData));

      setUser(userData);

      // Gérer le changement de mot de passe obligatoire
      if (response.mustChangePassword) {
        localStorage.setItem("mustChangePassword", "true");
        setMustChangePassword(true);
        return { success: true, mustChangePassword: true };
      }

      // Gérer le statut de validation du compte (utiliser isAccountValidated du backend)
      const accountValidated = response.isAccountValidated !== false;
      localStorage.setItem("isAccountValidated", accountValidated.toString());
      setIsAccountValidated(accountValidated);

      return {
        success: true,
        mustChangePassword: false,
        isAccountValidated: accountValidated,
      };
    } catch (error) {
      return { success: false, message: error.message };
    }
  };

  const register = async (userData) => {
    try {
      const response = await authService.register(userData);

      if (response.message === "Email already exists") {
        return { success: false, message: "Email déjà utilisé" };
      }

      // Connecter automatiquement après l'inscription
      if (response.token) {
        localStorage.setItem("token", response.token);

        const userInfo = {
          id: response.email, // Utiliser l'email comme ID temporaire
          email: response.email,
          firstName: response.firstName,
          lastName: response.lastName,
          role: response.role,
          roles: ["SPECTATOR"], // Tous les nouveaux utilisateurs sont spectateurs
        };
        localStorage.setItem("user", JSON.stringify(userInfo));
        setUser(userInfo);
      }

      return { success: true, message: "Inscription réussie" };
    } catch (error) {
      return { success: false, message: error.message };
    }
  };

  const logout = () => {
    authService.logout();
    localStorage.removeItem("mustChangePassword");
    localStorage.removeItem("isAccountValidated");
    setUser(null);
    setMustChangePassword(false);
    setIsAccountValidated(true);
    window.location.href = "/login";
  };

  const clearMustChangePassword = () => {
    localStorage.removeItem("mustChangePassword");
    setMustChangePassword(false);
  };

  const value = {
    user,
    loading,
    mustChangePassword,
    isAccountValidated,
    setIsAccountValidated,
    login,
    register,
    logout,
    clearMustChangePassword,
    isAuthenticated: authService.isAuthenticated,
    hasRole: authService.hasRole,
    hasAnyRole: authService.hasAnyRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
