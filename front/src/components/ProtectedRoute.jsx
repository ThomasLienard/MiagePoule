import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const ProtectedRoute = ({ children, allowedRoles = [] }) => {
    const { user, loading, mustChangePassword } = useAuth();
    const location = useLocation();

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-screen">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
            </div>
        );
    }

    if (!user) {
        // Rediriger vers la page de login si non connecté
        return <Navigate to="/login" replace />;
    }

    // Rediriger vers la page de changement de mot de passe si nécessaire
    // (sauf si on est déjà sur cette page)
    if (mustChangePassword && location.pathname !== '/change-password') {
        return <Navigate to="/change-password" replace />;
    }

    if (allowedRoles.length > 0 && !allowedRoles.some(role => user.roles?.includes(role))) {
        // Rediriger vers la page d'accueil si l'utilisateur n'a pas le rôle requis
        return <Navigate to="/" replace />;
    }

    return children;
};

export default ProtectedRoute;