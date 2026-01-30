import {Container, Nav, Navbar} from "react-bootstrap";
import {Link, Outlet, useNavigate, useLocation, Navigate} from "react-router-dom";
import React from "react";
import {useAuth} from "../../contexts/AuthContext.jsx";

export default function Layout() {
    const { user, logout, isAuthenticated, mustChangePassword } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    // Rediriger vers la page de changement de mot de passe si nécessaire
    const allowedPaths = ['/change-password', '/login', '/logout'];
    if (isAuthenticated() && mustChangePassword && !allowedPaths.includes(location.pathname)) {
        return <Navigate to="/change-password" replace />;
    }

    const handleLogout = () => {
        logout();
        navigate('/');
    };
    const handleProfile = () => {
        navigate('/profile');
    };
    return (
        <>
            <Navbar expand="sm" className="bg-body-tertiary">
                <Container>
                    <Navbar.Brand>
                        <Link to="/" className="text-decoration-none text-black">CiblOrgaSport</Link>
                    </Navbar.Brand>
                    <Navbar.Toggle aria-controls="basic-navbar-nav" />
                    <Navbar.Collapse id="basic-navbar-nav">
                        <Nav className="me-auto">
                            <Nav.Link className="auth-button secondary me-2">
                                <Link to="/public/championship" className="text-decoration-none text-body-secondary">Championnats</Link>
                            </Nav.Link>
                            <Nav.Link className="auth-button secondary me-2">
                                <Link to="/public/events" className="text-decoration-none text-body-secondary">Évènements</Link>
                            </Nav.Link>
                            {user?.roles?.includes('ADMIN') && (
                                <Nav.Link className="auth-button secondary me-2">
                                <Link to="/admin" className="text-decoration-none text-body-secondary">Administration</Link>
                                </Nav.Link>
                            )}
                        </Nav>
                        {isAuthenticated() ? (
                            <Nav>
                                <Nav.Link className="auth-button secondary me-2">
                                    <Link to="/logout"
                                          className="text-decoration-none text-body-secondary"
                                          onClick={handleLogout}
                                    >Déconnexion</Link>
                                </Nav.Link>
                                <Nav.Link className="auth-button secondary me-2">
                                    <Link to="/profile"
                                          className="text-decoration-none text-body-secondary"
                                          onClick={handleProfile}
                                    >Profile</Link>
                                </Nav.Link>
                            </Nav>
                        ) : (
                            <Nav>
                                <Nav.Link className="auth-button secondary me-2">
                                    <Link to="/login" className="text-decoration-none text-body-secondary">Connexion</Link>
                                </Nav.Link>
                                <Nav.Link className="auth-button secondary">
                                    <Link to="/register" className="text-decoration-none text-body-secondary">Inscription</Link>
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