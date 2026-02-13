import React from 'react';
import { Modal, Button } from 'react-bootstrap';

const DeleteConfirmModal = ({ show, onHide, onConfirm, itemName }) => {
    return (
        <Modal show={show} onHide={onHide} centered>
            <Modal.Header closeButton>
                <Modal.Title>Confirmer la suppression</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <p>Êtes-vous sûr de vouloir supprimer {itemName} ?</p>
                <p className="text-muted">Cette action est irréversible.</p>
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={onHide}>
                    Annuler
                </Button>
                <Button variant="danger" onClick={onConfirm}>
                    Supprimer
                </Button>
            </Modal.Footer>
        </Modal>
    );
};

export default DeleteConfirmModal;
