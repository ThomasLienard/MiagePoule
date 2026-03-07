package com.miage.pouleAPI.services.interfaces;


import com.miage.pouleAPI.dtos.event.CreateEventRequestDTO;
import com.miage.pouleAPI.dtos.event.UpdateEventRequestDTO;

public interface AdminEventService {
    void createEvent(CreateEventRequestDTO request);
    void updateEvent(UpdateEventRequestDTO request);
    void cancelEvent(Integer id, String reason);
}