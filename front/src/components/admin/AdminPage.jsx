import { Container, Row, Col, Card, Button, Alert } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import BulkUploadAgendaModal from './BulkUploadAgendaModal';
import { uploadAgendas } from '../../services/agendaService';

const AdminPage = () => {
    const { user } = useAuth();
    const [showAgendaModal, setShowAgendaModal] = useState(false);
    const [agendaSuccess, setAgendaSuccess] = useState(null);
    const [agendaError, setAgendaError] = useState(null);

    const handleUploadAgendas = async (agendas) => {
        try {
            const response = await uploadAgendas(agendas);
            setAgendaSuccess(
                `✓ Téléversement terminé : ${response.successfullyProcessed}/${response.totalVolunteers} agenda(s) traité(s) avec succès.`
            );
            setAgendaError(null);
            setShowAgendaModal(false);
        } catch (err) {
            setAgendaError(err.message);
            setShowAgendaModal(false);
        }
    };

    return (
        <Container className="py-4">
            <h1 className="mb-4">🛠️ Administration</h1>
            <p className="text-muted mb-4">Bienvenue, {user?.email}</p>

            {agendaSuccess && (
                <Alert variant="success" dismissible onClose={() => setAgendaSuccess(null)}>
                    {agendaSuccess}
                </Alert>
            )}
            {agendaError && (
                <Alert variant="danger" dismissible onClose={() => setAgendaError(null)}>
                    {agendaError}
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
                            <Card.Title>🗓️ Agendas bénévoles</Card.Title>
                            <Card.Text>
                                Téléverser les agendas des bénévoles au format JSON.
                                L'agenda remplace les tâches existantes de chaque bénévole mentionné.
                            </Card.Text>
                            <Button variant="secondary" onClick={() => setShowAgendaModal(true)}>
                                Téléverser les agendas
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

            {showAgendaModal && (
                <BulkUploadAgendaModal
                    onClose={() => setShowAgendaModal(false)}
                    onUpload={handleUploadAgendas}
                />
            )}
        </Container>
    );
};

export default AdminPage;