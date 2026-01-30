import React, { useState } from 'react';
import { Modal, Button, Form, Row, Col, Badge, Alert, Spinner } from 'react-bootstrap';

const UserDetailsModal = ({ 
    user, 
    roles, 
    onClose, 
    onUpdate, 
    onDeactivate, 
    onReactivate, 
    onResetPassword 
}) => {
    const [isEditing, setIsEditing] = useState(false);
    const [formData, setFormData] = useState({
        name: user.name || '',
        lastname: user.lastname || '',
        email: user.email || '',
        roleName: user.roleName || '',
        countryCode: user.countryCode || ''
    });
    const [deactivateReason, setDeactivateReason] = useState('');
    const [showDeactivateConfirm, setShowDeactivateConfirm] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSave = async () => {
        setSubmitting(true);
        try {
            await onUpdate(user.id, formData);
            setIsEditing(false);
        } finally {
            setSubmitting(false);
        }
    };

    const handleDeactivate = async () => {
        if (!deactivateReason.trim()) {
            alert('Veuillez saisir une raison de désactivation');
            return;
        }
        setSubmitting(true);
        try {
            await onDeactivate(user.id, deactivateReason);
        } finally {
            setSubmitting(false);
            setShowDeactivateConfirm(false);
        }
    };

    const handleReactivate = async () => {
        setSubmitting(true);
        try {
            await onReactivate(user.id);
        } finally {
            setSubmitting(false);
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return '-';
        return new Date(dateString).toLocaleDateString('fr-FR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getStatusInfo = () => {
        if (!user.isActive) {
            return { status: 'Désactivé', variant: 'danger', icon: '🚫' };
        }
        if (!user.isAccountActivated) {
            return { status: "En attente d'activation", variant: 'warning', icon: '⏳' };
        }
        return { status: 'Actif', variant: 'success', icon: '✅' };
    };

    const getRoleBadgeVariant = (roleName) => {
        const variants = {
            'ADMIN': 'danger',
            'COMMISSAIRE': 'warning',
            'ATHLETE': 'primary',
            'VOLONTAIRE': 'success',
            'SPECTATEUR': 'secondary'
        };
        return variants[roleName] || 'secondary';
    };

    const statusInfo = getStatusInfo();

    return (
        <Modal show={true} onHide={onClose} size="lg" centered>
            <Modal.Header closeButton>
                <Modal.Title>👤 Détails de l'utilisateur</Modal.Title>
            </Modal.Header>

            <Modal.Body>
                {/* Status Banner */}
                <Alert variant={statusInfo.variant} className="d-flex align-items-center justify-content-between">
                    <span>{statusInfo.icon} {statusInfo.status}</span>
                    {user.deactivationReason && (
                        <small>Raison: {user.deactivationReason}</small>
                    )}
                </Alert>

                {/* User Info */}
                <Row>
                    <Col md={6}>
                        <h6 className="text-muted border-bottom pb-2 mb-3">Identité</h6>
                        {isEditing ? (
                            <>
                                <Form.Group className="mb-3">
                                    <Form.Label>Prénom</Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="name"
                                        value={formData.name}
                                        onChange={handleChange}
                                    />
                                </Form.Group>
                                <Form.Group className="mb-3">
                                    <Form.Label>Nom</Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="lastname"
                                        value={formData.lastname}
                                        onChange={handleChange}
                                    />
                                </Form.Group>
                                <Form.Group className="mb-3">
                                    <Form.Label>Email</Form.Label>
                                    <Form.Control
                                        type="email"
                                        name="email"
                                        value={formData.email}
                                        onChange={handleChange}
                                    />
                                </Form.Group>
                            </>
                        ) : (
                            <>
                                <p><strong>Prénom:</strong> {user.name}</p>
                                <p><strong>Nom:</strong> {user.lastname}</p>
                                <p><strong>Email:</strong> {user.email}</p>
                            </>
                        )}
                    </Col>

                    <Col md={6}>
                        <h6 className="text-muted border-bottom pb-2 mb-3">Rôle</h6>
                        {isEditing ? (
                            <>
                                <Form.Group className="mb-3">
                                    <Form.Label>Rôle</Form.Label>
                                    <Form.Select
                                        name="roleName"
                                        value={formData.roleName}
                                        onChange={handleChange}
                                    >
                                        {roles.map(role => (
                                            <option key={role.value} value={role.value}>
                                                {role.label}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                                <Form.Group className="mb-3">
                                    <Form.Label>Code Pays</Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="countryCode"
                                        value={formData.countryCode}
                                        onChange={handleChange}
                                        maxLength={2}
                                    />
                                </Form.Group>
                            </>
                        ) : (
                            <>
                                <p><strong>Rôle:</strong> <Badge bg={getRoleBadgeVariant(user.roleName)}>{user.roleName}</Badge></p>
                                <p><strong>Code Pays:</strong> {user.countryCode || '-'}</p>
                            </>
                        )}
                    </Col>
                </Row>

                <Row className="mt-4">
                    <Col md={12}>
                        <h6 className="text-muted border-bottom pb-2 mb-3">Informations du compte</h6>
                        <Row>
                            <Col sm={6}>
                                <p><strong>ID:</strong> {user.id}</p>
                                <p><strong>Créé le:</strong> {formatDate(user.createdAt)}</p>
                                <p><strong>Créé par:</strong> {user.createdBy || 'Auto-inscription'}</p>
                            </Col>
                            <Col sm={6}>
                                <p><strong>Compte activé:</strong> {user.isAccountActivated ? 'Oui' : 'Non'}</p>
                                <p><strong>Doit changer MDP:</strong> {user.mustChangePassword ? 'Oui' : 'Non'}</p>
                                {user.deactivatedAt && (
                                    <p><strong>Désactivé le:</strong> {formatDate(user.deactivatedAt)}</p>
                                )}
                            </Col>
                        </Row>
                    </Col>
                </Row>

                {/* Deactivate Confirmation */}
                {showDeactivateConfirm && (
                    <Alert variant="danger" className="mt-4">
                        <h6>⚠️ Désactivation du compte</h6>
                        <p>Cette action empêchera l'utilisateur de se connecter.</p>
                        <Form.Group className="mb-3">
                            <Form.Label>Raison de la désactivation *</Form.Label>
                            <Form.Control
                                as="textarea"
                                value={deactivateReason}
                                onChange={(e) => setDeactivateReason(e.target.value)}
                                placeholder="Ex: Retrait d'accréditation, forfait définitif..."
                                rows={3}
                            />
                        </Form.Group>
                        <div className="d-flex gap-2">
                            <Button 
                                variant="secondary"
                                onClick={() => setShowDeactivateConfirm(false)}
                            >
                                Annuler
                            </Button>
                            <Button 
                                variant="danger"
                                onClick={handleDeactivate}
                                disabled={submitting}
                            >
                                {submitting && <Spinner animation="border" size="sm" className="me-2" />}
                                Confirmer la désactivation
                            </Button>
                        </div>
                    </Alert>
                )}
            </Modal.Body>

            <Modal.Footer>
                {isEditing ? (
                    <>
                        <Button 
                            variant="secondary"
                            onClick={() => setIsEditing(false)}
                            disabled={submitting}
                        >
                            Annuler
                        </Button>
                        <Button 
                            variant="primary"
                            onClick={handleSave}
                            disabled={submitting}
                        >
                            {submitting && <Spinner animation="border" size="sm" className="me-2" />}
                            Enregistrer
                        </Button>
                    </>
                ) : (
                    <>
                        <Button variant="secondary" onClick={onClose}>
                            Fermer
                        </Button>
                        
                        {!user.isAccountActivated && user.isActive && (
                            <Button 
                                variant="warning"
                                onClick={() => onResetPassword(user.id)}
                            >
                                🔑 Réinitialiser MDP
                            </Button>
                        )}
                        
                        <Button 
                            variant="info"
                            onClick={() => setIsEditing(true)}
                        >
                            ✏️ Modifier
                        </Button>
                        
                        {user.isActive ? (
                            // Ne pas afficher le bouton désactiver pour les ADMIN
                            user.roleName !== 'ADMIN' && (
                                <Button 
                                    variant="danger"
                                    onClick={() => setShowDeactivateConfirm(true)}
                                >
                                    🚫 Désactiver
                                </Button>
                            )
                        ) : (
                            <Button 
                                variant="success"
                                onClick={handleReactivate}
                                disabled={submitting}
                            >
                                {submitting && <Spinner animation="border" size="sm" className="me-2" />}
                                ✅ Réactiver
                            </Button>
                        )}
                    </>
                )}
            </Modal.Footer>
        </Modal>
    );
};

export default UserDetailsModal;