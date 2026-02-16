package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.services.interfaces.SseNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
public class SseNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(SseNotificationController.class);
    private final SseNotificationService sseNotificationService;

    public SseNotificationController(SseNotificationService sseNotificationService) {
        this.sseNotificationService = sseNotificationService;
    }

    @GetMapping(value = "/stream/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(@PathVariable Integer userId) {
        logger.info("SSE stream requested for user: {}", userId);
        return sseNotificationService.subscribe(userId);
    }
}
