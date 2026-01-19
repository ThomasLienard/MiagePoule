package com.miage.pouleAPI.services;

import com.miage.pouleAPI.adapters.EventAdapter;
import com.miage.pouleAPI.dtos.event.EventDetailDTO;
import com.miage.pouleAPI.dtos.event.EventSummaryDTO;
import com.miage.pouleAPI.repositories.EventRepository;
import com.miage.pouleAPI.services.interfaces.EventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {
    
    private EventRepository eventRepository;
     
    private EventAdapter eventAdapter;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, EventAdapter eventAdapter) {
        this.eventRepository = eventRepository;
        this.eventAdapter = eventAdapter;
    }
    
    @Override
    public List<EventSummaryDTO> getAllEvents() {
        return eventAdapter.entityListToSummaryDtoList(
            eventRepository.findAll()
        );
    }
    
    @Override
    public Optional<EventDetailDTO> getEventById(Integer id) {
        return eventRepository.findById(id)
            .map(eventAdapter::entityToDetailDto);
    }
    
    @Override
    public List<EventSummaryDTO> getEventsByChampionshipAndCompetition(Integer championshipId, Integer competitionId) {
        return eventAdapter.entityListToSummaryDtoList(
            eventRepository.findByCompetitionId(competitionId)
        );
    }
}
