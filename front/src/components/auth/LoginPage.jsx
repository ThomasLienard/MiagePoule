import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import {Button, Card} from "react-bootstrap";

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
            navigate('/');
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

                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="form-group">
                        <label htmlFor="email" className="form-label">
                            Email
                        </label>
                        <input
                            id="email"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
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
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="form-input"
                            placeholder="••••••••"
                            required
                        />
                    </div>

                    <Button
                        type="submit"
                        variant = "primary"
                        disabled={loading}
                    >
                        {loading ? 'Connexion en cours...' : 'Se connecter'}
                    </Button>
                </form>

                <div className="auth-footer">
                    <p className="auth-link-text">
                        Pas encore de compte ?{' '}
                        <Link to="/register" className="auth-link">
                            S'inscrire
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

export default LoginPage;