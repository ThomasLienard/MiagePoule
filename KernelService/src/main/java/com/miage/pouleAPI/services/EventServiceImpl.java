package com.miage.pouleAPI.services;

import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.repositories.interfaces.EventRepository;
import com.miage.pouleAPI.services.interfaces.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
    
    @Override
    public Optional<Event> getEventById(Integer id) {
        return eventRepository.findById(id);
    }
}
