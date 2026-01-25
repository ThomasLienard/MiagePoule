import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import {Button, Card, Form} from "react-bootstrap";

const RegisterPage = () => {
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        confirmPassword: '',
        firstName: '',
        lastName: '',
    });

    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);

    const { register } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.id]: e.target.value,
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        // Validation
        if (formData.password !== formData.confirmPassword) {
            setError('Les mots de passe ne correspondent pas');
            return;
        }

        if (formData.password.length < 6) {
            setError('Le mot de passe doit contenir au moins 6 caractères');
            return;
        }

        setLoading(true);

        // Préparer les données pour l'API
        const userData = {
            email: formData.email,
            password: formData.password,
            firstName: formData.firstName,
            lastName: formData.lastName,
            role: 'SPECTATEUR'
        };

        const result = await register(userData);

        if (result.success) {
            setSuccess(result.message);
            setTimeout(() => navigate('/'), 2000);
        } else {
            setError(result.message);
        }

        setLoading(false);
    };

    return (
        <div className="d-flex justify-content-center pt-4">
            <Card>
                <Card.Body >
                    <Card.Title className="text-center mb-2" as="h3">Inscription</Card.Title>
                    <Card.Subtitle className="text-center" as="h6" >Créez votre compte spectateur</Card.Subtitle>
                    <div className="d-flex justify-content-center">
                        <hr style={{width: "16rem"}}/>
                    </div>
                    <Card.Text>
                    {error && (
                        <div className="auth-error">
                            {error}
                        </div>
                    )}

                    {success && (
                        <div className="auth-success">
                            {success}
                        </div>
                    )}

                    <Form onSubmit={handleSubmit} className="auth-form">
                        <Form.Group controlId="firstName">
                            <Form.Label>
                                Prénom
                            </Form.Label>
                            <Form.Control
                                type="text"
                                value={formData.firstName}
                                onChange={handleChange}
                                placeholder="John"
                                required
                            />
                        </Form.Group>

                        <div className="form-group">
                            <label htmlFor="lastName" className="form-label">
                                Nom
                            </label>
                            <input
                                id="lastName"
                                type="text"
                                value={formData.lastName}
                                onChange={handleChange}
                                className="form-input"
                                placeholder="Doe"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="email" className="form-label">
                                Email
                            </label>
                            <input
                                id="email"
                                type="email"
                                value={formData.email}
                                onChange={handleChange}
                                className="form-input"
                                placeholder="votre@email.com"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="password" className="form-label">
                                Mot de passe
                            </label>
                            <input
                                id="password"
                                type="password"
                                value={formData.password}
                                onChange={handleChange}
                                className="form-input"
                                placeholder="••••••••"
                                required
                            />
                            <p className="form-hint">Minimum 6 caractères</p>
                        </div>

                        <div className="form-group">
                            <label htmlFor="confirmPassword" className="form-label">
                                Confirmer le mot de passe
                            </label>
                            <input
                                id="confirmPassword"
                                type="password"
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                className="form-input"
                                placeholder="••••••••"
                                required
                            />
                        </div>

                        <Button
                            type="submit"
                            variant="primary"
                            disabled={loading}
                        >
                            {loading ? 'Inscription en cours...' : "S'inscrire"}
                        </Button>
                    </Form>

                    <div className="auth-footer">
                        <p className="auth-link-text">
                            Déjà un compte ?{' '}
                            <Link to="/login" className="auth-link">
                                Se connecter
                            </Link>
                        </p>
                        <Link to="/" className="auth-link">
                            Retour à l'accueil
                        </Link>
                    </div>
                    </Card.Text>
                </Card.Body>
            </Card>
        </div>
    );
};

export default RegisterPage;