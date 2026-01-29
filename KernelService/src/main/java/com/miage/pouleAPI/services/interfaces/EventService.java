package com.miage.pouleAPI.services.interfaces;


import java.util.List;
import java.util.Optional;

import com.miage.pouleAPI.dtos.event.EventDetailDTO;
import com.miage.pouleAPI.dtos.event.EventSummaryDTO;

public interface EventService {
    void startEvent(int eventId);

    void publishResults(int eventId);

    List<EventSummaryDTO> getAllEvents();
    Optional<EventDetailDTO> getEventById(Integer id);
    List<EventSummaryDTO> getEventsByChampionshipAndCompetition(Integer championshipId, Integer competitionId);
}
