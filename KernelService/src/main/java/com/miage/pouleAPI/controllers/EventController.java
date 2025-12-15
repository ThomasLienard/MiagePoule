package com.miage.pouleAPI.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.miage.pouleAPI.dto.event.EventDetailDTO;
import com.miage.pouleAPI.dto.event.EventSummaryDTO;
import com.miage.pouleAPI.services.interfaces.EventService;

import java.util.List;

@RestController
@RequestMapping("/public")
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {
    
    @Autowired
    private EventService eventService;
    
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
}
