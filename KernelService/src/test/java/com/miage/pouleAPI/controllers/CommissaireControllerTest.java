package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.event.CancelEventRequestDTO;
import com.miage.pouleAPI.dtos.event.UpdateEventRequestDTO;
import com.miage.pouleAPI.dtos.timeslot.TimeSlotDTO;
import com.miage.pouleAPI.services.interfaces.AdminEventService;
import com.miage.pouleAPI.services.interfaces.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommissaireControllerTest {

    @Mock private AdminEventService adminEventService;
    @Mock private EventService eventService;

    @InjectMocks
    private CommissaireController commissaireController;

    @Test
    void update_shouldReturn202_AndCallServiceWithId() {
        Integer eventId = 5;
        LocalDateTime newStart = LocalDateTime.now().plusDays(5);
        LocalDateTime newEnd = LocalDateTime.now().plusDays(5).plusHours(2);

        UpdateEventRequestDTO request = new UpdateEventRequestDTO();
        request.setTimeSlot(new TimeSlotDTO(newStart, newEnd));

        ResponseEntity<Void> response = commissaireController.updateEventDates(eventId, request);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        assertEquals(eventId, request.getId());

        verify(adminEventService, times(1)).updateEvent(request);
    }

    @Test
    void cancelEvent_shouldReturn204_AndCallService() {
        Integer eventId = 10;
        String reason = "Terrain impraticable";
        CancelEventRequestDTO request = new CancelEventRequestDTO(reason);

        ResponseEntity<Void> response = commissaireController.cancelEvent(eventId, request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(adminEventService, times(1)).cancelEvent(eventId, reason);
    }
}