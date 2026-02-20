package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.dtos.NotificationDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseNotificationService {
    SseEmitter subscribe(Integer userId);
    void sendNotification(Integer userId, NotificationDTO notificationDto);
}
