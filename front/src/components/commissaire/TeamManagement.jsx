import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Alert, Spinner, Table, Badge } from 'react-bootstrap';
import { getAllTeams, deleteTeam, createTeam, updateTeam } from '../../services/teamService';
import { getAllCountries } from '../../services/countryService';
import commissaireUserService from '../../services/commissaireUserService';
import CreateTeamModal from './CreateTeamModal';
import UpdateTeamModal from './UpdateTeamModal';

const TeamManagement = () => {
    const [teams, setTeams] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showUpdateModal, setShowUpdateModal] = useState(false);
    const [selectedTeam, setSelectedTeam] = useState(null);
    const [countries, setCountries] = useState([]);
    const [athletes, setAthletes] = useState([]);

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            setLoading(true);
            const [teamsData, countriesData, athletesData] = await Promise.all([
                getAllTeams(),
                getAllCountries(),
                commissaireUserService.getUsersByRole('ATHLETE')
            ]);
            setTeams(teamsData);
            setCountries(countriesData);
            setAthletes(athletesData);
            setError(null);
        } catch (err) {
            setError(err.response?.data?.message || "Erreur lors du chargement des données");
        } finally {
            setLoading(false);
        }
    };

    const handleCreateTeam = async (teamData) => {
        try {
            await createTeam(teamData);
            setSuccess('Équipe créée avec succès');
            setShowCreateModal(false);
            loadData();
            setTimeout(() => setSuccess(null), 3000);
        } catch (err) {
            setError(err.response?.data?.message || "Erreur lors de la création de l'équipe");
            setTimeout(() => setError(null), 5000);
        }
    };

    const handleUpdateTeam = async (teamData) => {
        try {
            await updateTeam(selectedTeam.id, teamData);
            setSuccess('Équipe mise à jour avec succès');
            setShowUpdateModal(false);
            setSelectedTeam(null);
            loadData();
            setTimeout(() => setSuccess(null), 3000);
        } catch (err) {
            setError(err.response?.data?.message || "Erreur lors de la mise à jour de l'équipe");
            setTimeout(() => setError(null), 5000);
        }
    };

    const handleDeleteTeam = async (teamId) => {
        // eslint-disable-next-line no-restricted-globals
        if (confirm('Êtes-vous sûr de vouloir supprimer cette équipe ?')) {
            try {
                await deleteTeam(teamId);
                setSuccess('Équipe supprimée avec succès');
                loadData();
                setTimeout(() => setSuccess(null), 3000);
            } catch (err) {
                setError(err.response?.data?.message || "Erreur lors de la suppression de l'équipe");
                setTimeout(() => setError(null), 5000);
            }
        }
    };

    const handleEditTeam = (team) => {
        setSelectedTeam(team);
        setShowUpdateModal(true);
    };

    const getAthleteNames = (members) => {
        if (!members || members.length === 0) return 'Aucun membre';
        return members.map(member => `${member.name} ${member.lastname}`)
            .join(', ');
    };

    if (loading) {
        return (
            <Container className="text-center mt-5">
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">Chargement...</span>
                </Spinner>
            </Container>
        );
    }

    return (
        <Container className="mt-4">
            <Row className="mb-4">
                <Col>
                    <h2>Gestion des Équipes</h2>
                </Col>
                <Col className="text-end">
                    <Button variant="secondary" onClick={() => setShowCreateModal(true)}>
                        + Créer une équipe
                    </Button>
                </Col>
            </Row>

            {error && (
                <Alert variant="danger" onClose={() => setError(null)} dismissible>
                    {error}
                </Alert>
            )}

            {success && (
                <Alert variant="success" onClose={() => setSuccess(null)} dismissible>
                    {success}
                </Alert>
            )}

            <Card>
                <Card.Body>
                    {teams.length === 0 ? (
                        <p className="text-center text-muted">Aucune équipe enregistrée</p>
                    ) : (
                        <Table responsive hover>
                            <thead>
                                <tr>
                                    <th>Nom</th>
                                    <th>Pays</th>
                                    <th>Membres</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {teams.map(team => (
                                    <tr key={team.id}>
                                        <td>{team.name}</td>
                                        <td>
                                            <Badge bg="secondary">{team.countryCode}</Badge>
                                        </td>
                                        <td>
                                            <small>{getAthleteNames(team.members)}</small>
                                        </td>
                                        <td>
                                            <Button 
                                                variant="outline-secondary" 
                                                size="sm" 
                                                className="me-2"
                                                onClick={() => handleEditTeam(team)}
                                            >
                                                Modifier
                                            </Button>
                                            <Button 
                                                variant="outline-danger" 
                                                size="sm"
                                                onClick={() => handleDeleteTeam(team.id)}
                                            >
                                                Supprimer
                                            </Button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    )}
                </Card.Body>
            </Card>

            <CreateTeamModal
                show={showCreateModal}
                onHide={() => setShowCreateModal(false)}
                onSubmit={handleCreateTeam}
                countries={countries}
                athletes={athletes}
            />

            {selectedTeam && (
                <UpdateTeamModal
                    show={showUpdateModal}
                    onHide={() => {
                        setShowUpdateModal(false);
                        setSelectedTeam(null);
                    }}
                    onSubmit={handleUpdateTeam}
                    team={selectedTeam}
                    countries={countries}
                    athletes={athletes}
                />
            )}
        </Container>
    );
};

export default TeamManagement;
