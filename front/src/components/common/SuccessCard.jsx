import React from 'react';
import PropTypes from 'prop-types';
import { Container, Card, Button } from 'react-bootstrap';

/**
 * Composant d'écran de succès réutilisable
 */
const SuccessCard = ({ 
    title = '✅ Succès !',
    message,
    redirectMessage,
    buttonLabel,
    onButtonClick
}) => {
    return (
        <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '80vh' }}>
            <Card className="text-center p-4" style={{ maxWidth: '500px' }}>
                <Card.Body>
                    <h1 className="text-success">{title}</h1>
                    <p>{message}</p>
                    {redirectMessage && (
                        <p className="text-muted">{redirectMessage}</p>
                    )}
                    <Button 
                        variant="secondary"
                        onClick={onButtonClick}
                    >
                        {buttonLabel}
                    </Button>
                </Card.Body>
            </Card>
        </Container>
    );
};

SuccessCard.propTypes = {
    title: PropTypes.string.isRequired,
    message: PropTypes.string.isRequired,
    redirectMessage: PropTypes.string.isRequired,
    buttonLabel: PropTypes.string.isRequired,
    onButtonClick: PropTypes.func.isRequired
};

export default SuccessCard;
