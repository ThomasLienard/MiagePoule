import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const ProtectedRoute = ({ children, allowedRoles = [] }) => {
    const { user, loading } = useAuth();

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

    if (allowedRoles.length > 0 && !allowedRoles.some(role => user.roles?.includes(role))) {
        // Rediriger vers la page d'accueil si l'utilisateur n'a pas le rôle requis
        return <Navigate to="/" replace />;
    }

    return children;
};

export default ProtectedRoute;