package com.miage.pouleAPI.users;

import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Notification;

public interface Subject {
    void attach(ApplicationUser o);
    void detach(ApplicationUser o);
    void notifyObservers(Notification n);
}
