package com.miage.pouleAPI.services;

import com.miage.pouleAPI.adapter.EventAdapter;
import com.miage.pouleAPI.dto.event.EventDetailDTO;
import com.miage.pouleAPI.dto.event.EventSummaryDTO;
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
    
    @Autowired
    private EventAdapter eventAdapter;
    
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
}
