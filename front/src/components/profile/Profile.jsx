import React, { useEffect, useState } from "react";
import { Container, Card, Badge, Spinner } from "react-bootstrap";
import axios from "axios";

const Profile = () => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const token = localStorage.getItem("token");
                const response = await axios.get("http://localhost:8084/account", {
                    headers: { Authorization: `Bearer ${token}` },
                });

                let data = response.data;

                while (typeof data === "string") {
                    console.log("Parsing d'une string détectée...");
                    data = JSON.parse(data);
                }

                console.log("NOM APRÈS NETTOYAGE :", data.name);
                setUser(data);

                console.log("OBJET ENFIN PARSÉ :", data);
                console.log("NOM DÉTECTÉ :", data.name);
                console.log("TYPE DE DATA :", typeof data);
                console.log("EST-CE UN TABLEAU ? :", Array.isArray(data));
                setUser(data);
            } catch (error) {
                console.error("Erreur API :", error);
            } finally {
                setLoading(false);
            }
        };
        fetchProfile();
    }, []);

    if (loading) return <Container className="text-center pt-5"><Spinner animation="border" /></Container>;

    if (!user) return <div className="text-center pt-5">Impossible de charger les données.</div>;
    if (user) console.log(user.name);

    return (
        <Container className="pt-4">
            <h2 className="text-center mb-4">
                👤 Profil de {user?.name || "Inconnu"}
            </h2>

            <div className="d-flex justify-content-center">
                <Card className="p-4 shadow-sm" style={{ maxWidth: "500px", width: "100%" }}>
                    <Card.Body>
                        {/* On force l'affichage en vérifiant la présence de l'objet */}
                        {user && (
                            <>
                                <p><strong>Prénom :</strong> {user.name}</p>
                                <p><strong>Nom :</strong> {user.lastname}</p>
                                <p><strong>Email :</strong> {user.email}</p>
                                <p>
                                    <strong>Rôle :</strong>{" "}
                                    <Badge bg="danger">{user.role?.roleName || "Aucun"}</Badge>
                                </p>
                            </>
                        )}

                        {!user && <p className="text-muted">Données en cours de synchronisation...</p>}
                    </Card.Body>
                </Card>
            </div>
        </Container>
    );
};

export default Profile;