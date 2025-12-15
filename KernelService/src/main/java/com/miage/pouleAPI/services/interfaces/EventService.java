package com.miage.pouleAPI.services.interfaces;


import java.util.List;
import java.util.Optional;

import com.miage.pouleAPI.dto.event.EventDetailDTO;
import com.miage.pouleAPI.dto.event.EventSummaryDTO;

public interface EventService {
    List<EventSummaryDTO> getAllEvents();
    Optional<EventDetailDTO> getEventById(Integer id);
}
