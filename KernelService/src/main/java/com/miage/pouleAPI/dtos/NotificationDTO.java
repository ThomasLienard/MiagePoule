package com.miage.pouleAPI.dtos;

import com.miage.pouleAPI.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Integer id;
    private String title;
    private String description;
    private LocalDateTime emissionDate;
    private String type; // TypeNotification.name()
    private Integer eventId; // pour le lien
    private String severity; // Severity.name()

    public static NotificationDTO fromEntity(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getDescription(),
                notification.getEmissionDate(),
                notification.getType().name(),
                notification.getEvent() != null ? notification.getEvent().getId() : null,
                notification.getSeverity().getName()
        );
    }
}
