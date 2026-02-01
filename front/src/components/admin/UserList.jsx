import React from 'react';
import { Table, Badge, Button, Card } from 'react-bootstrap';

const UserList = ({ users, onSelectUser, onResetPassword }) => {
    const getRoleBadge = (role) => {
        const variants = {
            'ADMIN': 'danger',
            'COMMISSAIRE': 'warning',
            'ATHLETE': 'primary',
            'VOLONTAIRE': 'success',
            'SPECTATEUR': 'secondary'
        };
        return <Badge bg={variants[role] || 'secondary'}>{role}</Badge>;
    };

    const getStatusBadge = (user) => {
        if (!user.isActive) {
            return <Badge bg="danger">Désactivé</Badge>;
        }
        if (!user.isAccountActivated) {
            return <Badge bg="warning">En attente</Badge>;
        }
        return <Badge bg="success">Actif</Badge>;
    };

    if (users.length === 0) {
        return (
            <Card className="text-center py-5">
                <Card.Body>
                    <p className="text-muted mb-0">Aucun utilisateur trouvé</p>
                </Card.Body>
            </Card>
        );
    }

    return (
        <Card>
            <Table responsive hover className="mb-0">
                <thead className="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Nom</th>
                        <th>Prénom</th>
                        <th>Email</th>
                        <th>Rôle</th>
                        <th>Statut</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td>{user.id}</td>
                            <td>{user.lastname}</td>
                            <td>{user.name}</td>
                            <td>{user.email}</td>
                            <td>{getRoleBadge(user.roleName)}</td>
                            <td>{getStatusBadge(user)}</td>
                            <td>
                                <Button
                                    variant="outline-primary"
                                    size="sm"
                                    className="me-2"
                                    onClick={() => onSelectUser(user)}
                                >
                                    Détails
                                </Button>
                                {(
                                    <Button
                                        variant="outline-warning"
                                        size="sm"
                                        onClick={() => onResetPassword(user.id)}
                                    >
                                        Reset MDP
                                    </Button>
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </Table>
        </Card>
    );
};

export default UserList;