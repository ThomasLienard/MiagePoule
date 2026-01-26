import {Container, Nav, Navbar} from "react-bootstrap";
import {Link, Outlet} from "react-router-dom";
import React from "react";

export default function Layout() {
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
                        </Nav>
                        <Nav>
                            <Nav.Link className="auth-button secondary me-2">
                                <Link to="/login" className="text-decoration-none text-body-secondary">Connexion</Link>
                            </Nav.Link>
                            <Nav.Link className="auth-button secondary">
                                <Link to="/register" className="text-decoration-none text-body-secondary">Inscription</Link>
                            </Nav.Link>
                        </Nav>
                    </Navbar.Collapse>
                </Container>
            </Navbar>
            <Outlet />
        </>
    );
}