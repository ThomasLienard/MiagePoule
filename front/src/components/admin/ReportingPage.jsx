import { useEffect, useState } from 'react';
import { Alert, Badge, Button, Card, Col, Container, Form, Row, Spinner, Table } from 'react-bootstrap';
import reportingService from '../../services/reportingService';

const PERIODS = {
    DAILY: 'DAILY',
    WEEKLY: 'WEEKLY'
};

const toInputDate = (date) => date.toISOString().slice(0, 10);

const renderTypeRows = (data) => {
    const entries = Object.entries(data || {});

    if (entries.length === 0) {
        return [
            <tr key="empty-row">
                <td colSpan={2} className="text-center text-muted">Aucune donnée sur la période</td>
            </tr>
        ];
    }

    return entries.map(([type, count]) => (
        <tr key={type}>
            <td>{type}</td>
            <td className="text-end">{count}</td>
        </tr>
    ));
};

const removeRole = (roleMap, roleToRemove) => {
    return Object.fromEntries(
        Object.entries(roleMap || {}).filter(([role]) => role !== roleToRemove)
    );
};

const ReportingPage = () => {
    const [period, setPeriod] = useState(PERIODS.DAILY);
    const [selectedDate, setSelectedDate] = useState(toInputDate(new Date()));
    const [metrics, setMetrics] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const loadMetrics = async (nextPeriod = period, nextDate = selectedDate) => {
        setLoading(true);
        setError('');
        try {
            const response = await reportingService.getMetrics(nextPeriod, nextDate);
            setMetrics(response);
        } catch (err) {
            setError(err.response?.data?.message || 'Impossible de récupérer les métriques');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadMetrics();
    }, []);

    return (
        <Container className="py-4">
            <h1 className="mb-3">📊 Reporting applicatif</h1>
            <p className="text-muted mb-4">
                Consultez les métriques journalières ou hebdomadaires (7 derniers jours).
            </p>

            <Card className="mb-4">
                <Card.Body>
                    <Row className="g-3 align-items-end">
                        <Col md={4}>
                            <Form.Label>Période</Form.Label>
                            <Form.Select
                                value={period}
                                onChange={(e) => setPeriod(e.target.value)}
                            >
                                <option value={PERIODS.DAILY}>Journalière</option>
                                <option value={PERIODS.WEEKLY}>Hebdomadaire (7 derniers jours)</option>
                            </Form.Select>
                        </Col>
                        <Col md={4}>
                            <Form.Label>Date de référence</Form.Label>
                            <Form.Control
                                type="date"
                                value={selectedDate}
                                onChange={(e) => setSelectedDate(e.target.value)}
                            />
                        </Col>
                        <Col md={4}>
                            <Button
                                variant="secondary"
                                onClick={() => loadMetrics(period, selectedDate)}
                                disabled={loading}
                            >
                                {loading ? 'Chargement...' : 'Actualiser'}
                            </Button>
                        </Col>
                    </Row>
                </Card.Body>
            </Card>

            {error && <Alert variant="danger">{error}</Alert>}

            {loading && (
                <div className="d-flex justify-content-center py-5">
                    <Spinner animation="border" role="status" />
                </div>
            )}

            {!loading && metrics && (
                <>
                    <div className="mb-3">
                        <Badge bg="dark" className="me-2">{metrics.periodType}</Badge>
                        <span className="text-muted">
                            Période du {metrics.fromDate} au {metrics.toDate}
                        </span>
                    </div>

                    <Row className="g-3 mb-4">
                        <Col md={6} lg={3}>
                            <Card className="h-100">
                                <Card.Body>
                                    <Card.Subtitle className="mb-2 text-muted">Nouveaux comptes créés</Card.Subtitle>
                                    <Card.Title>{metrics.newAccounts}</Card.Title>
                                </Card.Body>
                            </Card>
                        </Col>
                        <Col md={6} lg={3}>
                            <Card className="h-100">
                                <Card.Body>
                                    <Card.Subtitle className="mb-2 text-muted">Nombre de connexions</Card.Subtitle>
                                    <Card.Title>{metrics.connections}</Card.Title>
                                </Card.Body>
                            </Card>
                        </Col>
                        <Col md={6} lg={3}>
                            <Card className="h-100">
                                <Card.Body>
                                    <Card.Subtitle className="mb-2 text-muted">Notifications envoyées</Card.Subtitle>
                                    <Card.Title>{metrics.totalSentNotifications}</Card.Title>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>

                    <Row className="g-3">
                        <Col lg={6}>
                            <Card className="h-100">
                                <Card.Header>Notifications envoyées par type</Card.Header>
                                <Card.Body className="p-0">
                                    <Table striped hover responsive className="mb-0">
                                        <thead>
                                            <tr>
                                                <th>Type</th>
                                                <th className="text-end">Nombre</th>
                                            </tr>
                                        </thead>
                                        <tbody>{renderTypeRows(metrics.sentNotificationsByType)}</tbody>
                                    </Table>
                                </Card.Body>
                            </Card>
                        </Col>

                        <Col lg={6}>
                            <Card className="h-100">
                                <Card.Header>Nombre de comptes par role</Card.Header>
                                <Card.Body className="p-0">
                                    <Table striped hover responsive className="mb-0">
                                        <thead>
                                            <tr>
                                                <th>Role</th>
                                                <th className="text-end">Nombre</th>
                                            </tr>
                                        </thead>
                                        <tbody>{renderTypeRows(removeRole(metrics.accountsByRole, 'ADMIN'))}</tbody>
                                    </Table>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>
                </>
            )}
        </Container>
    );
};

export default ReportingPage;
