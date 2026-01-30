import React from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Container, Card, Alert } from 'react-bootstrap';
import adminUserService from '../../services/adminUserService';
import usePasswordForm from '../../hooks/usePasswordForm';
import PasswordForm from './PasswordForm';
import SuccessCard from '../common/SuccessCard';

const ActivateAccountPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const email = searchParams.get('email');

    const submitAction = async (newPassword) => {
        if (!email) {
            throw new Error("Email manquant dans l'URL");
        }
        await adminUserService.activateAccount(email, newPassword);
    };

    const onSuccess = () => {
        setTimeout(() => {
            navigate('/login');
        }, 3000);
    };

    const { formData, error, success, loading, handleChange, handleSubmit } = usePasswordForm(
        submitAction,
        onSuccess
    );

    if (!email) {
        return (
            <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '80vh' }}>
                <Card className="text-center p-4" style={{ maxWidth: '500px' }}>
                    <Card.Body>
                        <h1 className="text-danger">❌ Erreur</h1>
                        <p>Le lien d'activation est invalide ou manquant.</p>
                        <p className="text-muted">Veuillez utiliser le lien fourni par l'administrateur.</p>
                    </Card.Body>
                </Card>
            </Container>
        );
    }

    if (success) {
        return (
            <SuccessCard
                title="✅ Compte activé !"
                message="Votre compte a été activé avec succès."
                redirectMessage="Vous allez être redirigé vers la page de connexion..."
                buttonLabel="Aller à la connexion"
                onButtonClick={() => navigate('/login')}
            />
        );
    }

    return (
        <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '80vh' }}>
            <Card className="p-4" style={{ maxWidth: '500px', width: '100%' }}>
                <Card.Body>
                    <h1 className="text-center mb-3">🔐 Activation de votre compte</h1>
                    <p className="text-muted text-center mb-4">
                        Bienvenue sur CiblOrgaSport ! Pour activer votre compte ({email}), 
                        veuillez définir votre nouveau mot de passe.
                    </p>

                    {error && (
                        <Alert variant="danger">{error}</Alert>
                    )}

                    <PasswordForm
                        formData={formData}
                        handleChange={handleChange}
                        handleSubmit={handleSubmit}
                        loading={loading}
                        submitLabel="Activer mon compte"
                        loadingLabel="Activation en cours..."
                    />

                    <Alert variant="info" className="mt-4">
                        ℹ️ Une fois votre compte activé, vous pourrez vous connecter avec votre email et votre nouveau mot de passe.
                    </Alert>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default ActivateAccountPage;