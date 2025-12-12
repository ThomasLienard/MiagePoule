package com.miage.pouleAPI.services;

import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.Trial;
import com.miage.pouleAPI.entity.TypeEvent;
import com.miage.pouleAPI.repositories.interfaces.TrialRepository;
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
@DisplayName("TrialServiceImpl Tests")
class TrialServiceImplTest {

    @Mock
    private TrialRepository trialRepository;

    @InjectMocks
    private TrialServiceImpl trialService;

    private Trial trial1;
    private Trial trial2;
    private Event event1;
    private Event event2;
    private TypeEvent typeEvent;

    @BeforeEach
    void setUp() {
        typeEvent = new TypeEvent();
        typeEvent.setName("Épreuve Sportive");

        event1 = new Event();
        event1.setId(1);
        event1.setName("Marathon de Paris");
        event1.setDescription("42km course");
        event1.setTypeEvent(typeEvent);

        event2 = new Event();
        event2.setId(2);
        event2.setName("100m Sprint");
        event2.setDescription("Sprint rapide");
        event2.setTypeEvent(typeEvent);

        trial1 = new Trial();
        trial1.setId(1);
        trial1.setEvent(event1);

        trial2 = new Trial();
        trial2.setId(2);
        trial2.setEvent(event2);
    }

    @Test
    @DisplayName("getAllTrials() - Devrait retourner toutes les épreuves")
    void testGetAllTrials_Success() {
        // Given
        List<Trial> trials = Arrays.asList(trial1, trial2);
        when(trialRepository.findAll()).thenReturn(trials);

        // When
        List<Trial> result = trialService.getAllTrials();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Marathon de Paris", result.get(0).getEvent().getName());
        assertEquals(2, result.get(1).getId());
        assertEquals("100m Sprint", result.get(1).getEvent().getName());
        verify(trialRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllTrials() - Devrait retourner une liste vide quand aucune épreuve")
    void testGetAllTrials_EmptyList() {
        // Given
        when(trialRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Trial> result = trialService.getAllTrials();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(trialRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllTrials() - Devrait propager l'exception du repository")
    void testGetAllTrials_ThrowsException() {
        // Given
        when(trialRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> trialService.getAllTrials());
        verify(trialRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getTrialById() - Devrait retourner une épreuve existante")
    void testGetTrialById_Success() {
        // Given
        when(trialRepository.findById(1)).thenReturn(Optional.of(trial1));

        // When
        Optional<Trial> result = trialService.getTrialById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertNotNull(result.get().getEvent());
        assertEquals("Marathon de Paris", result.get().getEvent().getName());
        verify(trialRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getTrialById() - Devrait retourner Optional.empty() si l'épreuve n'existe pas")
    void testGetTrialById_NotFound() {
        // Given
        when(trialRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<Trial> result = trialService.getTrialById(999);

        // Then
        assertFalse(result.isPresent());
        verify(trialRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("getTrialById() - Devrait gérer les IDs négatifs")
    void testGetTrialById_NegativeId() {
        // Given
        when(trialRepository.findById(-1)).thenReturn(Optional.empty());

        // When
        Optional<Trial> result = trialService.getTrialById(-1);

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
        Optional<Trial> result = trialService.getTrialById(null);

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
    }

    @Test
    @DisplayName("getAllTrials() - Devrait gérer les trials sans event associé")
    void testGetAllTrials_WithNullEvent() {
        // Given
        Trial trialWithoutEvent = new Trial();
        trialWithoutEvent.setId(3);
        trialWithoutEvent.setEvent(null);
        
        List<Trial> trials = Arrays.asList(trial1, trialWithoutEvent);
        when(trialRepository.findAll()).thenReturn(trials);

        // When
        List<Trial> result = trialService.getAllTrials();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNotNull(result.get(0).getEvent());
        assertNull(result.get(1).getEvent());
        verify(trialRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllTrials() - Devrait retourner les épreuves avec leurs relations")
    void testGetAllTrials_WithRelations() {
        // Given
        List<Trial> trials = Arrays.asList(trial1, trial2);
        when(trialRepository.findAll()).thenReturn(trials);

        // When
        List<Trial> result = trialService.getAllTrials();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNotNull(result.get(0).getEvent());
        assertNotNull(result.get(0).getEvent().getTypeEvent());
        assertEquals("Épreuve Sportive", result.get(0).getEvent().getTypeEvent().getName());
        assertNotNull(result.get(1).getEvent());
        assertNotNull(result.get(1).getEvent().getTypeEvent());
        verify(trialRepository, times(1)).findAll();
    }
}
