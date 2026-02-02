import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Form, Alert, Spinner, Badge, ButtonGroup } from 'react-bootstrap';
import adminUserService from '../../services/adminUserService';
import UserList from './UserList';
import CreateUserModal from './CreateUserModal';
import UserDetailsModal from './UserDetailsModal';
import Plus from '../../assets/ajouter.png'

const UserManagement = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [selectedRole, setSelectedRole] = useState('');
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');

    const roles = [
        { value: '', label: 'Tous les rôles' },
        { value: 'ATHLETE', label: 'Sportifs' },
        { value: 'VOLONTAIRE', label: 'Volontaires' },
        { value: 'COMMISSAIRE', label: 'Commissaires' },
        { value: 'ADMIN', label: 'Administrateurs' },
        { value: 'SPECTATEUR', label: 'Spectateurs' }
    ];

    useEffect(() => {
        loadUsers();
    }, [selectedRole]);

    const loadUsers = async () => {
        try {
            setLoading(true);
            const data = selectedRole 
                ? await adminUserService.getUsersByRole(selectedRole)
                : await adminUserService.getAllUsers();
            setUsers(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateUser = async (userData) => {
        try {
            const response = await adminUserService.createUser(userData);
            setSuccess(`Compte créé avec succès ! Mot de passe temporaire: ${response.temporaryPassword}`);
            setShowCreateModal(false);
            loadUsers();
        } catch (err) {
            throw err;
        }
    };

    const handleUpdateUser = async (id, userData) => {
        try {
            await adminUserService.updateUser(id, userData);
            setSuccess('Utilisateur mis à jour avec succès');
            setSelectedUser(null);
            loadUsers();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleDeactivateUser = async (id, reason) => {
        try {
            await adminUserService.deactivateUser(id, reason);
            setSuccess('Compte désactivé avec succès');
            setSelectedUser(null);
            loadUsers();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleReactivateUser = async (id) => {
        try {
            await adminUserService.reactivateUser(id);
            setSuccess('Compte réactivé avec succès');
            setSelectedUser(null);
            loadUsers();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleResetPassword = async (id) => {
        try {
            const response = await adminUserService.resetPassword(id);
            setSuccess(`Mot de passe réinitialisé ! Nouveau mot de passe: ${response.temporaryPassword}`);
            loadUsers();
        } catch (err) {
            setError(err.message);
        }
    };

    const filteredUsers = users.filter(user => {
        if (!searchTerm) return true;
        const search = searchTerm.toLowerCase();
        return (
            user.name?.toLowerCase().includes(search) ||
            user.lastname?.toLowerCase().includes(search) ||
            user.email?.toLowerCase().includes(search)
        );
    });

    const stats = {
        total: users.length,
        active: users.filter(u => u.isActive).length,
        pending: users.filter(u => !u.isAccountActivated && u.isActive).length
    };

    return (
        <Container className="py-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h1>👥 Gestion des comptes</h1>
                    <p className="text-muted">Créer et gérer les comptes utilisateurs</p>
                </div>
                <Button variant="secondary" onClick={() => setShowCreateModal(true)}>
                    <img src={Plus} alt="Nouveau compte" style={{height: '20px', marginRight: '8px', marginBottom: '3px'}} />
                    {' '}
                    Nouveau compte
                </Button>
            </div>

            {error && (
                <Alert variant="danger" dismissible onClose={() => setError(null)}>
                    {error}
                </Alert>
            )}
            
            {success && (
                <Alert variant="success" dismissible onClose={() => setSuccess(null)}>
                    {success}
                </Alert>
            )}

            {/* Stats cards */}
            <Row className="mb-4">
                <Col md={4}>
                    <Card className="text-center">
                        <Card.Body>
                            <h3>{stats.total}</h3>
                            <small className="text-muted">Comptes total</small>
                        </Card.Body>
                    </Card>
                </Col>
                <Col md={4}>
                    <Card className="text-center">
                        <Card.Body>
                            <h3 className="text-success">{stats.active}</h3>
                            <small className="text-muted">Comptes actifs</small>
                        </Card.Body>
                    </Card>
                </Col>
                <Col md={4}>
                    <Card className="text-center">
                        <Card.Body>
                            <h3 className="text-warning">{stats.pending}</h3>
                            <small className="text-muted">En attente d'activation</small>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            {/* Filters */}
            <Card className="mb-4">
                <Card.Body>
                    <Row>
                        <Col md={6}>
                            <Form.Group>
                                <Form.Label>Filtrer par rôle</Form.Label>
                                <Form.Select
                                    value={selectedRole}
                                    onChange={(e) => setSelectedRole(e.target.value)}
                                >
                                    {roles.map(role => (
                                        <option key={role.value} value={role.value}>
                                            {role.label}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Form.Group>
                        </Col>
                        <Col md={6}>
                            <Form.Group>
                                <Form.Label>Rechercher</Form.Label>
                                <Form.Control
                                    type="text"
                                    placeholder="Nom, prénom ou email..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                />
                            </Form.Group>
                        </Col>
                    </Row>
                </Card.Body>
            </Card>

            {/* User list */}
            {loading ? (
                <div className="text-center py-5">
                    <Spinner animation="border" variant="primary" />
                    <p className="mt-3">Chargement des utilisateurs...</p>
                </div>
            ) : (
                <UserList
                    users={filteredUsers}
                    onSelectUser={setSelectedUser}
                    onResetPassword={handleResetPassword}
                />
            )}

            {/* Modals */}
            {showCreateModal && (
                <CreateUserModal
                    onClose={() => setShowCreateModal(false)}
                    onCreate={handleCreateUser}
                    roles={roles.filter(r => r.value && r.value !== 'ADMIN')}
                />
            )}

            {selectedUser && (
                <UserDetailsModal
                    user={selectedUser}
                    roles={roles.filter(r => r.value)}
                    onClose={() => setSelectedUser(null)}
                    onUpdate={handleUpdateUser}
                    onDeactivate={handleDeactivateUser}
                    onReactivate={handleReactivateUser}
                    onResetPassword={handleResetPassword}
                />
            )}
        </Container>
    );
};

export default UserManagement;
