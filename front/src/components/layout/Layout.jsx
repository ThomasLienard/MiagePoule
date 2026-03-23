import {Link, Outlet, useNavigate, useLocation, Navigate} from "react-router-dom";
import {useAuth} from "../../contexts/AuthContext.jsx";
import { useNotificationsSSE } from "../../hooks/useNotificationSSE.js";
import {Navbar, Container, Nav, Badge, Popover, ListGroup} from "react-bootstrap";
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';

export default function Layout() {
    const { user, logout, isAuthenticated, mustChangePassword, isAccountValidated } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    // Hook notifications SSE
    const userId = user?.id ?? null;
    const { unreadCount, markAllAsRead, notifications } = useNotificationsSSE(userId);

    const handleNotificationClick = (eventId) => {
        if (eventId) {
            navigate(`/public/trials/${eventId}`);
        }
    };

    const popover = (
        <Popover id="popover-basic" className="notification-panel">
            <Popover.Header as="h4">Notifications</Popover.Header>
            <Popover.Body className="p-0 notification-panel">
                <ListGroup variant="flush" style={{ maxHeight: '300px', overflowY: 'auto' }}>
                    { notifications?.length > 0 ?
                        notifications?.map((notif, index) => (
                            <ListGroup.Item 
                                key={index}
                                action
                                onClick={() => handleNotificationClick(notif?.eventId)}
                                className={`notification-item ${notif?.eventId ? 'cursor-pointer' : ''}`}
                                style={{ cursor: notif?.eventId ? 'pointer' : 'default' }}
                                data-date={notif?.emissionDate}
                                data-type={notif?.type}
                            >
                                <div className="fw-semibold">{notif?.title || notif?.description}</div>
                                {notif?.title && notif?.description && (
                                    <div className="fw-normal notification-description">{notif?.description}</div>
                                )}
                                {notif?.eventId && (
                                    <small className="text-primary">
                                        → Voir les détails
                                    </small>
                                )}
                                <div className="text-muted notification-date" style={{ fontSize: '0.75em', marginTop: '4px' }}>
                                    {new Date(notif?.emissionDate).toLocaleString('fr-FR')}
                                </div>
                            </ListGroup.Item>
                        ))
                    : 
                        <ListGroup.Item className="text-muted text-center">
                            Aucune notification pour le moment
                        </ListGroup.Item> 
                    }
                </ListGroup>
            </Popover.Body>
        </Popover>
    );

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

                            {user?.roles?.includes('ATHLETE') && (
                                <Nav.Link className="auth-button secondary me-2" as="span">
                                    <Link to={`/public/athlete-trials/${user.id}`}
                                          className="text-decoration-none text-body-secondary">Mes épreuves</Link>
                                </Nav.Link>
                            )}
                            {isAuthenticated() && (
                                <Nav.Link className="auth-button secondary me-2" as="span">
                                    <Link to="/tickets"
                                          className="text-decoration-none text-body-secondary">📄 Mes Billets</Link>
                                </Nav.Link>
                            )}

                            {user?.roles?.includes('VOLONTAIRE') && (
                                <Nav.Link className="auth-button secondary me-2" as="span">
                                    <Link to={`/agenda`}
                                          className="text-decoration-none text-body-secondary">📖 Mon agenda</Link>
                                </Nav.Link>
                            )}

                            {user?.roles?.includes('ADMIN') && (
                                <>
                                    <Nav.Link className="auth-button secondary me-2" as="span">
                                        <Link to="/admin"
                                              className="text-decoration-none text-body-secondary">Administration</Link>
                                    </Nav.Link>
                                </>
                            )}

                            {user?.roles?.includes('COMMISSAIRE') && isAccountValidated && (
                                <>
                                    <Nav.Link className="auth-button secondary me-2" as="span">
                                        <Link to="/commissaire/trials"
                                              className="text-decoration-none text-body-secondary">Gestion épreuves</Link>
                                    </Nav.Link>
                                    <Nav.Link className="auth-button secondary me-2" as="span">
                                        <Link to="/commissaire/teams"
                                              className="text-decoration-none text-body-secondary">Gestion équipes</Link>
                                    </Nav.Link>
                                </>
                            )}

                        </Nav>

                        {/* Badge notifications (UNIQUEMENT si connecté) */}
                        {isAuthenticated() && (
                            <Nav className="me-2">
                                <OverlayTrigger trigger="click" placement="bottom" overlay={popover}>
                                <div className="position-relative notification-button">
                                    <Nav.Link
                                        className="p-0 notification-bell"
                                        onClick={markAllAsRead}
                                        style={{ cursor: 'pointer' }}
                                    >
                                        🔔
                                        {unreadCount > 0 &&
                                            <Badge
                                                bg="danger"
                                                pill
                                                className="position-absolute top-0 start-100 translate-middle notification-badge"
                                                style={{ fontSize: '0.65em' }}
                                            >
                                                {unreadCount}
                                            </Badge>
                                        }
                                    </Nav.Link>
                                </div>
                                </OverlayTrigger>
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