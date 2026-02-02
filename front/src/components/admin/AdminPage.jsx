import { Container, Row, Col, Card, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

const AdminPage = () => {
    const { user } = useAuth();

    return (
        <Container className="py-4">
            <h1 className="mb-4">🛠️ Administration</h1>
            <p className="text-muted mb-4">Bienvenue, {user?.email}</p>
            
            <Row>
                <Col md={6} lg={4} className="mb-4">
                    <Card className="h-100">
                        <Card.Body>
                            <Card.Title>👥 Gestion des comptes</Card.Title>
                            <Card.Text>
                                Créer et gérer les comptes des sportifs, volontaires et commissaires.
                            </Card.Text>
                            <Button as={Link} to="/admin/users" variant="secondary">
                                Gérer les comptes
                            </Button>
                        </Card.Body>
                    </Card>
                </Col>
                
                <Col md={6} lg={4} className="mb-4">
                    <Card className="h-100">
                        <Card.Body>
                            <Card.Title>📊 Statistiques</Card.Title>
                            <Card.Text>
                                Voir les statistiques de la plateforme et les rapports d'activité.
                            </Card.Text>
                            <Button variant="secondary" disabled>
                                Bientôt disponible
                            </Button>
                        </Card.Body>
                    </Card>
                </Col>
                
            </Row>
        </Container>
    );
};

export default AdminPage;