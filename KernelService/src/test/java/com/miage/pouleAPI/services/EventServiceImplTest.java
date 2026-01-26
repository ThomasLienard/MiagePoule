package com.miage.pouleAPI.services;

import com.miage.pouleAPI.adapters.EventAdapter;
import com.miage.pouleAPI.dtos.event.EventDetailDTO;
import com.miage.pouleAPI.dtos.event.EventSummaryDTO;
import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.Place;
import com.miage.pouleAPI.entity.TimeSlot;
import com.miage.pouleAPI.repositories.EventRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventServiceImpl Tests")
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventAdapter eventAdapter;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event1;
    private Event event2;
    private EventSummaryDTO summary1;
    private EventSummaryDTO summary2;
    private EventDetailDTO detailDTO;

    @BeforeEach
    void setUp() {
        Competition competition = new Competition();
        competition.setId(1);
        competition.setName("TechWorld 2025");

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStart(LocalDateTime.of(2025, 6, 15, 9, 0));
        timeSlot.setEnd(LocalDateTime.of(2025, 6, 15, 18, 0));

        Place place = new Place();
        place.setId(1);
        place.setName("Convention Center");

        event1 = new Event();
        event1.setId(1);
        event1.setName("Tech Conference 2025");
        event1.setDescription("Annual technology conference");
        event1.setCompetition(competition);
        event1.setTimeSlot(timeSlot);
        event1.setPlace(place);

        event2 = new Event();
        event2.setId(2);
        event2.setName("Music Festival");
        event2.setDescription("Summer music festival");

        summary1 = new EventSummaryDTO(1, "Tech Conference 2025", "Annual technology conference", "TechWorld 2025");
        summary2 = new EventSummaryDTO(2, "Music Festival", "Summer music festival", "TechWorld 2025");

        detailDTO = new EventDetailDTO();
        detailDTO.setId(1);
        detailDTO.setName("Tech Conference 2025");
        detailDTO.setDescription("Annual technology conference");
        detailDTO.setCompetitionName("TechWorld 2025");
    }

    @Test
    @DisplayName("getAllEvents() - Devrait retourner tous les événements en DTO")
    void testGetAllEvents_Success() {
        // Given
        List<Event> events = Arrays.asList(event1, event2);
        List<EventSummaryDTO> summaries = Arrays.asList(summary1, summary2);
        
        when(eventRepository.findAll()).thenReturn(events);
        when(eventAdapter.entityListToSummaryDtoList(events)).thenReturn(summaries);

        // When
        List<EventSummaryDTO> result = eventService.getAllEvents();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Tech Conference 2025", result.get(0).getName());
        assertEquals("Music Festival", result.get(1).getName());
        
        verify(eventRepository, times(1)).findAll();
        verify(eventAdapter, times(1)).entityListToSummaryDtoList(events);
    }

    @Test
    @DisplayName("getAllEvents() - Devrait retourner une liste vide quand aucun événement")
    void testGetAllEvents_EmptyList() {
        // Given
        when(eventRepository.findAll()).thenReturn(Collections.emptyList());
        when(eventAdapter.entityListToSummaryDtoList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());

        // When
        List<EventSummaryDTO> result = eventService.getAllEvents();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(eventRepository, times(1)).findAll();
        verify(eventAdapter, times(1)).entityListToSummaryDtoList(Collections.emptyList());
    }

    @Test
    @DisplayName("getAllEvents() - Devrait propager l'exception du repository")
    void testGetAllEvents_ThrowsException() {
        // Given
        when(eventRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> eventService.getAllEvents());
        verify(eventRepository, times(1)).findAll();
        verify(eventAdapter, never()).entityListToSummaryDtoList(any());
    }

    @Test
    @DisplayName("getEventById() - Devrait retourner un EventDetailDTO existant")
    void testGetEventById_Success() {
        // Given
        when(eventRepository.findById(1)).thenReturn(Optional.of(event1));
        when(eventAdapter.entityToDetailDto(event1)).thenReturn(detailDTO);

        // When
        Optional<EventDetailDTO> result = eventService.getEventById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("Tech Conference 2025", result.get().getName());
        assertEquals("TechWorld 2025", result.get().getCompetitionName());
        
        verify(eventRepository, times(1)).findById(1);
        verify(eventAdapter, times(1)).entityToDetailDto(event1);
    }

    @Test
    @DisplayName("getEventById() - Devrait retourner Optional.empty() si l'événement n'existe pas")
    void testGetEventById_NotFound() {
        // Given
        when(eventRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<EventDetailDTO> result = eventService.getEventById(999);

        // Then
        assertFalse(result.isPresent());
        
        verify(eventRepository, times(1)).findById(999);
        verify(eventAdapter, never()).entityToDetailDto(any());
    }

    @Test
    @DisplayName("getEventById() - Devrait gérer les IDs négatifs")
    void testGetEventById_NegativeId() {
        // Given
        when(eventRepository.findById(-1)).thenReturn(Optional.empty());

        // When
        Optional<EventDetailDTO> result = eventService.getEventById(-1);

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
        Optional<EventDetailDTO> result = eventService.getEventById(null);

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
        verify(eventAdapter, never()).entityToDetailDto(any());
    }

    @Test
    @DisplayName("getAllEvents() - Devrait utiliser l'adapter pour la conversion")
    void testGetAllEvents_UsesAdapter() {
        // Given
        List<Event> events = Arrays.asList(event1);
        List<EventSummaryDTO> summaries = Arrays.asList(summary1);
        
        when(eventRepository.findAll()).thenReturn(events);
        when(eventAdapter.entityListToSummaryDtoList(events)).thenReturn(summaries);

        // When
        eventService.getAllEvents();

        // Then
        verify(eventAdapter, times(1)).entityListToSummaryDtoList(events);
    }

    @Test
    @DisplayName("getEventById() - Devrait utiliser l'adapter pour la conversion détaillée")
    void testGetEventById_UsesAdapterForDetailConversion() {
        // Given
        when(eventRepository.findById(1)).thenReturn(Optional.of(event1));
        when(eventAdapter.entityToDetailDto(event1)).thenReturn(detailDTO);

        // When
        eventService.getEventById(1);

        // Then
        verify(eventAdapter, times(1)).entityToDetailDto(event1);
    }

    @Test
    @DisplayName("getEventsByChampionshipAndCompetition() - Devrait retourner les événements d'une compétition")
    void testGetEventsByChampionshipAndCompetition_Success() {
        // Given
        Integer championshipId = 1;
        Integer competitionId = 1;
        List<Event> competitionEvents = Arrays.asList(event1);
        List<EventSummaryDTO> summaries = Arrays.asList(summary1);
        
        when(eventRepository.findByCompetitionId(competitionId)).thenReturn(competitionEvents);
        when(eventAdapter.entityListToSummaryDtoList(competitionEvents)).thenReturn(summaries);

        // When
        List<EventSummaryDTO> result = eventService.getEventsByChampionshipAndCompetition(championshipId, competitionId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tech Conference 2025", result.get(0).getName());
        verify(eventRepository, times(1)).findByCompetitionId(competitionId);
        verify(eventAdapter, times(1)).entityListToSummaryDtoList(competitionEvents);
    }

    @Test
    @DisplayName("getEventsByChampionshipAndCompetition() - Devrait retourner une liste vide si aucun événement")
    void testGetEventsByChampionshipAndCompetition_EmptyList() {
        // Given
        Integer championshipId = 1;
        Integer competitionId = 999;
        
        when(eventRepository.findByCompetitionId(competitionId)).thenReturn(Collections.emptyList());
        when(eventAdapter.entityListToSummaryDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        List<EventSummaryDTO> result = eventService.getEventsByChampionshipAndCompetition(championshipId, competitionId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(eventRepository, times(1)).findByCompetitionId(competitionId);
    }

    @Test
    @DisplayName("getEventsByChampionshipAndCompetition() - Devrait propager les exceptions du repository")
    void testGetEventsByChampionshipAndCompetition_ThrowsException() {
        // Given
        Integer championshipId = 1;
        Integer competitionId = 1;
        
        when(eventRepository.findByCompetitionId(competitionId))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> eventService.getEventsByChampionshipAndCompetition(championshipId, competitionId));
        verify(eventRepository, times(1)).findByCompetitionId(competitionId);
    }

    @Test
    @DisplayName("getOtherEvents() - Devrait retourner les événements qui ne sont pas de type Trial")
    void testGetOtherEvents_Success() {
        // Given
        List<Event> otherEvents = Arrays.asList(event1, event2);
        List<EventSummaryDTO> summaries = Arrays.asList(summary1, summary2);
        
        when(eventRepository.findByTypeEventNameNotEqual()).thenReturn(otherEvents);
        when(eventAdapter.entityListToSummaryDtoList(otherEvents)).thenReturn(summaries);

        // When
        List<EventSummaryDTO> result = eventService.getOtherEvents();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Tech Conference 2025", result.get(0).getName());
        assertEquals("Music Festival", result.get(1).getName());
        verify(eventRepository, times(1)).findByTypeEventNameNotEqual();
        verify(eventAdapter, times(1)).entityListToSummaryDtoList(otherEvents);
    }

    @Test
    @DisplayName("getOtherEvents() - Devrait retourner une liste vide s'il n'y a pas d'autres événements")
    void testGetOtherEvents_EmptyList() {
        // Given
        when(eventRepository.findByTypeEventNameNotEqual()).thenReturn(Collections.emptyList());
        when(eventAdapter.entityListToSummaryDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        List<EventSummaryDTO> result = eventService.getOtherEvents();

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(eventRepository, times(1)).findByTypeEventNameNotEqual();
        verify(eventAdapter, times(1)).entityListToSummaryDtoList(Collections.emptyList());
    }

    @Test
    @DisplayName("getOtherEvents() - Devrait propager les exceptions du repository")
    void testGetOtherEvents_ThrowsException() {
        // Given
        when(eventRepository.findByTypeEventNameNotEqual())
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> eventService.getOtherEvents());
        verify(eventRepository, times(1)).findByTypeEventNameNotEqual();
        verify(eventAdapter, never()).entityListToSummaryDtoList(any());
    }

    @Test
    @DisplayName("getOtherEvents() - Devrait utiliser l'adapter pour la conversion")
    void testGetOtherEvents_UsesAdapter() {
        // Given
        List<Event> otherEvents = Arrays.asList(event1);
        List<EventSummaryDTO> summaries = Arrays.asList(summary1);
        
        when(eventRepository.findByTypeEventNameNotEqual()).thenReturn(otherEvents);
        when(eventAdapter.entityListToSummaryDtoList(otherEvents)).thenReturn(summaries);

        // When
        eventService.getOtherEvents();

        // Then
        verify(eventAdapter, times(1)).entityListToSummaryDtoList(otherEvents);
    }
}
