import React from 'react';
import PropTypes from 'prop-types';
import { Form, Button, Spinner } from 'react-bootstrap';

/**
 * Composant de formulaire de mot de passe réutilisable
 * Utilisé par ChangePasswordPage et ActivateAccountPage
 */
const PasswordForm = ({ 
    formData, 
    handleChange, 
    handleSubmit, 
    loading, 
    submitLabel = 'Valider',
    loadingLabel = 'En cours...'
}) => {
    return (
        <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
                <Form.Label>Nouveau mot de passe</Form.Label>
                <Form.Control
                    type="password"
                    name="newPassword"
                    value={formData.newPassword}
                    onChange={handleChange}
                    placeholder="Minimum 6 caractères"
                    autoComplete="new-password"
                />
            </Form.Group>

            <Form.Group className="mb-4">
                <Form.Label>Confirmer le mot de passe</Form.Label>
                <Form.Control
                    type="password"
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    placeholder="Retapez votre mot de passe"
                    autoComplete="new-password"
                />
            </Form.Group>

            <Button 
                type="submit" 
                variant="secondary"
                className="w-100"
                disabled={loading}
            >
                {loading ? (
                    <>
                        <Spinner animation="border" size="sm" className="me-2" />
                        {loadingLabel}
                    </>
                ) : submitLabel}
            </Button>
        </Form>
    );
};

PasswordForm.propTypes = {
    formData: PropTypes.shape({
        newPassword: PropTypes.string.isRequired,
        confirmPassword: PropTypes.string.isRequired
    }).isRequired,
    handleChange: PropTypes.func.isRequired,
    handleSubmit: PropTypes.func.isRequired,
    loading: PropTypes.bool.isRequired,
    submitLabel: PropTypes.string.isRequired,
    loadingLabel: PropTypes.string.isRequired
};

export default PasswordForm;
