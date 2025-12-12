package com.miage.pouleAPI.services;

import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.TypeEvent;
import com.miage.pouleAPI.repositories.interfaces.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventServiceImpl Tests")
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event1;
    private Event event2;
    private TypeEvent typeEvent;

    @BeforeEach
    void setUp() {
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
    @DisplayName("getAllEvents() - Devrait retourner tous les événements")
    void testGetAllEvents_Success() {
        // Given
        List<Event> events = Arrays.asList(event1, event2);
        when(eventRepository.findAll()).thenReturn(events);

        // When
        List<Event> result = eventService.getAllEvents();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Tech Conference 2025", result.get(0).getName());
        assertEquals("Music Festival", result.get(1).getName());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllEvents() - Devrait retourner une liste vide quand aucun événement")
    void testGetAllEvents_EmptyList() {
        // Given
        when(eventRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Event> result = eventService.getAllEvents();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllEvents() - Devrait propager l'exception du repository")
    void testGetAllEvents_ThrowsException() {
        // Given
        when(eventRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> eventService.getAllEvents());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getEventById() - Devrait retourner un événement existant")
    void testGetEventById_Success() {
        // Given
        when(eventRepository.findById(1)).thenReturn(Optional.of(event1));

        // When
        Optional<Event> result = eventService.getEventById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("Tech Conference 2025", result.get().getName());
        assertEquals("Annual technology conference", result.get().getDescription());
        verify(eventRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getEventById() - Devrait retourner Optional.empty() si l'événement n'existe pas")
    void testGetEventById_NotFound() {
        // Given
        when(eventRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<Event> result = eventService.getEventById(999);

        // Then
        assertFalse(result.isPresent());
        verify(eventRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("getEventById() - Devrait gérer les IDs négatifs")
    void testGetEventById_NegativeId() {
        // Given
        when(eventRepository.findById(-1)).thenReturn(Optional.empty());

        // When
        Optional<Event> result = eventService.getEventById(-1);

        // Then
        assertFalse(result.isPresent());
        verify(eventRepository, times(1)).findById(-1);
    }

    @Test
    @DisplayName("getEventById() - Devrait gérer les IDs null")
    void testGetEventById_NullId() {
        // Given
        when(eventRepository.findById(null)).thenReturn(Optional.empty());

        // When
        Optional<Event> result = eventService.getEventById(null);

        // Then
        assertFalse(result.isPresent());
        verify(eventRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("getEventById() - Devrait propager l'exception du repository")
    void testGetEventById_ThrowsException() {
        // Given
        when(eventRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> eventService.getEventById(1));
        verify(eventRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getAllEvents() - Devrait retourner les événements avec leurs relations")
    void testGetAllEvents_WithRelations() {
        // Given
        List<Event> events = Arrays.asList(event1, event2);
        when(eventRepository.findAll()).thenReturn(events);

        // When
        List<Event> result = eventService.getAllEvents();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNotNull(result.get(0).getTypeEvent());
        assertEquals("Conférence", result.get(0).getTypeEvent().getName());
        assertNotNull(result.get(1).getTypeEvent());
        assertEquals("Conférence", result.get(1).getTypeEvent().getName());
        verify(eventRepository, times(1)).findAll();
    }
}
