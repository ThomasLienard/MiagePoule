import React, { useState, useRef } from 'react';
import { Modal, Form, Button, Alert, Table, Spinner, Badge } from 'react-bootstrap';
import PropTypes from 'prop-types';

const BulkCreateUsersModal = ({ onClose, onBulkCreate }) => {
    const [users, setUsers] = useState([]);
    const [errors, setErrors] = useState([]);
    const [submitting, setSubmitting] = useState(false);
    const [apiError, setApiError] = useState(null);
    const [preview, setPreview] = useState(false);
    const fileInputRef = useRef(null);

    const validateUserData = (user, index) => {
        const errors = [];
        
        if (!user.name?.trim()) {
            errors.push(`Ligne ${index + 1}: Le prénom est requis`);
        }
        if (!user.lastname?.trim()) {
            errors.push(`Ligne ${index + 1}: Le nom est requis`);
        }
        if (!user.email?.trim()) {
            errors.push(`Ligne ${index + 1}: L'email est requis`);
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(user.email)) {
            errors.push(`Ligne ${index + 1}: L'email "${user.email}" n'est pas valide`);
        }
        if (!user.roleName?.trim()) {
            errors.push(`Ligne ${index + 1}: Le rôle est requis`);
        }
        
        return errors;
    };

    const handleFileChange = async (e) => {
        const selectedFile = e.target.files[0];
        setErrors([]);
        setApiError(null);
        setUsers([]);
        setPreview(false);

        if (selectedFile) {
            try {
                const text = await selectedFile.text();
                const jsonData = JSON.parse(text);
                
                let userList = [];
                if (Array.isArray(jsonData)) {
                    userList = jsonData;
                } else if (jsonData.users && Array.isArray(jsonData.users)) {
                    userList = jsonData.users;
                } else {
                    setErrors(['Le fichier JSON doit contenir un tableau d\'utilisateurs']);
                    return;
                }

                // Valider chaque utilisateur
                const validationErrors = [];
                userList.forEach((user, index) => {
                    const userErrors = validateUserData(user, index);
                    validationErrors.push(...userErrors);
                });

                if (validationErrors.length > 0) {
                    setErrors(validationErrors);
                } else {
                    setUsers(userList);
                    setPreview(true);
                }
            } catch (error) {
                setErrors(['Erreur lors de la lecture du fichier JSON: ' + error.message]);
            }
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (users.length === 0) {
            setApiError('Aucun utilisateur à créer');
            return;
        }

        setSubmitting(true);
        setApiError(null);
        
        try {
            await onBulkCreate({ users });
        } catch (err) {
            setApiError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const downloadTemplate = () => {
        const template = [
            {
                email: "email@example.com",
                name: "John",
                lastname: "Doe",
                roleName: "ATHLETE",
                countryCode: "FR"
            }
        ];
        
        const jsonString = JSON.stringify(template, null, 2);
        const blob = new Blob([jsonString], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'template_utilisateurs.json';
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
    };

    return (
        <Modal show={true} onHide={onClose} size="lg" centered>
            <Modal.Header closeButton>
                <Modal.Title>📄 Import en masse d'utilisateurs</Modal.Title>
            </Modal.Header>
            
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {apiError && (
                        <Alert variant="danger">{apiError}</Alert>
                    )}

                    {errors.length > 0 && (
                        <Alert variant="danger">
                            <Alert.Heading>Erreurs de validation</Alert.Heading>
                            <ul className="mb-0">
                                {errors.map((error) => (
                                    <li key={error}>{error}</li>
                                ))}
                            </ul>
                        </Alert>
                    )}

                    <div className="mb-3">
                        <p className="text-muted">
                            Importez un fichier JSON contenant une liste d'utilisateurs à créer.
                            Chaque utilisateur doit avoir les champs suivants :
                        </p>
                        <ul className="text-muted">
                            <li><code>email</code> : Adresse email (obligatoire)</li>
                            <li><code>name</code> : Prénom (obligatoire)</li>
                            <li><code>lastname</code> : Nom (obligatoire)</li>
                            <li><code>roleName</code> : Rôle (ATHLETE, VOLONTAIRE, COMMISSAIRE, ADMIN, SPECTATEUR) (obligatoire)</li>
                            <li><code>countryCode</code> : Code pays (optionnel, ex: FR)</li>
                        </ul>
                        <Button 
                            variant="link" 
                            className="p-0 mb-3"
                            onClick={downloadTemplate}
                        >
                            📄 Télécharger un fichier template
                        </Button>
                    </div>

                    <Form.Group className="mb-3">
                        <Form.Label>Fichier JSON</Form.Label>
                        <Form.Control
                            ref={fileInputRef}
                            type="file"
                            accept=".json"
                            onChange={handleFileChange}
                        />
                    </Form.Group>

                    {preview && users.length > 0 && (
                        <div>
                            <h6 className="mb-3">
                                Aperçu : <Badge bg="primary">{users.length} utilisateur(s)</Badge>
                            </h6>
                            <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                                <Table striped bordered hover size="sm">
                                    <thead>
                                        <tr>
                                            <th>#</th>
                                            <th>Email</th>
                                            <th>Prénom</th>
                                            <th>Nom</th>
                                            <th>Rôle</th>
                                            <th>Pays</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {users.map((user, index) => (
                                            <tr key={user.email}>
                                                <td>{index + 1}</td>
                                                <td>{user.email}</td>
                                                <td>{user.name}</td>
                                                <td>{user.lastname}</td>
                                                <td>{user.roleName}</td>
                                                <td>{user.countryCode || '-'}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </Table>
                            </div>
                        </div>
                    )}
                </Modal.Body>
                
                <Modal.Footer>
                    <Button variant="secondary" onClick={onClose} disabled={submitting}>
                        Annuler
                    </Button>
                    <Button 
                        variant="secondary" 
                        type="submit" 
                        disabled={users.length === 0 || submitting}
                    >
                        {submitting ? (
                            <>
                                <Spinner
                                    as="span"
                                    animation="border"
                                    size="sm"
                                    role="status"
                                    aria-hidden="true"
                                    className="me-2"
                                />
                                Création en cours...
                            </>
                        ) : (
                            `Créer ${users.length} utilisateur(s)`
                        )}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
};

BulkCreateUsersModal.propTypes = {
    onClose: PropTypes.func.isRequired,
    onBulkCreate: PropTypes.func.isRequired
};

export default BulkCreateUsersModal;
