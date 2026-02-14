import React, { useState } from 'react';
import { Modal, Button, Form, Alert } from 'react-bootstrap';
import PropTypes from 'prop-types';

const CreateTeamModal = ({ show, onHide, onSubmit, countries, athletes }) => {
    const [formData, setFormData] = useState({
        name: '',
        countryCode: '',
        memberIds: []
    });
    const [error, setError] = useState(null);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleMemberToggle = (athleteId) => {
        setFormData(prev => {
            const currentIds = prev.memberIds;
            const isSelected = currentIds.includes(athleteId);
            
            return {
                ...prev,
                memberIds: isSelected
                    ? currentIds.filter(id => id !== athleteId)
                    : [...currentIds, athleteId]
            };
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        
        if (!formData.name.trim()) {
            setError('Le nom de l\'équipe est obligatoire');
            return;
        }
        
        if (!formData.countryCode) {
            setError('Le pays est obligatoire');
            return;
        }

        setError(null);
        onSubmit(formData);
        setFormData({
            name: '',
            countryCode: '',
            memberIds: []
        });
    };

    const handleClose = () => {
        setFormData({
            name: '',
            countryCode: '',
            memberIds: []
        });
        setError(null);
        onHide();
    };

    return (
        <Modal show={show} onHide={handleClose} size="lg">
            <Modal.Header closeButton>
                <Modal.Title>Créer une nouvelle équipe</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {error && <Alert variant="danger">{error}</Alert>}
                <Form onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                        <Form.Label>Nom de l'équipe *</Form.Label>
                        <Form.Control
                            type="text"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            placeholder="Entrez le nom de l'équipe"
                            required
                        />
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Pays *</Form.Label>
                        <Form.Select
                            name="countryCode"
                            value={formData.countryCode}
                            onChange={handleChange}
                            required
                        >
                            <option value="">Sélectionnez un pays</option>
                            {countries.map(country => (
                                <option key={country.code} value={country.code}>
                                    {country.code}
                                </option>
                            ))}
                        </Form.Select>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Membres (Athlètes)</Form.Label>
                        <div style={{ maxHeight: '300px', overflowY: 'auto', border: '1px solid #dee2e6', borderRadius: '0.375rem', padding: '0.5rem' }}>
                            {athletes.length === 0 ? (
                                <p className="text-muted text-center">Aucun athlète disponible</p>
                            ) : (
                                athletes.map(athlete => (
                                    <Form.Check
                                        key={athlete.id}
                                        type="checkbox"
                                        id={`create-athlete-${athlete.id}`}
                                        label={`${athlete.lastname} ${athlete.name} (${athlete.countryCode})`}
                                        checked={formData.memberIds.includes(athlete.id)}
                                        onChange={() => handleMemberToggle(athlete.id)}
                                        className="mb-2"
                                    />
                                ))
                            )}
                        </div>
                        <Form.Text className="text-muted">
                            Cliquez sur un athlète pour l'ajouter ou le retirer de l'équipe
                        </Form.Text>
                    </Form.Group>
                </Form>
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={handleClose}>
                    Annuler
                </Button>
                <Button variant="secondary" onClick={handleSubmit}>
                    Créer l'équipe
                </Button>
            </Modal.Footer>
        </Modal>
    );
};

export default CreateTeamModal;
CreateTeamModal.propTypes = {
    show: PropTypes.bool.isRequired,
    onHide: PropTypes.func.isRequired,
    onSubmit: PropTypes.func.isRequired,
    countries: PropTypes.arrayOf(PropTypes.shape({
        code: PropTypes.string.isRequired
    })).isRequired,
    athletes: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.number.isRequired,
        name: PropTypes.string.isRequired,
        lastname: PropTypes.string.isRequired,
        countryCode: PropTypes.string.isRequired
    })).isRequired
};

