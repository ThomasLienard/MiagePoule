package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.Notification;

public interface NotificationService {
    void notifyEventStart(Event event);

    void notifyEventResults(Event event);

    Notification notifyIncident(Notification notification, Competition competition, String scope);
}
