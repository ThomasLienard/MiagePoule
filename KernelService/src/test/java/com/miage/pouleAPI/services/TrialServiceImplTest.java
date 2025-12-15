package com.miage.pouleAPI.services;

import com.miage.pouleAPI.adapter.TrialAdapter;
import com.miage.pouleAPI.dto.trial.TrialDetailDTO;
import com.miage.pouleAPI.dto.trial.TrialSummaryDTO;
import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.Place;
import com.miage.pouleAPI.entity.TimeSlot;
import com.miage.pouleAPI.entity.Trial;
import com.miage.pouleAPI.repositories.interfaces.TrialRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrialServiceImpl Tests")
class TrialServiceImplTest {

    @Mock
    private TrialRepository trialRepository;

    @Mock
    private TrialAdapter trialAdapter;

    @InjectMocks
    private TrialServiceImpl trialService;

    private Trial trial1;
    private Trial trial2;
    private Event event1;
    private Event event2;
    private TrialSummaryDTO summary1;
    private TrialSummaryDTO summary2;
    private TrialDetailDTO detailDTO;

    @BeforeEach
    void setUp() {
        Competition competition = new Competition();
        competition.setId(1);
        competition.setName("Championnats de France");

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStart(LocalDateTime.of(2025, 6, 20, 8, 0));
        timeSlot.setEnd(LocalDateTime.of(2025, 6, 20, 14, 0));

        Place place = new Place();
        place.setId(2);
        place.setName("Stade Olympique");

        event1 = new Event();
        event1.setId(1);
        event1.setName("Marathon de Paris");
        event1.setDescription("42km course");
        event1.setCompetition(competition);
        event1.setTimeSlot(timeSlot);
        event1.setPlace(place);

        event2 = new Event();
        event2.setId(2);
        event2.setName("100m Sprint");
        event2.setDescription("Sprint rapide");

        trial1 = new Trial();
        trial1.setId(1);
        trial1.setEvent(event1);

        trial2 = new Trial();
        trial2.setId(2);
        trial2.setEvent(event2);

        summary1 = new TrialSummaryDTO(1, "Marathon de Paris", "42km course");
        summary2 = new TrialSummaryDTO(2, "100m Sprint", "Sprint rapide");

        detailDTO = new TrialDetailDTO();
        detailDTO.setId(1);
        detailDTO.setName("Marathon de Paris");
        detailDTO.setDescription("42km course");
        detailDTO.setCompetitionName("Championnats de France");
    }

    @Test
    @DisplayName("getAllTrials() - Devrait retourner toutes les épreuves en DTO")
    void testGetAllTrials_Success() {
        // Given
        List<Trial> trials = Arrays.asList(trial1, trial2);
        List<TrialSummaryDTO> summaries = Arrays.asList(summary1, summary2);
        
        when(trialRepository.findAll()).thenReturn(trials);
        when(trialAdapter.entityListToSummaryDtoList(trials)).thenReturn(summaries);

        // When
        List<TrialSummaryDTO> result = trialService.getAllTrials();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Marathon de Paris", result.get(0).getName());
        assertEquals(2, result.get(1).getId());
        assertEquals("100m Sprint", result.get(1).getName());
        
        verify(trialRepository, times(1)).findAll();
        verify(trialAdapter, times(1)).entityListToSummaryDtoList(trials);
    }

    @Test
    @DisplayName("getAllTrials() - Devrait retourner une liste vide quand aucune épreuve")
    void testGetAllTrials_EmptyList() {
        // Given
        when(trialRepository.findAll()).thenReturn(Collections.emptyList());
        when(trialAdapter.entityListToSummaryDtoList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());

        // When
        List<TrialSummaryDTO> result = trialService.getAllTrials();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(trialRepository, times(1)).findAll();
        verify(trialAdapter, times(1)).entityListToSummaryDtoList(Collections.emptyList());
    }

    @Test
    @DisplayName("getAllTrials() - Devrait propager l'exception du repository")
    void testGetAllTrials_ThrowsException() {
        // Given
        when(trialRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> trialService.getAllTrials());
        verify(trialRepository, times(1)).findAll();
        verify(trialAdapter, never()).entityListToSummaryDtoList(any());
    }

