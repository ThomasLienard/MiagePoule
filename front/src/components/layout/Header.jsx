import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import './Header.css';

const Header = () => {
    const { user, logout, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    return (
        <header className="header">
            <div className="header-container">
                <div className="header-logo">
                    <Link to="/" className="logo-link">
                        <span className="logo-text">SportEvents</span>
                    </Link>
                </div>

                <nav className="header-nav">
                    <Link to="/" className="nav-link">
                        Carte
                    </Link>
                    <Link to="/public/events" className="nav-link">
                        Événements
                    </Link>

                    {user && user.roles?.includes('ADMIN') && (
                        <Link to="/admin" className="nav-link">
                            Administration
                        </Link>
                    )}
                </nav>

                <div className="header-auth">
                    {isAuthenticated() ? (
                        <div className="user-menu">
                            <span className="user-email">{user?.email}</span>
                            <div className="user-dropdown">
                                <div className="user-info">
                  <span className="user-name">
                    {user?.firstName} {user?.lastName}
                  </span>
                                    <span className="user-role">
                    {user?.roles?.join(', ')}
                  </span>
                                </div>
                                <button onClick={handleLogout} className="logout-button">
                                    Déconnexion
                                </button>
                            </div>
                        </div>
                    ) : (
                        <>
                            <Link to="/login" className="auth-button secondary">
                                Connexion
                            </Link>
                            <Link to="/register" className="auth-button primary">
                                Inscription
                            </Link>
                        </>
                    )}
                </div>
            </div>
        </header>
    );
};

export default Header;