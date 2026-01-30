import React, { useState } from 'react';
import { Modal, Form, Button, Row, Col, Alert, Spinner } from 'react-bootstrap';

const CreateUserModal = ({ roles, onClose, onCreate }) => {
    const [formData, setFormData] = useState({
        name: '',
        lastname: '',
        email: '',
        roleName: 'ATHLETE',
        countryCode: 'FR'
    });
    const [errors, setErrors] = useState({});
    const [submitting, setSubmitting] = useState(false);
    const [apiError, setApiError] = useState(null);

    const validateForm = () => {
        const newErrors = {};
        
        if (!formData.name.trim()) {
            newErrors.name = 'Le prénom est requis';
        }
        if (!formData.lastname.trim()) {
            newErrors.lastname = 'Le nom est requis';
        }
        if (!formData.email.trim()) {
            newErrors.email = "L'email est requis";
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
            newErrors.email = "L'email n'est pas valide";
        }
        if (!formData.roleName) {
            newErrors.roleName = 'Le rôle est requis';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: null
            }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setApiError(null);
        
        if (!validateForm()) {
            return;
        }

        setSubmitting(true);
        try {
            await onCreate(formData);
        } catch (err) {
            setApiError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Modal show={true} onHide={onClose} size="lg" centered>
            <Modal.Header closeButton>
                <Modal.Title>➕ Créer un nouveau compte</Modal.Title>
            </Modal.Header>
            
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {apiError && (
                        <Alert variant="danger">{apiError}</Alert>
                    )}

                    <Row className="mb-3">
                        <Col md={6}>
                            <Form.Group>
                                <Form.Label>Prénom *</Form.Label>
                                <Form.Control
                                    type="text"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleChange}
                                    isInvalid={!!errors.name}
                                    placeholder="Jean"
                                />
                                <Form.Control.Feedback type="invalid">
                                    {errors.name}
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Col>
                        <Col md={6}>
                            <Form.Group>
                                <Form.Label>Nom *</Form.Label>
                                <Form.Control
                                    type="text"
                                    name="lastname"
                                    value={formData.lastname}
                                    onChange={handleChange}
                                    isInvalid={!!errors.lastname}
                                    placeholder="Dupont"
                                />
                                <Form.Control.Feedback type="invalid">
                                    {errors.lastname}
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Col>
                    </Row>

                    <Form.Group className="mb-3">
                        <Form.Label>Email *</Form.Label>
                        <Form.Control
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            isInvalid={!!errors.email}
                            placeholder="jean.dupont@email.com"
                        />
                        <Form.Control.Feedback type="invalid">
                            {errors.email}
                        </Form.Control.Feedback>
                    </Form.Group>

                    <Row className="mb-3">
                        <Col md={6}>
                            <Form.Group>
                                <Form.Label>Rôle *</Form.Label>
                                <Form.Select
                                    name="roleName"
                                    value={formData.roleName}
                                    onChange={handleChange}
                                    isInvalid={!!errors.roleName}
                                >
                                    {roles.map(role => (
                                        <option key={role.value} value={role.value}>
                                            {role.label}
                                        </option>
                                    ))}
                                </Form.Select>
                                <Form.Control.Feedback type="invalid">
                                    {errors.roleName}
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Col>
                        <Col md={6}>
                            <Form.Group>
                                <Form.Label>Code Pays</Form.Label>
                                <Form.Control
                                    type="text"
                                    name="countryCode"
                                    value={formData.countryCode}
                                    onChange={handleChange}
                                    maxLength={2}
                                    placeholder="FR"
                                />
                            </Form.Group>
                        </Col>
                    </Row>

                    <Alert variant="info">
                        <strong>ℹ️ Information:</strong><br/>
                        Un mot de passe temporaire sera généré: <code>{formData.lastname.toLowerCase()}.{formData.name.toLowerCase()}</code><br/>
                        L'utilisateur devra le changer à sa première connexion.
                    </Alert>
                </Modal.Body>

                <Modal.Footer>
                    <Button variant="secondary" onClick={onClose} disabled={submitting}>
                        Annuler
                    </Button>
                    <Button variant="primary" type="submit" disabled={submitting}>
                        {submitting ? (
                            <>
                                <Spinner animation="border" size="sm" className="me-2" />
                                Création...
                            </>
                        ) : 'Créer le compte'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
};

export default CreateUserModal;
