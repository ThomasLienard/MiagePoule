import React, { useState, useEffect } from 'react';
import {
    Card,
    Button,
    Table,
    Row,
    Col,
    Container,
    Spinner,
    Alert,
    Modal,
    Badge,
    Form,
    FloatingLabel,
    ProgressBar
} from 'react-bootstrap';
import {
    Eye,
    Download,
    Trash,
    Upload,
    FileText,
    Calendar,
    FilePdf,
    Image,
    File
} from 'react-bootstrap-icons';
import { documentService } from '../../services/documentService';
import { format } from 'date-fns';
import { fr } from 'date-fns/locale';

const TicketsPage = () => {
    const [tickets, setTickets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedTicket, setSelectedTicket] = useState(null);
    const [showPreview, setShowPreview] = useState(false);
    const [showUploadModal, setShowUploadModal] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [uploadProgress, setUploadProgress] = useState(0);
    const [deleteConfirm, setDeleteConfirm] = useState(null);

    // État pour le formulaire d'upload
    const [uploadForm, setUploadForm] = useState({
        file: null,
        typeId: '',
        description: ''
    });

    useEffect(() => {
        fetchTickets();
    }, []);

    const fetchTickets = async () => {
        try {
            setLoading(true);
            const data = await documentService.getUserTickets();
            setTickets(data);
            setError(null);
        } catch (err) {
            setError('Erreur lors du chargement des billets. Veuillez réessayer.');
            console.error('Erreur:', err);
        } finally {
            setLoading(false);
        }
    };

    const handlePreview = async (ticketId) => {
        try {
            const blob = await documentService.downloadTicket(ticketId);
            const url = URL.createObjectURL(blob);
            window.open(url, '_blank');
        } catch (err) {
            setError('Erreur lors de l\'ouverture du billet');
            console.error('Erreur:', err);
        }
    };

    const handleDownload = async (ticketId, fileName) => {
        try {
            const blob = await documentService.downloadTicket(ticketId);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = fileName || `ticket_${ticketId}.pdf`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch (err) {
            setError('Erreur lors du téléchargement du billet');
            console.error('Erreur:', err);
        }
    };

    const handleDelete = async (ticketId) => {
        try {
            await documentService.deleteTicket(ticketId);
            setTickets(tickets.filter(ticket => ticket.id !== ticketId));
            setDeleteConfirm(null);
        } catch (err) {
            setError('Erreur lors de la suppression du billet');
            console.error('Erreur:', err);
        }
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setUploadForm({
                ...uploadForm,
                file: file
            });
        }
    };

    const handleUpload = async () => {
        if (!uploadForm.file) {
            setError('Veuillez sélectionner un fichier');
            return;
        }

        try {
            setUploading(true);
            setUploadProgress(0);

            // Simulation de progression
            const progressInterval = setInterval(() => {
                setUploadProgress(prev => {
                    if (prev >= 90) {
                        clearInterval(progressInterval);
                        return prev;
                    }
                    return prev + 10;
                });
            }, 200);

            await documentService.uploadTicket(
                uploadForm.file,
                uploadForm.typeId,
                uploadForm.description
            );

            clearInterval(progressInterval);
            setUploadProgress(100);

            // Rafraîchir la liste
            await fetchTickets();

            // Réinitialiser le formulaire
            setUploadForm({
                file: null,
                typeId: '',
                description: ''
            });
            setShowUploadModal(false);

            setTimeout(() => setUploadProgress(0), 1000);
        } catch (err) {
            setError('Erreur lors de l\'upload du billet');
            console.error('Erreur:', err);
        } finally {
            setUploading(false);
        }
    };

    const getFileIcon = (fileName) => {
        if (!fileName) return <File className="me-2" />;

        const extension = fileName.split('.').pop().toLowerCase();
        switch (extension) {
            case 'pdf':
                return <FilePdf className="me-2 text-danger" />;
            case 'jpg':
            case 'jpeg':
            case 'png':
            case 'gif':
                return <Image className="me-2 text-success" />;
            default:
                return <FileText className="me-2 text-primary" />;
        }
    };

    const formatDate = (dateString) => {
        try {
            return format(new Date(dateString), 'dd MMMM yyyy à HH:mm', { locale: fr });
        } catch (e) {
            return dateString;
        }
    };

    const getTypeBadge = (typeId) => {
        const types = {
            1: { label: 'Entrée', variant: 'success' },
            2: { label: 'VIP', variant: 'warning' },
            3: { label: 'Standard', variant: 'info' },
            4: { label: 'Réduit', variant: 'secondary' }
        };

        const type = types[typeId] || { label: `Type ${typeId}`, variant: 'primary' };
        return <Badge bg={type.variant}>{type.label}</Badge>;
    };

    if (loading) {
        return (
            <Container className="py-5">
                <Row className="justify-content-center">
                    <Col md={6} className="text-center">
                        <Spinner animation="border" role="status">
                            <span className="visually-hidden">Chargement...</span>
                        </Spinner>
                        <p className="mt-3">Chargement de vos billets...</p>
                    </Col>
                </Row>
            </Container>
        );
    }

    return (
        <Container className="py-4">
            <Row className="mb-4">
                <Col>
                    <h2 className="mb-0">📄 Mes Billets</h2>
                    <p className="text-muted">Gérez vos billets d'événements</p>
                </Col>
                <Col className="text-end">
                    <Button
                        variant="primary"
                        onClick={() => setShowUploadModal(true)}
                        className="d-inline-flex align-items-center"
                    >
                        <Upload className="me-2" /> Ajouter un billet
                    </Button>
                </Col>
            </Row>

            {error && (
                <Alert variant="danger" dismissible onClose={() => setError(null)}>
                    {error}
                </Alert>
            )}

            {tickets.length === 0 ? (
                <Card className="text-center py-5">
                    <Card.Body>
                        <FileText size={48} className="text-muted mb-3" />
                        <h4>Aucun billet trouvé</h4>
                        <p className="text-muted">
                            Vous n'avez pas encore de billets. Commencez par en ajouter un !
                        </p>
                        <Button
                            variant="primary"
                            onClick={() => setShowUploadModal(true)}
                            className="d-inline-flex align-items-center"
                        >
                            <Upload className="me-2" /> Ajouter mon premier billet
                        </Button>
                    </Card.Body>
                </Card>
            ) : (
                <>
                    <Card className="mb-4">
                        <Card.Body>
                            <Table responsive hover>
                                <thead>
                                <tr>
                                    <th>Nom du fichier</th>
                                    <th>Type</th>
                                    <th>Description</th>
                                    <th>Date d'ajout</th>
                                    <th>Taille</th>
                                    <th className="text-center">Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {tickets.map((ticket) => (
                                    <tr key={ticket.id}>
                                        <td className="align-middle">
                                            <div className="d-flex align-items-center">
                                                {getFileIcon(ticket.fileName)}
                                                <span className="text-truncate" style={{ maxWidth: '200px' }}>
                                                        {ticket.fileName || `Ticket ${ticket.id}`}
                                                    </span>
                                            </div>
                                        </td>
                                        <td className="align-middle">
                                            {getTypeBadge(ticket.typeId)}
                                        </td>
                                        <td className="align-middle">
                                            {ticket.description || '-'}
                                        </td>
                                        <td className="align-middle">
                                            <div className="d-flex align-items-center">
                                                <Calendar className="me-2 text-muted" size={14} />
                                                {formatDate(ticket.createdAt || ticket.uploadDate)}
                                            </div>
                                        </td>
                                        <td className="align-middle">
                                            {ticket.fileSize ? `${(ticket.fileSize / 1024).toFixed(1)} KB` : '-'}
                                        </td>
                                        <td className="align-middle">
                                            <div className="d-flex justify-content-center gap-2">
                                                <Button
                                                    variant="outline-primary"
                                                    size="sm"
                                                    onClick={() => handlePreview(ticket.id)}
                                                    className="d-flex align-items-center"
                                                    title="Visualiser"
                                                >
                                                    <Eye size={16} />
                                                </Button>
                                                <Button
                                                    variant="outline-success"
                                                    size="sm"
                                                    onClick={() => handleDownload(ticket.id, ticket.fileName)}
                                                    className="d-flex align-items-center"
                                                    title="Télécharger"
                                                >
                                                    <Download size={16} />
                                                </Button>
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    onClick={() => setDeleteConfirm(ticket.id)}
                                                    className="d-flex align-items-center"
                                                    title="Supprimer"
                                                >
                                                    <Trash size={16} />
                                                </Button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </Table>
                        </Card.Body>
                    </Card>

                    <Row>
                        <Col className="text-muted">
                            <small>Total : {tickets.length} billet{tickets.length > 1 ? 's' : ''}</small>
                        </Col>
                    </Row>
                </>
            )}

            {/* Modal d'upload */}
            <Modal show={showUploadModal} onHide={() => setShowUploadModal(false)} size="lg">
                <Modal.Header closeButton>
                    <Modal.Title>📤 Ajouter un nouveau billet</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form>
                        <FloatingLabel controlId="floatingFile" label="Fichier du billet" className="mb-3">
                            <Form.Control
                                type="file"
                                accept=".pdf,.jpg,.jpeg,.png"
                                onChange={handleFileChange}
                                disabled={uploading}
                            />
                            <Form.Text className="text-muted">
                                Formats acceptés : PDF, JPG, PNG (max 10MB)
                            </Form.Text>
                            {uploadForm.file && (
                                <Alert variant="info" className="mt-2 py-2">
                                    <FileText className="me-2" />
                                    {uploadForm.file.name} ({(uploadForm.file.size / 1024).toFixed(1)} KB)
                                </Alert>
                            )}
                        </FloatingLabel>

                        <FloatingLabel controlId="floatingType" label="Type de billet" className="mb-3">
                            <Form.Select
                                value={uploadForm.typeId}
                                onChange={(e) => setUploadForm({...uploadForm, typeId: e.target.value})}
                                disabled={uploading}
                            >
                                <option value="">Sélectionnez un type</option>
                                <option value="1">Entrée</option>
                                <option value="2">VIP</option>
                                <option value="3">Standard</option>
                                <option value="4">Réduit</option>
                            </Form.Select>
                        </FloatingLabel>

                        <FloatingLabel controlId="floatingDescription" label="Description (optionnelle)">
                            <Form.Control
                                as="textarea"
                                placeholder="Description"
                                style={{ height: '100px' }}
                                value={uploadForm.description}
                                onChange={(e) => setUploadForm({...uploadForm, description: e.target.value})}
                                disabled={uploading}
                            />
                        </FloatingLabel>

                        {uploading && (
                            <div className="mt-3">
                                <ProgressBar
                                    now={uploadProgress}
                                    label={`${uploadProgress}%`}
                                    animated
                                    className="mb-2"
                                />
                                <p className="text-center text-muted">
                                    Upload en cours...
                                </p>
                            </div>
                        )}
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowUploadModal(false)} disabled={uploading}>
                        Annuler
                    </Button>
                    <Button
                        variant="primary"
                        onClick={handleUpload}
                        disabled={uploading || !uploadForm.file || !uploadForm.typeId}
                        className="d-flex align-items-center"
                    >
                        {uploading ? (
                            <>
                                <Spinner animation="border" size="sm" className="me-2" />
                                Upload...
                            </>
                        ) : (
                            <>
                                <Upload className="me-2" />
                                Uploader
                            </>
                        )}
                    </Button>
                </Modal.Footer>
            </Modal>

            {/* Modal de confirmation de suppression */}
            <Modal show={!!deleteConfirm} onHide={() => setDeleteConfirm(null)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>⚠️ Confirmation de suppression</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    Êtes-vous sûr de vouloir supprimer ce billet ? Cette action est irréversible.
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setDeleteConfirm(null)}>
                        Annuler
                    </Button>
                    <Button
                        variant="danger"
                        onClick={() => handleDelete(deleteConfirm)}
                        className="d-flex align-items-center"
                    >
                        <Trash className="me-2" />
                        Supprimer
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
};

export default TicketsPage;