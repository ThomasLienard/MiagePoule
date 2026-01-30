package com.miage.pouleAPI.users;

import com.miage.pouleAPI.entity.Notification;

public interface Observer {
    void update(Notification notification);
}
