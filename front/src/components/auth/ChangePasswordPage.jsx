import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Card, Form, Button, Alert, Spinner } from 'react-bootstrap';
import { useAuth } from '../../contexts/AuthContext';
import adminUserService from '../../services/adminUserService';

const ChangePasswordPage = () => {
    const navigate = useNavigate();
    const { user, logout, clearMustChangePassword } = useAuth();

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

        setLoading(true);
        try {
            await adminUserService.activateAccount(user.email, formData.newPassword);
            setSuccess(true);
            
            // Mettre à jour le flag dans le contexte
            if (clearMustChangePassword) {
                clearMustChangePassword();
            }
            
            setTimeout(() => {
                navigate('/');
            }, 2000);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

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
            <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '80vh' }}>
                <Card className="text-center p-4" style={{ maxWidth: '500px' }}>
                    <Card.Body>
                        <h1 className="text-success">✅ Mot de passe modifié !</h1>
                        <p>Votre mot de passe a été mis à jour avec succès.</p>
                        <p className="text-muted">Vous allez être redirigé vers l'accueil...</p>
                        <Button 
                            variant="primary"
                            onClick={() => navigate('/')}
                        >
                            Aller à l'accueil
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

                        <div className="d-grid gap-2">
                            <Button 
                                type="submit" 
                                variant="primary"
                                disabled={loading}
                            >
                                {loading ? (
                                    <>
                                        <Spinner animation="border" size="sm" className="me-2" />
                                        Mise à jour en cours...
                                    </>
                                ) : 'Mettre à jour mon mot de passe'}
                            </Button>
                            <Button 
                                variant="outline-secondary"
                                onClick={handleLogout}
                                disabled={loading}
                            >
                                Se déconnecter
                            </Button>
                        </div>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default ChangePasswordPage;
