import React, { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Container, Card, Form, Button, Alert, Spinner } from 'react-bootstrap';
import adminUserService from '../../services/adminUserService';

const ActivateAccountPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const email = searchParams.get('email');

    const [formData, setFormData] = useState({
        newPassword: '',
        confirmPassword: ''
    });
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);

        // Validation
        if (!formData.newPassword) {
            setError('Le mot de passe est requis');
            return;
        }
        if (formData.newPassword.length < 6) {
            setError('Le mot de passe doit contenir au moins 6 caractères');
            return;
        }
        if (formData.newPassword !== formData.confirmPassword) {
            setError('Les mots de passe ne correspondent pas');
            return;
        }
        if (!email) {
            setError("Email manquant dans l'URL");
            return;
        }

        setLoading(true);
        try {
            await adminUserService.activateAccount(email, formData.newPassword);
            setSuccess(true);
            setTimeout(() => {
                navigate('/login');
            }, 3000);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

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
            <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '80vh' }}>
                <Card className="text-center p-4" style={{ maxWidth: '500px' }}>
                    <Card.Body>
                        <h1 className="text-success">✅ Compte activé !</h1>
                        <p>Votre compte a été activé avec succès.</p>
                        <p className="text-muted">Vous allez être redirigé vers la page de connexion...</p>
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

                    <Form onSubmit={handleSubmit}>
                        <Form.Group className="mb-3">
                            <Form.Label>Nouveau mot de passe</Form.Label>
                            <Form.Control
                                type="password"
                                name="newPassword"
                                value={formData.newPassword}
                                onChange={handleChange}
                                placeholder="Minimum 6 caractères"
                                autoComplete="new-password"
                            />
                        </Form.Group>

                        <Form.Group className="mb-4">
                            <Form.Label>Confirmer le mot de passe</Form.Label>
                            <Form.Control
                                type="password"
                                name="confirmPassword"
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                placeholder="Retapez votre mot de passe"
                                autoComplete="new-password"
                            />
                        </Form.Group>

                        <Button 
                            type="submit" 
                            variant="primary"
                            className="w-100"
                            disabled={loading}
                        >
                            {loading ? (
                                <>
                                    <Spinner animation="border" size="sm" className="me-2" />
                                    Activation en cours...
                                </>
                            ) : 'Activer mon compte'}
                        </Button>
                    </Form>

                    <Alert variant="info" className="mt-4">
                        ℹ️ Une fois votre compte activé, vous pourrez vous connecter avec votre email et votre nouveau mot de passe.
                    </Alert>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default ActivateAccountPage;