import { useState } from 'react';
import { Container, Row, Col, Card, Button, Alert } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import CreateIncidentModal from './CreateIncidentModal';

const AdminPage = () => {
    const { user } = useAuth();
    const [showIncidentModal, setShowIncidentModal] = useState(false);
    const [successMessage, setSuccessMessage] = useState(null);

    const handleIncidentCreated = (incident) => {
        setSuccessMessage(`Incident créé : ${incident.title}`);
        setTimeout(() => setSuccessMessage(null), 5000);
    };

    return (
        <Container className="py-4">
            <h1 className="mb-4">🛠️ Administration</h1>
            <p className="text-muted mb-4">Bienvenue, {user?.email}</p>

            {successMessage && (
                <Alert variant="success" onClose={() => setSuccessMessage(null)} dismissible>
                    {successMessage}
                </Alert>
            )}

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
                            <Card.Title>📅 Planification</Card.Title>
                            <Card.Text>
                                Créer et modifier des épreuves ou activités extra-compétition.
                                Gérez les lieux, horaires et accès PMR/Parking.
                            </Card.Text>
                            <div className="d-flex gap-2 flex-wrap">
                                <Button as={Link} to="/admin/create-event" variant="secondary">
                                    Créer un évènement
                                </Button>
                                <Button variant="secondary" disabled>
                                    Modifier un évènement
                                </Button>
                                <Button as={Link} to="/admin/create-champ" variant="secondary">
                                    Créer un championnat
                                </Button>
                                <Button as={Link} to="/admin/create-comp" variant="secondary">
                                    Créer une compétition
                                </Button>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>

                <Col md={6} lg={4} className="mb-4">
                    <Card className="h-100">
                        <Card.Body>
                            <Card.Title>🚨 Incidents</Card.Title>
                            <Card.Text>
                                Création rapide d'incidents (notifications de type incident) pour signaler des anomalies.
                            </Card.Text>
                            <Button variant="secondary" onClick={() => setShowIncidentModal(true)}>
                                Créer un incident
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

            <CreateIncidentModal
                show={showIncidentModal}
                onClose={() => setShowIncidentModal(false)}
                onCreated={handleIncidentCreated}
            />
        </Container>
    );
};

export default AdminPage;