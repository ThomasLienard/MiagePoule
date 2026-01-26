import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Button, Card } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Profile.css';

const Profile = () => {
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        axios.get('/account')
            .then(res => setUser(res.data))
            .catch(err => console.error("Erreur profil", err));
    }, []);

    if (!user) return <div className="text-center mt-5">Chargement...</div>;

    return (
        <Container className="mt-5" style={{ maxWidth: '850px' }}>
            <h2 className="text-center fw-light mb-4">Profil</h2>

            <Card className="p-4 shadow-sm border-dark" style={{ borderRadius: '15px' }}>
                <div className="border border-dark rounded-3 p-2 mb-4 d-flex align-items-center" style={{ height: '60px' }}>
                    <div className="border border-dark rounded-2" style={{ width: '40px', height: '40px' }}></div>
                </div>

                <div className="ms-md-5 mb-4">
                    <Row className="mb-2">
                        <Col xs={3} md={2}>nom</Col>
                        <Col>: {user.lastname}</Col>
                    </Row>
                    <Row className="mb-2">
                        <Col xs={3} md={2}>prénom</Col>
                        <Col>: {user.name}</Col>
                    </Row>
                    <Row className="mb-2">
                        <Col xs={3} md={2}>mail</Col>
                        <Col>: {user.email}</Col>
                    </Row>
                </div>

                <div className="mt-4 ms-md-4">
                    <Button
                        variant="outline-dark"
                        className="px-4 py-2"
                        style={{ borderRadius: '12px' }}
                        onClick={() => navigate('/settings')}
                    >
                        Params
                    </Button>
                </div>
            </Card>
        </Container>
    );
};

export default Profile;