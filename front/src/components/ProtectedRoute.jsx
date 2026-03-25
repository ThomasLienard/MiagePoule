import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Alert, Container } from 'react-bootstrap';

const ProtectedRoute = ({ children, allowedRoles = [] }) => {
    const { user, loading, mustChangePassword, isAccountValidated } = useAuth();
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

    // Vérifier si le compte doit être validé pour accéder aux routes spécifiques
    const rolesRequiringValidation = ['ATHLETE', 'COMMISSAIRE', 'VOLONTAIRE'];
    const needsValidation = allowedRoles.some(role => rolesRequiringValidation.includes(role));

    // Autoriser l'accès aux pages profil et confidentialité même si non validé
    const allowedPathsForUnvalidated = ['/account', '/privacy'];
    const isAllowedPath = allowedPathsForUnvalidated.includes(location.pathname);

    if (needsValidation && !isAccountValidated && !isAllowedPath) {
        // Afficher un message si le compte n'est pas validé pour un rôle nécessitant validation
        return (
            <Container className="mt-5">
                <Alert variant="warning">
                    <Alert.Heading>⏳ Compte en attente de validation</Alert.Heading>
                    <p>
                        Votre compte doit être validé par un administrateur avant de pouvoir accéder à cette fonctionnalité.
                    </p>
                    <hr />
                    <p className="mb-0">
                        <strong>Que faire ?</strong>
                        <ul className="mt-2">
                            <li>Assurez-vous d'avoir déposé tous les documents requis dans votre <a href="/account">profil</a></li>
                            {user.roles?.includes('ATHLETE') && (
                                <li>Signez la Charte Européenne du Sport dans votre profil</li>
                            )}
                            <li>Contactez un administrateur si votre compte n'est pas validé après 48h</li>
                        </ul>
                    </p>
                </Alert>
            </Container>
        );
    }

    if (allowedRoles.length > 0 && !allowedRoles.some(role => user.roles?.includes(role))) {
        // Rediriger vers la page d'accueil si l'utilisateur n'a pas le rôle requis
        return <Navigate to="/" replace />;
    }

    return children;
};

export default ProtectedRoute;