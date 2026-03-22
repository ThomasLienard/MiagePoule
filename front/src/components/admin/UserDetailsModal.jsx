import React, { useMemo, useState, useEffect } from 'react';
import { Modal, Button, Form, Row, Col, Badge, Alert, Spinner, Card, ListGroup, Tabs, Tab } from 'react-bootstrap';
import { FileText, CheckCircle, XCircle, Download } from 'lucide-react';
import axios from 'axios';

const UserDetailsModal = ({ 
    user, 
    roles, 
    onClose, 
    onUpdate, 
    onDeactivate, 
    onReactivate, 
    onResetPassword,
    onValidateAccount,
    onInvalidateAccount
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
    const [documents, setDocuments] = useState([]);
    const [loadingDocuments, setLoadingDocuments] = useState(false);
    const [activeTab, setActiveTab] = useState('info');

    const API_BASE_URL = 'http://localhost:8084';

    const getAuthHeaders = () => {
        const token = localStorage.getItem('token');
        return {
            'Authorization': `Bearer ${token}`
        };
    };

    // Charger les documents de l'utilisateur
    useEffect(() => {
        if (activeTab === 'documents') {
            loadUserDocuments();
        }
    }, [activeTab, user.id]);

    const loadUserDocuments = async () => {
        setLoadingDocuments(true);
        try {
            const response = await axios.get(
                `${API_BASE_URL}/admin/users/${user.id}/documents`,
                { headers: getAuthHeaders() }
            );
            setDocuments(response.data);
        } catch (error) {
            console.error('Erreur lors du chargement des documents:', error);
        } finally {
            setLoadingDocuments(false);
        }
    };

    const downloadDocument = async (documentId, fileName) => {
        try {
            const response = await axios.get(
                `${API_BASE_URL}/admin/users/${user.id}/documents/${documentId}/download`,
                {
                    headers: getAuthHeaders(),
                    responseType: 'blob'
                }
            );

            // Créer un lien de téléchargement
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', fileName || 'document');
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            alert('Erreur lors du téléchargement du document');
        }
    };

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

    const handleValidateAccount = async () => {
        setSubmitting(true);
        try {
            await onValidateAccount(user.id);
        } finally {
            setSubmitting(false);
        }
    };

    const handleInvalidateAccount = async () => {
        setSubmitting(true);
        try {
            await onInvalidateAccount(user.id);
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
        if (!user.isAccountValidated) {
            return { status: 'Activé - En attente de validation', variant: 'info', icon: '⏳' };
        }
        return { status: 'Actif et Validé', variant: 'success', icon: '✅' };
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

    const getRequiredDocuments = (roleName) => {
        const role = roleName?.toUpperCase();
        if (role === 'VOLONTAIRE' || role === 'COMMISSAIRE') {
            return ['CEN_ACCREDITATION'];
        }
        if (role === 'ATHLETE') {
            return ['PASSPORT', 'MEDICAL_CERTIFICATE'];
        }
        return [];
    };

    const statusInfo = getStatusInfo();
    const requiredDocs = getRequiredDocuments(user.roleName);
    const uploadedRequiredDocsCount = useMemo(() => {
        if (!requiredDocs.length) return 0;
        const requiredSet = new Set(requiredDocs);
        const uploadedSet = new Set(
            documents
                .filter((d) => requiredSet.has(d.typeName))
                .map((d) => d.typeName)
        );
        return uploadedSet.size;
    }, [documents, requiredDocs]);

    return (
        <Modal show={true} onHide={onClose} size="xl" centered>
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

                <Tabs activeKey={activeTab} onSelect={(k) => setActiveTab(k)} className="mb-3">
                    <Tab eventKey="info" title="Informations">
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
                                        <p><strong>Compte activé:</strong> {user.isAccountActivated ? 'Oui ✅' : 'Non ❌'}</p>
                                        <p><strong>Compte validé:</strong> {user.isAccountValidated ? 'Oui ✅' : 'Non ❌'}</p>
                                        <p><strong>Doit changer MDP:</strong> {user.mustChangePassword ? 'Oui' : 'Non'}</p>
                                        {user.deactivatedAt && (
                                            <p><strong>Désactivé le:</strong> {formatDate(user.deactivatedAt)}</p>
                                        )}
                                    </Col>
                                </Row>
                            </Col>
                        </Row>
                    </Tab>

                    <Tab eventKey="documents" title={
                        <span>
                            📄 Documents
                            {requiredDocs.length > 0 && (
                                <Badge bg="secondary" className="ms-2">
                                    {loadingDocuments ? '—' : uploadedRequiredDocsCount}/{requiredDocs.length}
                                </Badge>
                            )}
                        </span>
                    }>
                        <div className="mt-3">
                            {loadingDocuments ? (
                                <div className="text-center py-4">
                                    <Spinner animation="border" />
                                    <p className="mt-2">Chargement des documents...</p>
                                </div>
                            ) : (
                                <>
                                    {requiredDocs.length > 0 && (
                                        <Alert variant="info" className="mb-3">
                                            <strong>Documents requis pour le rôle {user.roleName}:</strong>
                                            <ul className="mb-0 mt-2">
                                                {requiredDocs.map(doc => {
                                                    const hasDoc = documents.some(d => d.typeName === doc);
                                                    return (
                                                        <li key={doc}>
                                                            {hasDoc ? <CheckCircle size={16} className="text-success me-1" /> : <XCircle size={16} className="text-danger me-1" />}
                                                            {doc.replace('_', ' ')}
                                                        </li>
                                                    );
                                                })}
                                            </ul>
                                        </Alert>
                                    )}

                                    {documents.length === 0 ? (
                                        <Alert variant="warning">
                                            Aucun document déposé pour cet utilisateur.
                                        </Alert>
                                    ) : (
                                        <ListGroup>
                                            {documents.map((doc) => (
                                                <ListGroup.Item key={doc.id}>
                                                    <Row className="align-items-center">
                                                        <Col md={4}>
                                                            <FileText size={20} className="me-2" />
                                                            <strong>{doc.typeName}</strong>
                                                        </Col>
                                                        <Col md={4}>
                                                            <small className="text-muted">
                                                                Déposé le: {formatDate(doc.uploadedAt)}
                                                            </small>
                                                        </Col>
                                                        <Col md={4} className="text-end">
                                                            <Button
                                                                variant="outline-primary"
                                                                size="sm"
                                                                onClick={() => downloadDocument(doc.id, doc.fileName)}
                                                            >
                                                                <Download size={16} /> Télécharger
                                                            </Button>
                                                        </Col>
                                                    </Row>
                                                </ListGroup.Item>
                                            ))}
                                        </ListGroup>
                                    )}
                                </>
                            )}
                        </div>
                    </Tab>

                    {user.roleName === 'ATHLETE' && (
                        <Tab eventKey="charter" title={
                            <span>
                                📜 Charte
                                {user.hasSignedCharter ? ' ✅' : ' ❌'}
                            </span>
                        }>
                            <div className="mt-3">
                                <Card className={user.hasSignedCharter ? 'border-success' : 'border-warning'}>
                                    <Card.Body>
                                        <h5>Charte Européenne du Sport</h5>
                                        {user.hasSignedCharter ? (
                                            <Alert variant="success">
                                                <CheckCircle className="me-2" />
                                                La charte a été signée par l'athlète
                                            </Alert>
                                        ) : (
                                            <Alert variant="warning">
                                                <XCircle className="me-2" />
                                                La charte n'a pas encore été signée par l'athlète
                                            </Alert>
                                        )}
                                    </Card.Body>
                                </Card>
                            </div>
                        </Tab>
                    )}
                </Tabs>

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
                            variant="secondary"
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

                        {/* Boutons de validation du compte */}
                        {user.isAccountActivated && (
                            <>
                                {user.isAccountValidated ? (
                                    <Button
                                        variant="warning"
                                        onClick={handleInvalidateAccount}
                                        disabled={submitting}
                                    >
                                        {submitting && <Spinner animation="border" size="sm" className="me-2" />}
                                        ❌ Invalider le compte
                                    </Button>
                                ) : (
                                    <Button
                                        variant="success"
                                        onClick={handleValidateAccount}
                                        disabled={submitting}
                                    >
                                        {submitting && <Spinner animation="border" size="sm" className="me-2" />}
                                        ✅ Valider le compte
                                    </Button>
                                )}
                            </>
                        )}

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
