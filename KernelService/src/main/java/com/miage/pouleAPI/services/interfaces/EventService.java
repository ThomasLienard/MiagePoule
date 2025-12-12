package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.entity.Event;
import java.util.List;
import java.util.Optional;

public interface EventService {
    List<Event> getAllEvents();
    Optional<Event> getEventById(Integer id);
}
