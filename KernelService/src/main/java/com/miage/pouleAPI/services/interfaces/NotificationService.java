package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.entity.Event;

public interface NotificationService {
    void notifyEventStart(Event event);

    void notifyEventResults(Event event);
}
