package com.miage.pouleAPI.users;

import com.miage.pouleAPI.entity.Notification;
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

    public Notification addNotification(Notification notification) {
        this.getNotifications().add(notification);
        return notification;
    }

    public Notification removeNotification(Notification notification) {
        this.getNotifications().remove(notification);
        return notification;
    }

    @Override
    public void update(Notification notification) {
        this.addNotification(notification);
    }
}
