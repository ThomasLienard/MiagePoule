import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Card, Button, Alert } from 'react-bootstrap';
import { useAuth } from '../../contexts/AuthContext';
import adminUserService from '../../services/adminUserService';
import usePasswordForm from '../../hooks/usePasswordForm';
import PasswordForm from './PasswordForm';
import SuccessCard from '../common/SuccessCard';

const ChangePasswordPage = () => {
    const navigate = useNavigate();
    const { user, logout, clearMustChangePassword } = useAuth();

    const handlePasswordChange = async (newPassword) => {
        await adminUserService.activateAccount(user.email, newPassword);
        
        // Mettre à jour le flag dans le contexte
        if (clearMustChangePassword) {
            clearMustChangePassword();
        }
        
        setTimeout(() => {
            navigate('/');
        }, 2000);
    };

    const {
        formData,
        error,
        success,
        loading,
        handleChange,
        handleSubmit
    } = usePasswordForm(handlePasswordChange);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    if (!user) {
        return (
            <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '80vh' }}>
                <Card className="text-center p-4" style={{ maxWidth: '500px' }}>
                    <Card.Body>
                        <h1 className="text-danger">❌ Non connecté</h1>
                        <p>Vous devez être connecté pour accéder à cette page.</p>
                        <Button 
                            variant="primary"
                            onClick={() => navigate('/login')}
                        >
                            Aller à la connexion
                        </Button>
                    </Card.Body>
                </Card>
            </Container>
        );
    }

    if (success) {
        return (
            <SuccessCard
                title="✅ Mot de passe modifié !"
                message="Votre mot de passe a été mis à jour avec succès."
                redirectMessage="Vous allez être redirigé vers l'accueil..."
                buttonLabel="Aller à l'accueil"
                onButtonClick={() => navigate('/')}
            />
        );
    }

    return (
        <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '80vh' }}>
            <Card className="p-4" style={{ maxWidth: '500px', width: '100%' }}>
                <Card.Body>
                    <h1 className="text-center mb-3">🔐 Changement de mot de passe requis</h1>
                    <p className="text-muted text-center mb-4">
                        Pour des raisons de sécurité, vous devez définir un nouveau mot de passe 
                        avant de pouvoir continuer à utiliser l'application.
                    </p>

                    <Alert variant="info">
                        <strong>Compte :</strong> {user.email}
                    </Alert>

                    {error && (
                        <Alert variant="danger">{error}</Alert>
                    )}

                    <PasswordForm
                        formData={formData}
                        handleChange={handleChange}
                        handleSubmit={handleSubmit}
                        loading={loading}
                        submitLabel="Mettre à jour mon mot de passe"
                        loadingLabel="Mise à jour en cours..."
                    />

                    <Button 
                        variant="outline-secondary"
                        className="w-100 mt-2"
                        onClick={handleLogout}
                        disabled={loading}
                    >
                        Se déconnecter
                    </Button>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default ChangePasswordPage;
