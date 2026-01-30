// src/hooks/useNotificationsSSE.js
import { useEffect, useState } from "react";

export const useNotificationsSSE = (userId) => {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);

    useEffect(() => {
        // Si pas d'userId, on ne fait rien
        if (!userId) {
            return;
        }

        console.log(`Connecting SSE for user ${userId}`);

        // 1️⃣ Ouvre la connexion SSE vers le backend
        const eventSource = new EventSource(
            `http://localhost:8080/api/notifications/stream/${userId}`
        );

        // 2️⃣ Écoute les nouvelles notifications
        eventSource.addEventListener("newNotification", (event) => {
            const notification = JSON.parse(event.data);
            console.log("New notification:", notification);

            // Ajoute en début de liste (les plus récentes en haut)
            setNotifications((prevNotifications) => [notification, ...prevNotifications]);

            // Incrémente le badge
            setUnreadCount((prevCount) => prevCount + 1);
        });

        // 3️⃣ Gère les erreurs de connexion
        eventSource.onerror = (err) => {
            console.error("SSE connection error:", err);
            eventSource.close();
        };

        // 4️⃣ Nettoyage : ferme la connexion quand le composant se démonte
        return () => {
            console.log(`Closing SSE for user ${userId}`);
            eventSource.close();
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
        markAllAsRead
    };
};
