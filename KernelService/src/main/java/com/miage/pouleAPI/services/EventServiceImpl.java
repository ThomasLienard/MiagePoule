package com.miage.pouleAPI.services;

import com.miage.pouleAPI.adapters.EventAdapter;
import com.miage.pouleAPI.dtos.event.EventDetailDTO;
import com.miage.pouleAPI.dtos.event.EventSummaryDTO;
import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.repositories.EventRepository;
import com.miage.pouleAPI.services.interfaces.EventService;

import com.miage.pouleAPI.services.interfaces.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {
    
    private EventRepository eventRepository;
     
    private EventAdapter eventAdapter;
    private NotificationService notificationService;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, EventAdapter eventAdapter, NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.eventAdapter = eventAdapter;
        this.notificationService = notificationService;
    }

    @Override
    public void startEvent(int eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        notificationService.notifyEventStart(event);
    }

    @Override
    public void publishResults(int eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        notificationService.notifyEventResults(event);
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
    
    @Override
    public List<EventSummaryDTO> getOtherEvents() {
        return eventAdapter.entityListToSummaryDtoList(
            eventRepository.findByTypeEventNameNotEqual()
        );
    }
    
    @Override
    public List<EventSummaryDTO> getOtherEventsByCompetition(Integer competitionId) {
        return eventAdapter.entityListToSummaryDtoList(
            eventRepository.findByCompetitionIdAndTypeEventNameNotEqual(competitionId)
        );
    }}