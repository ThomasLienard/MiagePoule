package com.miage.pouleAPI.services.interfaces;


import java.util.List;
import java.util.Optional;

import com.miage.pouleAPI.dtos.event.EventDetailDTO;
import com.miage.pouleAPI.dtos.event.EventSummaryDTO;

public interface EventService {
    List<EventSummaryDTO> getAllEvents();
    Optional<EventDetailDTO> getEventById(Integer id);
    List<EventSummaryDTO> getEventsByChampionshipAndCompetition(Integer championshipId, Integer competitionId);
    List<EventSummaryDTO> getOtherEvents();
    List<EventSummaryDTO> getOtherEventsByCompetition(Integer competitionId);
}
