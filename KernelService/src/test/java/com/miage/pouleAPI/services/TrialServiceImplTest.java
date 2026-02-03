package com.miage.pouleAPI.services;

import com.miage.pouleAPI.adapters.TrialAdapter;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.dtos.trial.TrialDetailDTO;
import com.miage.pouleAPI.dtos.trial.TrialSummaryDTO;
import com.miage.pouleAPI.dtos.trial.AssignedTrialsResponseDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.Place;
import com.miage.pouleAPI.entity.TimeSlot;
import com.miage.pouleAPI.entity.Trial;
import com.miage.pouleAPI.repositories.TrialRepository;

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
@DisplayName("TrialServiceImpl Tests")
class TrialServiceImplTest {

    @Mock
    private TrialRepository trialRepository;

    @Mock
    private ApplicationUserRepository userRepository;

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
        trial1.setName(event1.getName());
        trial1.setDescription(event1.getDescription());
        trial1.setCompetition(event1.getCompetition());
        trial1.setTimeSlot(event1.getTimeSlot());
        trial1.setPlace(event1.getPlace());

        trial2 = new Trial();
        trial2.setId(2);
        trial2.setName(event2.getName());
        trial2.setDescription(event2.getDescription());
        trial2.setCompetition(event2.getCompetition());
        trial2.setTimeSlot(event2.getTimeSlot());
        trial2.setPlace(event2.getPlace());

        summary1 = new TrialSummaryDTO(1, 10, "Marathon de Paris", "42km course");
        summary2 = new TrialSummaryDTO(2, 20, "100m Sprint", "Sprint rapide");

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

    @Test
    @DisplayName("getTrialsByChampionshipAndCompetition() - Devrait retourner les épreuves d'une compétition")
    void testGetTrialsByChampionshipAndCompetition_Success() {
        // Given
        Integer championshipId = 1;
        Integer competitionId = 1;
        List<Trial> competitionTrials = Arrays.asList(trial1);
        List<TrialSummaryDTO> summaries = Arrays.asList(summary1);
        
        when(trialRepository.findByCompetitionId(competitionId)).thenReturn(competitionTrials);
        when(trialAdapter.entityListToSummaryDtoList(competitionTrials)).thenReturn(summaries);

        // When
        List<TrialSummaryDTO> result = trialService.getTrialsByChampionshipAndCompetition(championshipId, competitionId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Marathon de Paris", result.get(0).getName());
        verify(trialRepository, times(1)).findByCompetitionId(competitionId);
        verify(trialAdapter, times(1)).entityListToSummaryDtoList(competitionTrials);
    }

    @Test
    @DisplayName("getTrialsByChampionshipAndCompetition() - Devrait retourner une liste vide si aucune épreuve")
    void testGetTrialsByChampionshipAndCompetition_EmptyList() {
        // Given
        Integer championshipId = 1;
        Integer competitionId = 999;
        
        when(trialRepository.findByCompetitionId(competitionId)).thenReturn(Collections.emptyList());
        when(trialAdapter.entityListToSummaryDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        List<TrialSummaryDTO> result = trialService.getTrialsByChampionshipAndCompetition(championshipId, competitionId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(trialRepository, times(1)).findByCompetitionId(competitionId);
    }

    @Test
    @DisplayName("getTrialsByChampionshipAndCompetition() - Devrait propager les exceptions du repository")
    void testGetTrialsByChampionshipAndCompetition_ThrowsException() {
        // Given
        Integer championshipId = 1;
        Integer competitionId = 1;
        
        when(trialRepository.findByCompetitionId(competitionId))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> trialService.getTrialsByChampionshipAndCompetition(championshipId, competitionId));
        verify(trialRepository, times(1)).findByCompetitionId(competitionId);
    }

    @Test
    @DisplayName("getAssignedTrialsForUserEmail - Devrait retourner les épreuves solo et équipe")
    void testGetAssignedTrialsForUserEmail_Success() {
        // Given
        String email = "athlete@test.com";
        ApplicationUser user = new ApplicationUser();
        user.setId(1);
        user.setEmail(email);

        List<Trial> soloTrials = Arrays.asList(trial1);
        List<Trial> teamTrials = Arrays.asList(trial2);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(trialRepository.findSoloTrialsByUserId(1)).thenReturn(soloTrials);
        when(trialRepository.findTeamTrialsByUserId(1)).thenReturn(teamTrials);
        when(trialAdapter.entityListToSummaryDtoList(soloTrials))
            .thenReturn(Arrays.asList(summary1));
        when(trialAdapter.entityListToSummaryDtoList(teamTrials))
            .thenReturn(Arrays.asList(summary2));

        // When
        Optional<AssignedTrialsResponseDTO> result = trialService.getAssignedTrialsForUserEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getSoloTrials().size());
        assertEquals(1, result.get().getTeamTrials().size());

        verify(userRepository, times(1)).findByEmail(email);
        verify(trialRepository, times(1)).findSoloTrialsByUserId(1);
        verify(trialRepository, times(1)).findTeamTrialsByUserId(1);
    }

    @Test
    @DisplayName("getAssignedTrialsForUserEmail - Devrait retourner empty si utilisateur non trouvé")
    void testGetAssignedTrialsForUserEmail_UserNotFound() {
        // Given
        String email = "unknown@test.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<AssignedTrialsResponseDTO> result = trialService.getAssignedTrialsForUserEmail(email);

        // Then
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findByEmail(email);
        verify(trialRepository, never()).findSoloTrialsByUserId(any());
        verify(trialRepository, never()).findTeamTrialsByUserId(any());
    }

    @Test
    @DisplayName("getAssignedTrialsForUserEmail - Devrait retourner des listes vides si pas d'épreuves")
    void testGetAssignedTrialsForUserEmail_EmptyLists() {
        // Given
        String email = "athlete@test.com";
        ApplicationUser user = new ApplicationUser();
        user.setId(1);
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(trialRepository.findSoloTrialsByUserId(1)).thenReturn(Collections.emptyList());
        when(trialRepository.findTeamTrialsByUserId(1)).thenReturn(Collections.emptyList());
        when(trialAdapter.entityListToSummaryDtoList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());

        // When
        Optional<AssignedTrialsResponseDTO> result = trialService.getAssignedTrialsForUserEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertTrue(result.get().getSoloTrials().isEmpty());
        assertTrue(result.get().getTeamTrials().isEmpty());

        verify(userRepository, times(1)).findByEmail(email);
    }
}
