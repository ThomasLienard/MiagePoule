package com.miage.pouleAPI.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.miage.pouleAPI.dtos.event.EventDetailDTO;
import com.miage.pouleAPI.dtos.event.EventSummaryDTO;
import com.miage.pouleAPI.services.interfaces.EventService;

import java.util.List;

@RestController
@RequestMapping("/public")
public class EventController {
    
   
    private EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }
    
    @GetMapping("/events")
    public List<EventSummaryDTO> getAllEvents() {
        return eventService.getAllEvents();
    }
    
    @GetMapping("/events/{id}")
    public ResponseEntity<EventDetailDTO> getEventById(@PathVariable Integer id) {
        return eventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/championships/{championshipId}/comp/{competitionId}/events")
    public List<EventSummaryDTO> getEventsByChampionshipAndCompetition(
            @PathVariable Integer championshipId,
            @PathVariable Integer competitionId) {
        return eventService.getEventsByChampionshipAndCompetition(championshipId, competitionId);
    }
}
