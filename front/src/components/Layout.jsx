import {Container, Nav, Navbar} from "react-bootstrap";
import {Link, Outlet} from "react-router-dom";

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
                            <Nav.Link>
                                <Link to="/public/championship" className="text-decoration-none text-body-secondary">Championnats</Link>
                            </Nav.Link>
                            <Nav.Link>
                                <Link to="/public/events" className="text-decoration-none text-body-secondary">Évènements</Link>
                            </Nav.Link>
                        </Nav>
                    </Navbar.Collapse>
                </Container>
            </Navbar>
            <Outlet />
        </>
    );
}