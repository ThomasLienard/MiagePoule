import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import {Button, Card, FloatingLabel, Form} from "react-bootstrap";

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
                <Card.Body className="m-3">
                    <Card.Title className="text-center mb-2" as="h3">Inscription</Card.Title>
                    <Card.Subtitle className="text-center" as="h6" >Créez votre compte spectateur</Card.Subtitle>
                    <div className="d-flex justify-content-center">
                        <hr style={{width: "16rem"}}/>
                    </div>
                    {error && (
                        <div>
                            {error}
                        </div>
                    )}

                    {success && (
                        <div >
                            {success}
                        </div>
                    )}

                    <Form onSubmit={handleSubmit} className="mt-2">
                            <FloatingLabel label="Prénom" controlId="firstName">
                                <Form.Control
                                    type="text"
                                    placeholder="Prénom"
                                    onChange={handleChange}
                                    required
                                    value={formData.firstName}
                                />
                        </FloatingLabel>
                            <FloatingLabel label="Nom" controlId="lastName" className="mt-1">
                                <Form.Control
                                    type="text"
                                    placeholder="Nom"
                                    onChange={handleChange}
                                    required
                                    value={formData.lastName}
                                />
                            </FloatingLabel>
                            <FloatingLabel label="Email" controlId="email"  className="mt-1">
                                <Form.Control
                                    type="email"
                                    placeholder="Eail"
                                    onChange={handleChange}
                                    required
                                    value={formData.email}
                                />
                            </FloatingLabel>
                        <div>
                                <FloatingLabel label="Mot de passe" controlId="password"  className="mt-1">
                                    <Form.Control
                                        type="password"
                                        placeholder="Mot de passe"
                                        onChange={handleChange}
                                        value={formData.password}
                                        required
                                    />
                                </FloatingLabel>
                            <span className="text-body-tertiary">Minimum 6 caractères</span>
                        </div>
                            <FloatingLabel label="Confirmez le mot de passe" controlId="confirmPassword"  className="mt-2">
                                <Form.Control
                                    type="password"
                                    placeholder="Confirmez le mot de passe"
                                    onChange={handleChange}
                                    required
                                    value={formData.confirmPassword}
                                />
                            </FloatingLabel>
                        <div className="d-flex justify-content-center mt-2 mb-3">
                            <Button
                                type="submit"
                                variant="secondary"
                                disabled={loading}
                            >
                                {loading ? 'Inscription en cours...' : "S'inscrire"}
                            </Button>
                        </div>
                    </Form>
                    <div className="auth-footer">
                        <p className="auth-link-text">
                            Déjà un compte ?{' '}
                            <Link to="/login" className="auth-link">
                                Se connecter
                            </Link>
                        </p>
                    </div>
                </Card.Body>
            </Card>
        </div>
    );
};

export default RegisterPage;