package com.miage.pouleAPI.users;

import com.miage.pouleAPI.entity.Notification;

public interface Subject {
    void attach(Observer o);
    void detach(Observer o);
    void notifyObservers(Notification n);
}
