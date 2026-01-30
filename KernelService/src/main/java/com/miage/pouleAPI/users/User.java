package com.miage.pouleAPI.users;

import com.miage.pouleAPI.dtos.NotificationDTO;
import com.miage.pouleAPI.entity.Notification;
import com.miage.pouleAPI.services.SseNotificationService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class User implements Observer{
    @Setter(AccessLevel.NONE)
    private Integer id;

    private String name;

    private String lastname;

    private String password;

    private String email;

    private Set<Notification> notifications;
    private SseNotificationService sseNotificationService;

    public Notification addNotification(Notification notification) {
        this.getNotifications().add(notification);
        return notification;
    }

    public Notification removeNotification(Notification notification) {
        this.getNotifications().remove(notification);
        return notification;
    }

    // Dans ApplicationUser.update()
    @Override
    public void update(Notification notification) {
        this.notifications.add(notification);

        // Pousse au front si connecté
        sseNotificationService.sendNotification(this.getId(),
                NotificationDTO.fromEntity(notification));
    }
}
