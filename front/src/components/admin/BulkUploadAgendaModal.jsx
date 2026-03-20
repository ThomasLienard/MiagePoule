import React, { useState, useRef } from 'react';
import { Modal, Form, Button, Alert, Table, Spinner, Badge } from 'react-bootstrap';
import PropTypes from 'prop-types';

const BulkUploadAgendaModal = ({ onClose, onUpload }) => {
    const [agendas, setAgendas] = useState([]);
    const [errors, setErrors] = useState([]);
    const [submitting, setSubmitting] = useState(false);
    const [apiError, setApiError] = useState(null);
    const [preview, setPreview] = useState(false);
    const fileInputRef = useRef(null);

    const validateAgendaData = (item, index) => {
        const validationErrors = [];

        if (!item.volunteerEmail?.trim()) {
            validationErrors.push(`Entrée ${index + 1} : L'email du bénévole est requis`);
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(item.volunteerEmail)) {
            validationErrors.push(`Entrée ${index + 1} : L'email "${item.volunteerEmail}" n'est pas valide`);
        }

        if (!Array.isArray(item.tasks) || item.tasks.length === 0) {
            validationErrors.push(`Entrée ${index + 1} (${item.volunteerEmail || '?'}) : La liste des tâches ne peut pas être vide`);
        } else {
            item.tasks.forEach((task, ti) => {
                if (!task.name?.trim()) {
                    validationErrors.push(`Entrée ${index + 1}, tâche ${ti + 1} : Le nom de la tâche est requis`);
                }
                if (!task.competitionName?.trim()) {
                    validationErrors.push(`Entrée ${index + 1}, tâche ${ti + 1} : Le nom de la compétition est requis`);
                }
                if (!task.eventName?.trim()) {
                    validationErrors.push(`Entrée ${index + 1}, tâche ${ti + 1} : Le nom de l'événement est requis`);
                }
            });
        }

        return validationErrors;
    };

    const handleFileChange = async (e) => {
        const selectedFile = e.target.files[0];
        setErrors([]);
        setApiError(null);
        setAgendas([]);
        setPreview(false);

        if (!selectedFile) return;

        try {
            const text = await selectedFile.text();
            const jsonData = JSON.parse(text);

            if (!Array.isArray(jsonData)) {
                setErrors(["Le fichier JSON doit contenir un tableau d'agendas."]);
                return;
            }

            const validationErrors = [];
            jsonData.forEach((item, index) => {
                validationErrors.push(...validateAgendaData(item, index));
            });

            if (validationErrors.length > 0) {
                setErrors(validationErrors);
            } else {
                setAgendas(jsonData);
                setPreview(true);
            }
        } catch (error) {
            setErrors(["Erreur lors de la lecture du fichier JSON : " + error.message]);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (agendas.length === 0) {
            setApiError('Aucun agenda à téléverser');
            return;
        }

        setSubmitting(true);
        setApiError(null);

        try {
            await onUpload(agendas);
        } catch (err) {
            setApiError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const downloadTemplate = () => {
        const template = [
            {
                volunteerEmail: "volontaire@example.com",
                tasks: [
                    {
                        name: "Distribution de goodies",
                        description: "Distribuer les goodies aux athlètes",
                        competitionName: "Marathon",
                        eventName: "Waterpolo demi-finals"
                    },
                    {
                        name: "Accueil participants",
                        description: "Accueillir les participants à l'entrée",
                        competitionName: "Marathon",
                        eventName: "Waterpolo demi-finals"
                    }
                ]
            }
        ];

        const jsonString = JSON.stringify(template, null, 2);
        const blob = new Blob([jsonString], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'template_agendas.json';
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
    };

    const totalTasks = agendas.reduce((sum, a) => sum + (a.tasks?.length ?? 0), 0);

    return (
        <Modal show={true} onHide={onClose} size="lg" centered>
            <Modal.Header closeButton>
                <Modal.Title>📅 Téléversement des agendas bénévoles</Modal.Title>
            </Modal.Header>

            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {apiError && <Alert variant="danger">{apiError}</Alert>}

                    {errors.length > 0 && (
                        <Alert variant="danger">
                            <Alert.Heading>Erreurs de validation</Alert.Heading>
                            <ul className="mb-0">
                                {errors.map((err) => (
                                    <li key={err}>{err}</li>
                                ))}
                            </ul>
                        </Alert>
                    )}

                    <div className="mb-3">
                        <p className="text-muted">
                            Importez un fichier JSON contenant les agendas des bénévoles.
                            Chaque entrée doit avoir les champs suivants.
                            Les tâches ne peuvent être affectées qu'à des événements planifiés pour demain :
                        </p>
                        <ul className="text-muted">
                            <li><code>volunteerEmail</code> : Email du bénévole (obligatoire)</li>
                            <li><code>tasks</code> : Liste des tâches (obligatoire, non vide)
                                <ul>
                                    <li><code>name</code> : Nom de la tâche (obligatoire)</li>
                                    <li><code>description</code> : Description de la tâche (optionnel)</li>
                                    <li><code>competitionName</code> : Nom de la compétition (obligatoire)</li>
                                    <li><code>eventName</code> : Nom de l'événement dans la compétition (obligatoire)</li>
                                </ul>
                            </li>
                        </ul>
                        <p className="text-warning small">
                            ⚠️ Le téléversement remplacera l'agenda existant de chaque bénévole mentionné
                            et n'accepte que des événements du lendemain.
                        </p>
                        <Button variant="link" className="p-0 mb-3" onClick={downloadTemplate}>
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

                    {preview && agendas.length > 0 && (
                        <div>
                            <h6 className="mb-3">
                                Aperçu :{' '}
                                <Badge bg="primary">{agendas.length} bénévole(s)</Badge>{' '}
                                <Badge bg="secondary">{totalTasks} tâche(s)</Badge>
                            </h6>
                            <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                                <Table striped bordered hover size="sm">
                                    <thead>
                                        <tr>
                                            <th>Email bénévole</th>
                                            <th>Nombre de tâches</th>
                                            <th>Tâches</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {agendas.map((agenda) => (
                                            <tr key={agenda.volunteerEmail}>
                                                <td>{agenda.volunteerEmail}</td>
                                                <td className="text-center">{agenda.tasks.length}</td>
                                                <td>
                                                    {agenda.tasks.map((t) => (
                                                        <div key={`${t.competitionName}-${t.eventName}-${t.name}`} className="small">
                                                            <strong>{t.name}</strong>
                                                            {t.description && ` — ${t.description}`}
                                                            <span className="text-muted"> ({t.competitionName} / {t.eventName})</span>
                                                        </div>
                                                    ))}
                                                </td>
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
                        variant="primary"
                        type="submit"
                        disabled={agendas.length === 0 || submitting}
                    >
                        {submitting ? (
                            <>
                                <Spinner animation="border" size="sm" className="me-2" />
                                Téléversement...
                            </>
                        ) : (
                            `Téléverser (${agendas.length} bénévole(s))`
                        )}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
};

BulkUploadAgendaModal.propTypes = {
    onClose: PropTypes.func.isRequired,
    onUpload: PropTypes.func.isRequired,
};

export default BulkUploadAgendaModal;
