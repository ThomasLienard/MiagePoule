package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.TypeEvent;
import com.miage.pouleAPI.services.interfaces.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventController Tests")
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private MockMvc mockMvc;
    private Event event1;
    private Event event2;
    private TypeEvent typeEvent;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
        
        typeEvent = new TypeEvent();
        typeEvent.setName("Conférence");

        event1 = new Event();
        event1.setId(1);
        event1.setName("Tech Conference 2025");
        event1.setDescription("Annual technology conference");
        event1.setTypeEvent(typeEvent);

        event2 = new Event();
        event2.setId(2);
        event2.setName("Music Festival");
        event2.setDescription("Summer music festival");
        event2.setTypeEvent(typeEvent);
    }

    @Test
    @DisplayName("GET /public/events - Devrait retourner tous les événements")
    void testGetAllEvents_Success() throws Exception {
        // Given
        List<Event> events = Arrays.asList(event1, event2);
        when(eventService.getAllEvents()).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/public/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Tech Conference 2025"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Music Festival"));

        verify(eventService, times(1)).getAllEvents();
    }

    @Test
    @DisplayName("GET /public/events - Devrait retourner une liste vide quand aucun événement")
    void testGetAllEvents_EmptyList() throws Exception {
        // Given
        when(eventService.getAllEvents()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/public/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(eventService, times(1)).getAllEvents();
    }

    @Test
    @DisplayName("GET /public/events - Test avec méthode directe du controller")
    void testGetAllEvents_DirectCall() {
        // Given
        List<Event> events = Arrays.asList(event1, event2);
        when(eventService.getAllEvents()).thenReturn(events);

        // When
        List<Event> result = eventController.getAllEvents();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Tech Conference 2025", result.get(0).getName());
        assertEquals("Music Festival", result.get(1).getName());
        verify(eventService, times(1)).getAllEvents();
    }

    @Test
    @DisplayName("GET /public/events/{id} - Devrait retourner un événement par ID")
    void testGetEventById_Success() throws Exception {
        // Given
        when(eventService.getEventById(1)).thenReturn(Optional.of(event1));

        // When & Then
        mockMvc.perform(get("/public/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Tech Conference 2025"))
                .andExpect(jsonPath("$.description").value("Annual technology conference"));

        verify(eventService, times(1)).getEventById(1);
    }

    @Test
    @DisplayName("GET /public/events/{id} - Devrait retourner 404 si l'événement n'existe pas")
    void testGetEventById_NotFound() throws Exception {
        // Given
        when(eventService.getEventById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/public/events/999"))
                .andExpect(status().isNotFound());

        verify(eventService, times(1)).getEventById(999);
    }

    @Test
    @DisplayName("GET /public/events/{id} - Test avec méthode directe du controller")
    void testGetEventById_DirectCall_Success() {
        // Given
        when(eventService.getEventById(1)).thenReturn(Optional.of(event1));

        // When
        ResponseEntity<Event> response = eventController.getEventById(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals("Tech Conference 2025", response.getBody().getName());
        verify(eventService, times(1)).getEventById(1);
    }

    @Test
    @DisplayName("GET /public/events/{id} - Test avec méthode directe retournant 404")
    void testGetEventById_DirectCall_NotFound() {
        // Given
        when(eventService.getEventById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Event> response = eventController.getEventById(999);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(eventService, times(1)).getEventById(999);
    }

    @Test
    @DisplayName("GET /public/events - Devrait gérer les erreurs du service")
    void testGetAllEvents_ServiceException() {
        // Given
        when(eventService.getAllEvents()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> eventController.getAllEvents());
        verify(eventService, times(1)).getAllEvents();
    }

    @Test
    @DisplayName("Vérifier que le CORS est configuré pour localhost:5173")
    void testCorsConfiguration() throws Exception {
        mockMvc.perform(get("/public/events")
                .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());
    }
}
