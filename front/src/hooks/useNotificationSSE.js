// src/hooks/useNotificationsSSE.js
import { useEffect, useState, useRef, useCallback } from "react";

const RECONNECT_DELAY = 5000;

export const useNotificationsSSE = (userId) => {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [connected, setConnected] = useState(false);
    const eventSourceRef = useRef(null);
    const reconnectTimeoutRef = useRef(null);
    const isOpenRef = useRef(true);

    const handleMessage = useCallback((event) => {
        const notification = JSON.parse(event.data);
        setNotifications((prev) => [notification, ...prev]);
        setUnreadCount((prev) => prev + 1);
    }, []);

    const handleOpen = useCallback(() => {
        setConnected(true);
    }, []);

    useEffect(() => {
        if (!userId) return;

        const handleError = () => {
            setConnected(false);
            eventSourceRef.current?.close();
            reconnectTimeoutRef.current = setTimeout(connectSSE, RECONNECT_DELAY);
        };

        const connectSSE = () => {
            const eventSource = new EventSource(
                `${import.meta.env.VITE_API_URL}/api/notifications/stream/${userId}`
            );
            eventSourceRef.current = eventSource;
            eventSource.addEventListener("message", handleMessage);
            eventSource.addEventListener("open", handleOpen);
            eventSource.onerror = handleError;
        };

        connectSSE();

        return () => {
            clearTimeout(reconnectTimeoutRef.current);
            eventSourceRef.current?.close();
        };
    }, [userId, handleMessage, handleOpen]);

    const markAllAsRead = () => {
        if (isOpenRef.current) {
            isOpenRef.current = false;
        } else {
            setNotifications([]);
            isOpenRef.current = true;
        }
        setUnreadCount(0);
    };

    return { notifications, unreadCount, markAllAsRead, connected };
};

