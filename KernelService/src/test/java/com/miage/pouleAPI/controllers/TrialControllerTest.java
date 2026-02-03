package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.place.PlaceDTO;
import com.miage.pouleAPI.dtos.timeslot.TimeSlotDTO;
import com.miage.pouleAPI.dtos.trial.TrialDetailDTO;
import com.miage.pouleAPI.dtos.trial.TrialSummaryDTO;
import com.miage.pouleAPI.dtos.trial.AssignedTrialsResponseDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.services.interfaces.TrialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
@DisplayName("TrialController Tests")
class TrialControllerTest {

    @Mock
    private TrialService trialService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TrialController trialController;

    private MockMvc mockMvc;
    private TrialSummaryDTO trialSummary1;
    private TrialSummaryDTO trialSummary2;
    private TrialDetailDTO trialDetail;
    private ApplicationUser testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trialController).build();
        
        // Mise à jour avec idEvent
        trialSummary1 = new TrialSummaryDTO(1, 10, "Marathon de Paris", "42km course");
        trialSummary2 = new TrialSummaryDTO(2, 20, "100m Sprint", "Sprint rapide");
        
        TimeSlotDTO timeSlot = new TimeSlotDTO(
            LocalDateTime.of(2025, 6, 20, 8, 0),
            LocalDateTime.of(2025, 6, 20, 14, 0)
        );
        
        PlaceDTO place = new PlaceDTO();
        place.setId(2);
        place.setName("Stade Olympique");
        place.setCity("Paris");
        place.setStreet("Avenue Pierre de Coubertin");
        place.setNumber("1");
        place.setZip("75012");
        
        trialDetail = new TrialDetailDTO(
            1,
            "Marathon de Paris",
            "42km course",
            "Championnats de France",
            timeSlot,
            place,
            new ArrayList<>()
        );

        // Setup test user
        testUser = new ApplicationUser();
        testUser.setId(1);
        testUser.setEmail("athlete@test.com");
        testUser.setName("Test");
        testUser.setLastname("User");
    }

    @Test
    @DisplayName("GET /public/trials - Devrait retourner toutes les épreuves avec idEvent")
    void testGetAllTrials_Success() throws Exception {
        // Given
        List<TrialSummaryDTO> trials = Arrays.asList(trialSummary1, trialSummary2);
        when(trialService.getAllTrials()).thenReturn(trials);

        // When & Then
        mockMvc.perform(get("/public/trials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].idEvent").value(10))
                .andExpect(jsonPath("$[0].name").value("Marathon de Paris"))
                .andExpect(jsonPath("$[0].description").value("42km course"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].idEvent").value(20))
                .andExpect(jsonPath("$[1].name").value("100m Sprint"));

        verify(trialService, times(1)).getAllTrials();
    }

    @Test
    @DisplayName("GET /public/trials - Devrait retourner une liste vide quand aucune épreuve")
    void testGetAllTrials_EmptyList() throws Exception {
        // Given
        when(trialService.getAllTrials()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/public/trials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(trialService, times(1)).getAllTrials();
    }

    @Test
    @DisplayName("GET /public/trials - Test avec appel direct du controller")
    void testGetAllTrials_DirectCall() {
        // Given
        List<TrialSummaryDTO> trials = Arrays.asList(trialSummary1, trialSummary2);
        when(trialService.getAllTrials()).thenReturn(trials);

        // When
        List<TrialSummaryDTO> result = trialController.getAllTrials();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(10, result.get(0).getIdEvent());
        assertEquals("Marathon de Paris", result.get(0).getName());
        assertEquals(2, result.get(1).getId());
        assertEquals(20, result.get(1).getIdEvent());
        assertEquals("100m Sprint", result.get(1).getName());
        verify(trialService, times(1)).getAllTrials();
    }

    @Test
    @DisplayName("GET /public/trials/{id} - Devrait retourner une épreuve détaillée par ID")
    void testGetTrialById_Success() throws Exception {
        // Given
        when(trialService.getTrialById(1)).thenReturn(Optional.of(trialDetail));

        // When & Then
        mockMvc.perform(get("/public/trials/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Marathon de Paris"))
                .andExpect(jsonPath("$.description").value("42km course"))
                .andExpect(jsonPath("$.competitionName").value("Championnats de France"))
                .andExpect(jsonPath("$.timeSlot").exists())
                .andExpect(jsonPath("$.place").exists())
                .andExpect(jsonPath("$.place.name").value("Stade Olympique"))
                .andExpect(jsonPath("$.place.city").value("Paris"));

        verify(trialService, times(1)).getTrialById(1);
    }

    @Test
    @DisplayName("GET /public/trials/{id} - Devrait retourner 404 si l'épreuve n'existe pas")
    void testGetTrialById_NotFound() throws Exception {
        // Given
        when(trialService.getTrialById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/public/trials/999"))
                .andExpect(status().isNotFound());

        verify(trialService, times(1)).getTrialById(999);
    }

    @Test
    @DisplayName("GET /public/trials/{id} - Test avec appel direct retournant 200")
    void testGetTrialById_DirectCall_Success() {
        // Given
        when(trialService.getTrialById(1)).thenReturn(Optional.of(trialDetail));

        // When
        ResponseEntity<TrialDetailDTO> response = trialController.getTrialById(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals("Marathon de Paris", response.getBody().getName());
        assertEquals("Championnats de France", response.getBody().getCompetitionName());
        verify(trialService, times(1)).getTrialById(1);
    }

    @Test
    @DisplayName("GET /public/trials/{id} - Test avec appel direct retournant 404")
    void testGetTrialById_DirectCall_NotFound() {
        // Given
        when(trialService.getTrialById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<TrialDetailDTO> response = trialController.getTrialById(999);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(trialService, times(1)).getTrialById(999);
    }

    @Test
    @DisplayName("GET /public/trials - Devrait gérer les exceptions du service")
    void testGetAllTrials_ServiceException() {
        // Given
        when(trialService.getAllTrials()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> trialController.getAllTrials());
        verify(trialService, times(1)).getAllTrials();
    }

    @Test
    @DisplayName("GET /public/trials/{id} - Devrait propager les exceptions du service")
    void testGetTrialById_ServiceException() {
        // Given
        when(trialService.getTrialById(1)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> trialController.getTrialById(1));
        verify(trialService, times(1)).getTrialById(1);
    }

    @Test
    @DisplayName("Vérifier que le CORS est configuré pour localhost:3000")
    void testCorsConfiguration() throws Exception {
        when(trialService.getAllTrials()).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/public/trials")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /public/trials/{id} - Devrait retourner épreuve sans TimeSlot ni Place")
    void testGetTrialById_WithoutOptionalFields() {
        // Given
        TrialDetailDTO minimalTrial = new TrialDetailDTO(
            1, "Minimal Trial", "Description", null, null, null, new ArrayList<>()
        );
        when(trialService.getTrialById(1)).thenReturn(Optional.of(minimalTrial));

        // When
        ResponseEntity<TrialDetailDTO> response = trialController.getTrialById(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getCompetitionName());
        assertNull(response.getBody().getTimeSlot());
        assertNull(response.getBody().getPlace());
    }

    @Test
    @DisplayName("GET /public/trials/{id} - Devrait gérer les IDs négatifs")
    void testGetTrialById_NegativeId() throws Exception {
        // Given
        when(trialService.getTrialById(-1)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/public/trials/-1"))
                .andExpect(status().isNotFound());

        verify(trialService, times(1)).getTrialById(-1);
    }

    @Test
    @DisplayName("GET /public/championships/{championshipId}/comp/{competitionId}/trials - Devrait retourner les épreuves d'une compétition")
    void testGetTrialsByChampionshipAndCompetition_Success() throws Exception {
        // Given
        List<TrialSummaryDTO> competitionTrials = Arrays.asList(trialSummary1);
        when(trialService.getTrialsByChampionshipAndCompetition(1, 1)).thenReturn(competitionTrials);

        // When & Then
        mockMvc.perform(get("/public/championships/1/comp/1/trials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].idEvent").value(10))
                .andExpect(jsonPath("$[0].name").value("Marathon de Paris"));

        verify(trialService, times(1)).getTrialsByChampionshipAndCompetition(1, 1);
    }

    @Test
    @DisplayName("GET /public/championships/{championshipId}/comp/{competitionId}/trials - Devrait retourner une liste vide")
    void testGetTrialsByChampionshipAndCompetition_EmptyList() throws Exception {
        // Given
        when(trialService.getTrialsByChampionshipAndCompetition(1, 999)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/public/championships/1/comp/999/trials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(trialService, times(1)).getTrialsByChampionshipAndCompetition(1, 999);
    }

    @Test
    @DisplayName("GET /public/championships/{championshipId}/comp/{competitionId}/trials - Devrait propager les exceptions")
    void testGetTrialsByChampionshipAndCompetition_ServiceException() {
        // Given
        when(trialService.getTrialsByChampionshipAndCompetition(1, 1))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> trialController.getTrialsByChampionshipAndCompetition(1, 1));
        verify(trialService, times(1)).getTrialsByChampionshipAndCompetition(1, 1);
    }

    @Test
    @DisplayName("GET /public/trials/assigned - Devrait retourner les épreuves assignées (solo et équipe)")
    void testGetAssignedTrials_Success() {
        // Given
        List<TrialSummaryDTO> soloTrials = Arrays.asList(trialSummary1);
        List<TrialSummaryDTO> teamTrials = Arrays.asList(trialSummary2);
        AssignedTrialsResponseDTO response = new AssignedTrialsResponseDTO(soloTrials, teamTrials);

        when(authentication.getName()).thenReturn("athlete@test.com");
        when(trialService.getAssignedTrialsForUserEmail("athlete@test.com")).thenReturn(Optional.of(response));

        // When
        ResponseEntity<AssignedTrialsResponseDTO> result = trialController.getAssignedTrials(authentication);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getSoloTrials().size());
        assertEquals(1, result.getBody().getTeamTrials().size());
        assertEquals("Marathon de Paris", result.getBody().getSoloTrials().get(0).getName());
        assertEquals("100m Sprint", result.getBody().getTeamTrials().get(0).getName());

        verify(trialService, times(1)).getAssignedTrialsForUserEmail("athlete@test.com");
    }

    @Test
    @DisplayName("GET /public/trials/assigned - Devrait retourner des listes vides quand pas d'épreuves assignées")
    void testGetAssignedTrials_EmptyLists() {
        // Given
        AssignedTrialsResponseDTO response = new AssignedTrialsResponseDTO(
            Collections.emptyList(), 
            Collections.emptyList()
        );

        when(authentication.getName()).thenReturn("athlete@test.com");
        when(trialService.getAssignedTrialsForUserEmail("athlete@test.com")).thenReturn(Optional.of(response));

        // When
        ResponseEntity<AssignedTrialsResponseDTO> result = trialController.getAssignedTrials(authentication);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getSoloTrials().isEmpty());
        assertTrue(result.getBody().getTeamTrials().isEmpty());

        verify(trialService, times(1)).getAssignedTrialsForUserEmail("athlete@test.com");
    }

    @Test
    @DisplayName("GET /public/trials/assigned - Devrait retourner 404 si utilisateur non trouvé")
    void testGetAssignedTrials_UserNotFound() {
        // Given
        when(authentication.getName()).thenReturn("unknown@test.com");
        when(trialService.getAssignedTrialsForUserEmail("unknown@test.com")).thenReturn(Optional.empty());

        // When
        ResponseEntity<AssignedTrialsResponseDTO> result = trialController.getAssignedTrials(authentication);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());

        verify(trialService, times(1)).getAssignedTrialsForUserEmail("unknown@test.com");
    }

    @Test
    @DisplayName("GET /public/trials/assigned - Devrait retourner les deux types d'épreuves")
    void testGetAssignedTrials_BothTypes() {
        // Given
        TrialSummaryDTO soloTrial = new TrialSummaryDTO(1, 10, "Solo Trial", "Solo description");
        TrialSummaryDTO teamTrial1 = new TrialSummaryDTO(2, 20, "Team Trial 1", "Team description 1");
        TrialSummaryDTO teamTrial2 = new TrialSummaryDTO(3, 30, "Team Trial 2", "Team description 2");

        List<TrialSummaryDTO> soloTrials = Arrays.asList(soloTrial);
        List<TrialSummaryDTO> teamTrials = Arrays.asList(teamTrial1, teamTrial2);
        AssignedTrialsResponseDTO response = new AssignedTrialsResponseDTO(soloTrials, teamTrials);

        when(authentication.getName()).thenReturn("athlete@test.com");
        when(trialService.getAssignedTrialsForUserEmail("athlete@test.com")).thenReturn(Optional.of(response));

        // When
        ResponseEntity<AssignedTrialsResponseDTO> result = trialController.getAssignedTrials(authentication);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getSoloTrials().size());
        assertEquals(2, result.getBody().getTeamTrials().size());
        assertEquals("Solo Trial", result.getBody().getSoloTrials().get(0).getName());
        assertEquals("Team Trial 1", result.getBody().getTeamTrials().get(0).getName());
        assertEquals("Team Trial 2", result.getBody().getTeamTrials().get(1).getName());

        verify(trialService, times(1)).getAssignedTrialsForUserEmail("athlete@test.com");
    }
}