    @Test
    @DisplayName("getTrialById() - Devrait retourner un TrialDetailDTO existant")
    void testGetTrialById_Success() {
        // Given
        when(trialRepository.findById(1)).thenReturn(Optional.of(trial1));
        when(trialAdapter.entityToDetailDto(trial1)).thenReturn(detailDTO);

        // When
        Optional<TrialDetailDTO> result = trialService.getTrialById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("Marathon de Paris", result.get().getName());
        assertEquals("Championnats de France", result.get().getCompetitionName());
        
        verify(trialRepository, times(1)).findById(1);
        verify(trialAdapter, times(1)).entityToDetailDto(trial1);
    }

    @Test
    @DisplayName("getTrialById() - Devrait retourner Optional.empty() si l'épreuve n'existe pas")
    void testGetTrialById_NotFound() {
        // Given
        when(trialRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<TrialDetailDTO> result = trialService.getTrialById(999);

        // Then
        assertFalse(result.isPresent());
        
        verify(trialRepository, times(1)).findById(999);
        verify(trialAdapter, never()).entityToDetailDto(any());
    }

    @Test
    @DisplayName("getTrialById() - Devrait gérer les IDs négatifs")
    void testGetTrialById_NegativeId() {
        // Given
        when(trialRepository.findById(-1)).thenReturn(Optional.empty());

        // When
        Optional<TrialDetailDTO> result = trialService.getTrialById(-1);

        // Then
        assertFalse(result.isPresent());
        verify(trialRepository, times(1)).findById(-1);
    }

    @Test
    @DisplayName("getTrialById() - Devrait gérer les IDs null")
    void testGetTrialById_NullId() {
        // Given
        when(trialRepository.findById(null)).thenReturn(Optional.empty());

        // When
        Optional<TrialDetailDTO> result = trialService.getTrialById(null);

        // Then
        assertFalse(result.isPresent());
        verify(trialRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("getTrialById() - Devrait propager l'exception du repository")
    void testGetTrialById_ThrowsException() {
        // Given
        when(trialRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> trialService.getTrialById(1));
        verify(trialRepository, times(1)).findById(1);
        verify(trialAdapter, never()).entityToDetailDto(any());
    }

    @Test
    @DisplayName("getAllTrials() - Devrait gérer les trials sans event associé")
    void testGetAllTrials_WithNullEvent() {
        // Given
        Trial trialWithoutEvent = new Trial();
        trialWithoutEvent.setId(3);
        trialWithoutEvent.setEvent(null);
        
        List<Trial> trials = Arrays.asList(trial1, trialWithoutEvent);
        List<TrialSummaryDTO> summaries = Arrays.asList(summary1); // L'adapter filtre le null
        
        when(trialRepository.findAll()).thenReturn(trials);
        when(trialAdapter.entityListToSummaryDtoList(trials)).thenReturn(summaries);

        // When
        List<TrialSummaryDTO> result = trialService.getAllTrials();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Seulement le trial valide
        
        verify(trialRepository, times(1)).findAll();
        verify(trialAdapter, times(1)).entityListToSummaryDtoList(trials);
    }

    @Test
    @DisplayName("getAllTrials() - Devrait utiliser l'adapter pour la conversion")
    void testGetAllTrials_UsesAdapter() {
        // Given
        List<Trial> trials = Arrays.asList(trial1);
        List<TrialSummaryDTO> summaries = Arrays.asList(summary1);
        
        when(trialRepository.findAll()).thenReturn(trials);
        when(trialAdapter.entityListToSummaryDtoList(trials)).thenReturn(summaries);

        // When
        trialService.getAllTrials();

        // Then
        verify(trialAdapter, times(1)).entityListToSummaryDtoList(trials);
    }

    @Test
    @DisplayName("getTrialById() - Devrait utiliser l'adapter pour la conversion détaillée")
    void testGetTrialById_UsesAdapterForDetailConversion() {
        // Given
        when(trialRepository.findById(1)).thenReturn(Optional.of(trial1));
        when(trialAdapter.entityToDetailDto(trial1)).thenReturn(detailDTO);

        // When
        trialService.getTrialById(1);

        // Then
        verify(trialAdapter, times(1)).entityToDetailDto(trial1);
    }
}
