import {Link, Outlet, useNavigate, useLocation, Navigate} from "react-router-dom";
import React from "react";
import {useAuth} from "../../contexts/AuthContext.jsx";
import { useNotificationsSSE } from "../../hooks/useNotificationSSE.js"; // ← IMPORT

export default function Layout() {
    const { user, logout, isAuthenticated, mustChangePassword } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    // ← NOUVEAU : Hook notifications SSE
    const userId = user?.id ?? null;
    const { unreadCount, markAllAsRead } = useNotificationsSSE(userId);

    // Rediriger vers la page de changement de mot de passe si nécessaire
    const allowedPaths = ['/change-password', '/login', '/logout'];
    if (isAuthenticated() && mustChangePassword && !allowedPaths.includes(location.pathname)) {
        return <Navigate to="/change-password" replace />;
    }

    const handleLogout = (e) => {
        e.preventDefault();
        logout();
        navigate('/');
    };

    const handleProfile = () => {
        navigate('/account');
    };

    return (
        <>
            <Navbar expand="sm" className="bg-body-tertiary">
                <Container>
                    <Navbar.Brand>
                        <Link to="/" className="text-decoration-none text-black">CiblOrgaSport</Link>
                    </Navbar.Brand>
                    <Navbar.Toggle aria-controls="basic-navbar-nav"/>
                    <Navbar.Collapse id="basic-navbar-nav">
                        <Nav className="me-auto">
                            <Nav.Link className="auth-button secondary me-2" as="span">
                                <Link to="/public/championship"
                                      className="text-decoration-none text-body-secondary">Championnats</Link>
                            </Nav.Link>
                            {user?.roles?.includes('ADMIN') && (
                                <Nav.Link className="auth-button secondary me-2" as="span">
                                    <Link to="/admin" className="text-decoration-none text-body-secondary">Administration</Link>
                                </Nav.Link>
                            )}
                        </Nav>

                        {/* ← NOUVEAU : Badge notifications (UNIQUEMENT si connecté) */}
                        {isAuthenticated() && (
                            <Nav className="me-2">
                                <div className="position-relative">
                                    <Nav.Link
                                        className="p-0 notification-bell"
                                        onClick={markAllAsRead}
                                        style={{ cursor: 'pointer' }}
                                    >
                                        🔔
                                        {unreadCount > 0 && (
                                            <Badge
                                                bg="danger"
                                                pill
                                                className="position-absolute top-0 start-100 translate-middle"
                                                style={{ fontSize: '0.65em' }}
                                            >
                                                {unreadCount}
                                            </Badge>
                                        )}
                                    </Nav.Link>
                                </div>
                            </Nav>
                        )}

                        {isAuthenticated() ? (
                            <Nav>
                                <Nav.Link className="auth-button secondary me-2" as="span">
                                    <Link to="/logout"
                                          className="text-decoration-none text-body-secondary"
                                          onClick={handleLogout}
                                    >Déconnexion</Link>
                                </Nav.Link>
                                <Nav.Link className="auth-button secondary me-2" as="span">
                                    <Link to="/account"
                                          className="text-decoration-none text-body-secondary"
                                          onClick={handleProfile}
                                    >Profil</Link>
                                </Nav.Link>
                                <Nav.Link className="auth-button secondary me-2" as="span">
                                    <Link to="/privacy"
                                          className="text-decoration-none text-body-secondary">Confidentialité</Link>
                                </Nav.Link>
                            </Nav>
                        ) : (
                            <Nav>
                                <Nav.Link className="auth-button secondary me-2" as="span">
                                    <Link to="/login"
                                          className="text-decoration-none text-body-secondary">Connexion</Link>
                                </Nav.Link>
                                <Nav.Link className="auth-button secondary" as="span">
                                    <Link to="/register"
                                          className="text-decoration-none text-body-secondary">Inscription</Link>
                                </Nav.Link>
                            </Nav>
                        )}
                    </Navbar.Collapse>
                </Container>
            </Navbar>
            <Outlet />
        </>
    );
}