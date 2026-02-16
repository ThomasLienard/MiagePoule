package com.miage.pouleAPI.services.interfaces;


import com.miage.pouleAPI.dtos.event.CreateEventRequestDTO;

public interface AdminEventService {
    void createEvent(CreateEventRequestDTO request);
}