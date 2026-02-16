import React from 'react';
import { Modal, Button, Alert, Table, Badge } from 'react-bootstrap';
import PropTypes from 'prop-types';

const BulkCreateResultsModal = ({ results, onClose }) => {
    const { totalRequested, successfullyCreated, failed, results: details } = results;
    const successfulUsers = details.filter(r => r.success);
    const failedUsers = details.filter(r => !r.success);

    return (
        <Modal show={true} onHide={onClose} size="lg" centered>
            <Modal.Header closeButton>
                <Modal.Title>Résultats de l'import</Modal.Title>
            </Modal.Header>
            
            <Modal.Body>
                {/* Summary */}
                <div className="mb-4">
                    <h5>Résumé</h5>
                    <div className="d-flex gap-3">
                        <div>
                            <Badge bg="secondary" className="fs-6">
                                Total: {totalRequested}
                            </Badge>
                        </div>
                        {successfullyCreated > 0 && (
                            <div>
                                <Badge bg="success" className="fs-6">
                                    ✓ Succès: {successfullyCreated}
                                </Badge>
                            </div>
                        )}
                        {failed > 0 && (
                            <div>
                                <Badge bg="danger" className="fs-6">
                                    ✗ Échecs: {failed}
                                </Badge>
                            </div>
                        )}
                    </div>
                </div>

                {/* Successful creations */}
                {successfulUsers.length > 0 && (
                    <div className="mb-4">
                        <Alert variant="success">
                            <Alert.Heading className="h6">
                                ✓ {successfulUsers.length} utilisateur(s) créé(s) avec succès
                            </Alert.Heading>
                            <div style={{ maxHeight: '200px', overflowY: 'auto' }}>
                                <Table striped size="sm" className="mb-0">
                                    <thead>
                                        <tr>
                                            <th>Email</th>
                                            <th>Mot de passe temporaire</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {successfulUsers.map((user) => (
                                            <tr key={user.email}>
                                                <td>{user.email}</td>
                                                <td>
                                                    <code className="bg-light px-2 py-1 rounded">
                                                        {user.temporaryPassword}
                                                    </code>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </Table>
                            </div>
                            <div className="mt-3">
                                <Alert variant="info" className="mb-0 py-2">
                                    <small>
                                        ✉️ <strong>Emails envoyés :</strong> Tous les utilisateurs créés avec succès ont reçu un email d'activation contenant leur mot de passe provisoire.
                                    </small>
                                </Alert>
                            </div>
                            <div className="mt-2 small text-muted">
                                💡 Conservez ces mots de passe temporaires en backup
                            </div>
                        </Alert>
                    </div>
                )}

                {/* Failed creations */}
                {failedUsers.length > 0 && (
                    <div className="mb-3">
                        <Alert variant="danger">
                            <Alert.Heading className="h6">
                                ✗ {failedUsers.length} échec(s)
                            </Alert.Heading>
                            <div style={{ maxHeight: '200px', overflowY: 'auto' }}>
                                <Table striped size="sm" className="mb-0">
                                    <thead>
                                        <tr>
                                            <th>Email</th>
                                            <th>Raison</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {failedUsers.map((user) => (
                                            <tr key={user.email}>
                                                <td>{user.email}</td>
                                                <td className="text-danger small">{user.message}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </Table>
                            </div>
                        </Alert>
                    </div>
                )}
            </Modal.Body>
            
            <Modal.Footer>
                <Button variant="secondary" onClick={onClose}>
                    Fermer
                </Button>
            </Modal.Footer>
        </Modal>
    );
};

BulkCreateResultsModal.propTypes = {
    results: PropTypes.shape({
        totalRequested: PropTypes.number.isRequired,
        successfullyCreated: PropTypes.number.isRequired,
        failed: PropTypes.number.isRequired,
        results: PropTypes.arrayOf(
            PropTypes.shape({
                email: PropTypes.string.isRequired,
                success: PropTypes.bool.isRequired,
                message: PropTypes.string.isRequired,
                temporaryPassword: PropTypes.string
            })
        ).isRequired
    }).isRequired,
    onClose: PropTypes.func.isRequired
};

export default BulkCreateResultsModal;
