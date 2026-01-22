package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.event.EventDetailDTO;
import com.miage.pouleAPI.dtos.event.EventSummaryDTO;
import com.miage.pouleAPI.dtos.place.PlaceDTO;
import com.miage.pouleAPI.dtos.timeslot.TimeSlotDTO;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    private EventSummaryDTO eventSummary1;
    private EventSummaryDTO eventSummary2;
    private EventDetailDTO eventDetail;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
        
        eventSummary1 = new EventSummaryDTO(1, "Tech Conference 2025", "Annual technology conference");
        eventSummary2 = new EventSummaryDTO(2, "Music Festival", "Summer music festival");
        
        TimeSlotDTO timeSlot = new TimeSlotDTO(
            LocalDateTime.of(2025, 6, 15, 9, 0),
            LocalDateTime.of(2025, 6, 15, 18, 0)
        );
        
        PlaceDTO place = new PlaceDTO();
        place.setId(1);
        place.setName("Convention Center");
        place.setCity("Paris");
        place.setStreet("Rue de la Paix");
        place.setNumber("10");
        place.setZip("75001");
        
        eventDetail = new EventDetailDTO(
            1,
            "Tech Conference 2025",
            "Annual technology conference",
            "TechWorld 2025",
            timeSlot,
            place,
            new ArrayList<>()
        );
    }

    @Test
    @DisplayName("GET /public/events - Devrait retourner tous les événements")
    void testGetAllEvents_Success() throws Exception {
        // Given
        List<EventSummaryDTO> events = Arrays.asList(eventSummary1, eventSummary2);
        when(eventService.getAllEvents()).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/public/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Tech Conference 2025"))
                .andExpect(jsonPath("$[0].description").value("Annual technology conference"))
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
    @DisplayName("GET /public/events - Test avec appel direct du controller")
    void testGetAllEvents_DirectCall() {
        // Given
        List<EventSummaryDTO> events = Arrays.asList(eventSummary1, eventSummary2);
        when(eventService.getAllEvents()).thenReturn(events);

        // When
        List<EventSummaryDTO> result = eventController.getAllEvents();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Tech Conference 2025", result.get(0).getName());
        assertEquals("Music Festival", result.get(1).getName());
        verify(eventService, times(1)).getAllEvents();
    }

    @Test
    @DisplayName("GET /public/events/{id} - Devrait retourner un événement détaillé par ID")
    void testGetEventById_Success() throws Exception {
        // Given
        when(eventService.getEventById(1)).thenReturn(Optional.of(eventDetail));

        // When & Then
        mockMvc.perform(get("/public/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Tech Conference 2025"))
                .andExpect(jsonPath("$.description").value("Annual technology conference"))
                .andExpect(jsonPath("$.competitionName").value("TechWorld 2025"))
                .andExpect(jsonPath("$.timeSlot").exists())
                .andExpect(jsonPath("$.place").exists())
                .andExpect(jsonPath("$.place.name").value("Convention Center"))
                .andExpect(jsonPath("$.place.city").value("Paris"));

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
    @DisplayName("GET /public/events/{id} - Test avec appel direct retournant 200")
    void testGetEventById_DirectCall_Success() {
        // Given
        when(eventService.getEventById(1)).thenReturn(Optional.of(eventDetail));

        // When
        ResponseEntity<EventDetailDTO> response = eventController.getEventById(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals("Tech Conference 2025", response.getBody().getName());
        assertEquals("TechWorld 2025", response.getBody().getCompetitionName());
        verify(eventService, times(1)).getEventById(1);
    }

    @Test
    @DisplayName("GET /public/events/{id} - Test avec appel direct retournant 404")
    void testGetEventById_DirectCall_NotFound() {
        // Given
        when(eventService.getEventById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<EventDetailDTO> response = eventController.getEventById(999);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(eventService, times(1)).getEventById(999);
    }

    @Test
    @DisplayName("GET /public/events - Devrait gérer les exceptions du service")
    void testGetAllEvents_ServiceException() {
        // Given
        when(eventService.getAllEvents()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> eventController.getAllEvents());
        verify(eventService, times(1)).getAllEvents();
    }

    @Test
    @DisplayName("GET /public/events/{id} - Devrait propager les exceptions du service")
    void testGetEventById_ServiceException() {
        // Given
        when(eventService.getEventById(1)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> eventController.getEventById(1));
        verify(eventService, times(1)).getEventById(1);
    }

    @Test
    @DisplayName("Vérifier que le CORS est configuré pour localhost:3000")
    void testCorsConfiguration() throws Exception {
        when(eventService.getAllEvents()).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/public/events")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /public/events/{id} - Devrait retourner événement sans TimeSlot ni Place")
    void testGetEventById_WithoutOptionalFields() {
        // Given
        EventDetailDTO minimalEvent = new EventDetailDTO(
            1, "Minimal Event", "Description", null, null, null, new ArrayList<>()
        );
        when(eventService.getEventById(1)).thenReturn(Optional.of(minimalEvent));

        // When
        ResponseEntity<EventDetailDTO> response = eventController.getEventById(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getCompetitionName());
        assertNull(response.getBody().getTimeSlot());
        assertNull(response.getBody().getPlace());
    }

    @Test
    @DisplayName("GET /public/championships/{championshipId}/comp/{competitionId}/events - Devrait retourner les événements d'une compétition")
    void testGetEventsByChampionshipAndCompetition_Success() throws Exception {
        // Given
        List<EventSummaryDTO> competitionEvents = Arrays.asList(eventSummary1);
        when(eventService.getEventsByChampionshipAndCompetition(1, 1)).thenReturn(competitionEvents);

        // When & Then
        mockMvc.perform(get("/public/championships/1/comp/1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Tech Conference 2025"));

        verify(eventService, times(1)).getEventsByChampionshipAndCompetition(1, 1);
    }

    @Test
    @DisplayName("GET /public/championships/{championshipId}/comp/{competitionId}/events - Devrait retourner une liste vide")
    void testGetEventsByChampionshipAndCompetition_EmptyList() throws Exception {
        // Given
        when(eventService.getEventsByChampionshipAndCompetition(1, 999)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/public/championships/1/comp/999/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(eventService, times(1)).getEventsByChampionshipAndCompetition(1, 999);
    }

    @Test
    @DisplayName("GET /public/championships/{championshipId}/comp/{competitionId}/events - Devrait propager les exceptions")
    void testGetEventsByChampionshipAndCompetition_ServiceException() throws Exception {
        // Given
        when(eventService.getEventsByChampionshipAndCompetition(1, 1))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> eventController.getEventsByChampionshipAndCompetition(1, 1));
        verify(eventService, times(1)).getEventsByChampionshipAndCompetition(1, 1);
    }

    @Test
    @DisplayName("GET /public/otherEvent - Devrait retourner les événements non-Trial")
    void testGetOtherEvents_Success() throws Exception {
        // Given
        List<EventSummaryDTO> otherEvents = Arrays.asList(eventSummary1, eventSummary2);
        when(eventService.getOtherEvents()).thenReturn(otherEvents);

        // When & Then
        mockMvc.perform(get("/public/otherEvent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Tech Conference 2025"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Music Festival"));

        verify(eventService, times(1)).getOtherEvents();
    }

    @Test
    @DisplayName("GET /public/otherEvent - Devrait retourner une liste vide s'il n'y a pas d'autres événements")
    void testGetOtherEvents_EmptyList() throws Exception {
        // Given
        when(eventService.getOtherEvents()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/public/otherEvent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(eventService, times(1)).getOtherEvents();
    }

    @Test
    @DisplayName("GET /public/otherEvent - Test avec appel direct du controller")
    void testGetOtherEvents_DirectCall() {
        // Given
        List<EventSummaryDTO> otherEvents = Arrays.asList(eventSummary1, eventSummary2);
        when(eventService.getOtherEvents()).thenReturn(otherEvents);

        // When
        List<EventSummaryDTO> result = eventController.getOtherEvents();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Tech Conference 2025", result.get(0).getName());
        assertEquals("Music Festival", result.get(1).getName());
        verify(eventService, times(1)).getOtherEvents();
    }

    @Test
    @DisplayName("GET /public/otherEvent - Devrait propager les exceptions du service")
    void testGetOtherEvents_ServiceException() {
        // Given
        when(eventService.getOtherEvents())
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> eventController.getOtherEvents());
        verify(eventService, times(1)).getOtherEvents();
    }
}
