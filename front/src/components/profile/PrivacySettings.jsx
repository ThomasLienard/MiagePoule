import React, { useEffect, useState } from "react";
import { Container, Table, Form, Badge, Spinner, Alert, Card } from "react-bootstrap";
import axios from "axios";

const PrivacySettings = () => {
    const [settings, setSettings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [message, setMessage] = useState({ type: "", text: "" });

    const API_URL = "http://localhost:8084/account/privacy";
    const token = localStorage.getItem("token");
    const config = { headers: { Authorization: `Bearer ${token}` } };

    useEffect(() => {
        fetchPrivacySettings();
    }, []);

    const fetchPrivacySettings = async () => {
        try {
            const response = await axios.get(API_URL, config);
            setSettings(response.data);
        } catch (error) {
            setMessage({ type: "danger", text: "Erreur lors du chargement des préférences." });
        } finally {
            setLoading(false);
        }
    };

    const handleToggle = async (categoryName, currentStatus, isMandatory) => {
        if (isMandatory) return;

        try {
            await axios.put(`${API_URL}/${categoryName}`, { enabled: !currentStatus }, config);

            setSettings(settings.map(s =>
                s.categoryName === categoryName ? { ...s, enabled: !currentStatus } : s
            ));

            setMessage({ type: "success", text: "Préférences mise à jour." });
            setTimeout(() => setMessage({}), 3000); // Cache le message après 3s
        } catch (error) {
            setMessage({ type: "danger", text: "Impossible de modifier ce réglage." });
        }
    };

    if (loading) return <Container className="text-center pt-5"><Spinner animation="border" variant="primary" /></Container>;

    return (
        <Container className="pt-4">
            <h2 className="mb-4">🛡️ Données et confidentialité</h2>
            <p className="text-muted">
                Gérez ici la façon dont vos données sont collectées et utilisées par l'application.
            </p>

            {message.text && <Alert variant={message.type} dismissible onClose={() => setMessage({})}>{message.text}</Alert>}

            <Card className="shadow-sm">
                <Table hover responsive className="mb-0">
                    <thead className="bg-light">
                    <tr>
                        <th>Catégorie et Finalité</th>
                        <th className="text-center">Partage</th>
                        <th className="text-center">Statut</th>
                    </tr>
                    </thead>
                    <tbody>
                    {settings.map((s) => (
                        <tr key={s.categoryName} className={s.mandatory ? "table-light" : ""}>
                            <td className="py-3">
                                <div className="fw-bold">
                                    {s.label}
                                    {s.mandatory && (
                                        <Badge bg="secondary" className="ms-2" style={{ fontSize: '0.7rem' }}>
                                            OBLIGATOIRE
                                        </Badge>
                                    )}
                                </div>
                                <div className="small text-muted">{s.purpose}</div>
                            </td>
                            <td className="text-center align-middle">
                                    {s.sharingLevel}
                            </td>
                            <td className="text-center align-middle">
                                <Form.Check
                                    type="switch"
                                    id={`switch-${s.categoryName}`}
                                    checked={s.enabled}
                                    disabled={s.mandatory}
                                    onChange={() => handleToggle(s.categoryName, s.enabled, s.mandatory)}
                                    style={{ cursor: s.mandatory ? 'not-allowed' : 'pointer' }}
                                />
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </Table>
            </Card>

            <div className="mt-4 p-3 bg-light rounded border">
                <small className="text-muted">
                    <strong>Note :</strong> Les traitements marqués comme "obligatoires" sont nécessaires au bon fonctionnement technique de votre compte (sécurité, accès aux services). Ils ne peuvent pas être désactivés.
                </small>
            </div>
        </Container>
    );
};

export default PrivacySettings;