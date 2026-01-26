import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import {Button, Card, FloatingLabel, Form} from "react-bootstrap";

const LoginPage = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        const result = await login(email, password);

        if (result.success) {
            navigate('/account');
        } else {
            setError(result.message);
        }

        setLoading(false);
    };

    return (
        <div className="d-flex justify-content-center pt-4">
            <Card>
                <Card.Body>
                    <Card.Title className="text-center mb-2" as="h3">Connexion</Card.Title>
                    <Card.Subtitle className="text-center" as="h6">Connectez-vous à votre compte</Card.Subtitle>
                    <div className="d-flex justify-content-center">
                        <hr style={{width: "16rem"}}/>
                    </div>
                    <Card.Text>

                {error && (
                    <div className="auth-error">
                        {error}
                    </div>
                )}
                        <Form onSubmit={handleSubmit} className="mt-2">
                            <FloatingLabel label="Email" controlId="email"  className="mt-1">
                                <Form.Control
                                    type="email"
                                    placeholder="Email"
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                    value={email}
                                />
                            </FloatingLabel>
                                <FloatingLabel label="Mot de passe" controlId="password"  className="mt-1">
                                    <Form.Control
                                        type="password"
                                        placeholder="Mot de passe"
                                        onChange={(e) => setPassword(e.target.value)}
                                        value={password}
                                        required
                                    />
                                </FloatingLabel>
                            <div className="d-flex justify-content-center mt-2 mb-3">
                                <Button
                                    type="submit"
                                    variant="secondary"
                                    disabled={loading}
                                >
                                    {loading ? 'Connexion en cours...' : "Se connecter"}
                                </Button>
                            </div>
                        </Form>

                <div className="auth-footer">
                    <p className="auth-link-text">
                        Pas encore de compte ?{' '}
                        <Link to="/register" className="auth-link">
                            S'inscrire
                        </Link>
                    </p>
                </div>
                    </Card.Text>
                </Card.Body>
            </Card>
        </div>
    );
};

export default LoginPage;