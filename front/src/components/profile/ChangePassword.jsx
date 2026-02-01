import React, { useState } from "react";
import { Modal, Button, Form, Alert } from "react-bootstrap";
import axios from "axios";

const ChangePassword = ({ show, handleClose }) => {
    const [passwords, setPasswords] = useState({ currentPassword: "", newPassword: "" });
    const [status, setStatus] = useState({ type: "", msg: "" });

    const API_URL = "http://localhost:8084/account/password";
    const token = localStorage.getItem("token");
    const config = { headers: { Authorization: `Bearer ${token}` } };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await axios.put(API_URL, passwords, config);

            setStatus({ type: "success", msg: "Mot de passe modifié avec succès !" });

            setTimeout(() => {
                setPasswords({ currentPassword: "", newPassword: "" });
                setStatus({ type: "", msg: "" });
                handleClose();
            }, 1500);
        } catch (err) {
            setStatus({ type: "danger", msg: "L'ancien mot de passe est incorrect." });
        }
    };

    return (
        <Modal show={show} onHide={handleClose} centered>
            <Modal.Header closeButton>
                <Modal.Title>🔒 Sécurité du compte</Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {status.msg && <Alert variant={status.type}>{status.msg}</Alert>}

                    <Form.Group className="mb-3">
                        <Form.Label>Mot de passe actuel</Form.Label>
                        <Form.Control
                            type="password"
                            required
                            value={passwords.currentPassword}
                            onChange={e => setPasswords({...passwords, currentPassword: e.target.value})}
                        />
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Nouveau mot de passe</Form.Label>
                        <Form.Control
                            type="password"
                            required
                            value={passwords.newPassword}
                            onChange={e => setPasswords({...passwords, newPassword: e.target.value})}
                        />
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleClose}>Annuler</Button>
                    <Button variant="danger" type="submit">Mettre à jour</Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
};

export default ChangePassword;