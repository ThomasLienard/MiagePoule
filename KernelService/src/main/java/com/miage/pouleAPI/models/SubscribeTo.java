package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Subscribe_to")
public class SubscribeTo {

    @EmbeddedId
    private SubscribeToId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "id")
    private ApplicationUser user;

    @ManyToOne
    @MapsId("notificationId")
    @JoinColumn(name = "id_notification")
    private Notification notification;

}

