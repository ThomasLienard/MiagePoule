package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.NotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(SseNotificationService.class);
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Integer userId) {
        // Set timeout to 24 hours (in milliseconds) to allow for long-lived connections
        // while preventing indefinite hangs
        SseEmitter emitter = new SseEmitter(24 * 60 * 60 * 1000L);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            logger.info("User {} SSE disconnected", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            logger.info("User {} SSE timeout", userId);
        });
        emitter.onError(e -> {
            emitters.remove(userId);
            logger.error("User {} SSE error: {}", userId, e.getMessage());
        });

        return emitter;
    }

    public void sendNotification(Integer userId, NotificationDTO notificationDto) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("newNotification")
                        .data(notificationDto));
                logger.info("SSE sent to user {}: {}", userId, notificationDto);
            } catch (IOException e) {
                logger.error("Failed to send SSE to user {}", userId, e);
                emitters.remove(userId);
            }
        }
    }
}
