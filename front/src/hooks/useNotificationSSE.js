// src/hooks/useNotificationsSSE.js
import { useEffect, useState, useRef } from "react";

export const useNotificationsSSE = (userId) => {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [connected, setConnected] = useState(false);
    const eventSourceRef = useRef(null);

    useEffect(() => {
        // Si pas d'userId, on ne fait rien
        if (!userId) {
            return;
        }

        const connectSSE = () => {
            console.log(`Connecting SSE for user ${userId}`);

            // 1️⃣ Ouvre la connexion SSE vers le backend (via la gateway)
            const eventSource = new EventSource(
                `http://localhost:8084/api/notifications/stream/${userId}`
            );

            eventSourceRef.current = eventSource;

            // 2️⃣ Écoute les nouvelles notifications
            eventSource.addEventListener("message", (event) => {
                const notification = JSON.parse(event.data);
                console.log("New notification:", notification);

                // Affiche le contenu brut de l'événement dans une alert
                try {
                    alert(JSON.stringify(notification, null, 2));
                } catch (e) {
                    // Fallback si JSON.stringify pose problème
                    alert(event.data);
                }

                // Ajoute en début de liste (les plus récentes en haut)
                setNotifications((prevNotifications) => [notification, ...prevNotifications]);

                // Incrémente le badge
                setUnreadCount((prevCount) => prevCount + 1);
            });

            eventSource.onmessage = event => {
                console.log("==== onmessage ====");
                console.log(event.data);
            }

            // Handle open event
            eventSource.addEventListener("open", () => {
                console.log("SSE connection established for user", userId);
                setConnected(true);
            });

            // 3️⃣ Gère les erreurs de connexion
            eventSource.onerror = (err) => {
                console.error("SSE connection error:", err);
                console.error("EventSource readyState:", eventSource.readyState);
                
                // Log error details for debugging
                if (err && err.message) {
                    console.error("Error message:", err.message);
                }
                
                setConnected(false);
                eventSource.close();
                
                // Attempt to reconnect after 5 seconds
                console.log("Attempting to reconnect in 5 seconds...");
                const timeoutId = setTimeout(() => {
                    connectSSE();
                }, 5000);
                
                return () => clearTimeout(timeoutId);
            };
        };

        connectSSE();

        // 4️⃣ Nettoyage : ferme la connexion quand le composant se démonte
        return () => {
            console.log(`Closing SSE for user ${userId}`);
            if (eventSourceRef.current) {
                eventSourceRef.current.close();
            }
        };
    }, [userId]); // Relance si userId change (connexion/déconnexion)

    // Fonction pour marquer tout comme lu
    const markAllAsRead = () => {
        setUnreadCount(0);
        setNotifications([]); // vide la liste côté front
        // TODO : appel API pour marquer comme lues en base si besoin
    };

    return {
        notifications,
        unreadCount,
        markAllAsRead,
        connected
    };
};

