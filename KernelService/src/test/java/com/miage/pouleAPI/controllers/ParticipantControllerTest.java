package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.dtos.participant.*;
import com.miage.pouleAPI.services.interfaces.ParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests ParticipantController - Gestion des participants aux épreuves")
class ParticipantControllerTest {

    @Mock
    private ParticipantService participantService;

    @InjectMocks
    private ParticipantController participantController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TrialParticipantsDTO trialParticipantsDTO;
    private TrialParticipantsFullDTO trialParticipantsFullDTO;
    private ParticipantDTO athleteParticipant;
    private ParticipantDTO teamParticipant;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(participantController).build();
        objectMapper = new ObjectMapper();

        // Setup athlete participant
        athleteParticipant = new ParticipantDTO();
        athleteParticipant.setId(10);
        athleteParticipant.setName("John Doe");
        athleteParticipant.setType("ATHLETE");
        athleteParticipant.setCountry("FR");
        athleteParticipant.setForfeit(false);

        // Setup team participant
        teamParticipant = new ParticipantDTO();
        teamParticipant.setId(1);
        teamParticipant.setName("Team France");
        teamParticipant.setType("TEAM");
        teamParticipant.setCountry("FR");
        teamParticipant.setForfeit(false);

        // Setup trial participants DTO
        trialParticipantsDTO = new TrialParticipantsDTO();
        trialParticipantsDTO.setTrialId(1);
        trialParticipantsDTO.setTrialName("100m Sprint");
        trialParticipantsDTO.setTeamTrial(false);
        trialParticipantsDTO.setParticipants(List.of(athleteParticipant));
        trialParticipantsDTO.setPotentialParticipants(List.of());

        // Setup trial participants full DTO
        trialParticipantsFullDTO = new TrialParticipantsFullDTO();
        trialParticipantsFullDTO.setTrialId(1);
        trialParticipantsFullDTO.setTrialName("100m Sprint");
        trialParticipantsFullDTO.setTeamTrial(false);
        trialParticipantsFullDTO.setCanChangeType(false);
        trialParticipantsFullDTO.setParticipants(List.of(athleteParticipant));
        trialParticipantsFullDTO.setPotentialAthletes(List.of());
        trialParticipantsFullDTO.setPotentialTeams(List.of());
    }

    // ===== TESTS GET TRIALS FOR COMMISSAIRE =====

    @Nested
    @DisplayName("GET /commissaire/trials")
    class GetTrialsForCommissaireTests {

        @Test
        @DisplayName("Devrait retourner la liste des épreuves du commissaire")
        void getTrials_shouldReturnTrialsList() throws Exception {
            when(participantService.getTrialsForCommissaire()).thenReturn(List.of(trialParticipantsDTO));

            mockMvc.perform(get("/commissaire/trials"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].trialId").value(1))
                    .andExpect(jsonPath("$[0].trialName").value("100m Sprint"));

            verify(participantService).getTrialsForCommissaire();
        }

        @Test
        @DisplayName("Devrait retourner une liste vide si aucune épreuve assignée")
        void getTrials_shouldReturnEmptyList() throws Exception {
            when(participantService.getTrialsForCommissaire()).thenReturn(List.of());

            mockMvc.perform(get("/commissaire/trials"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ===== TESTS GET TRIAL PARTICIPANTS =====

    @Nested
    @DisplayName("GET /commissaire/trials/{trialId}/participants")
    class GetTrialParticipantsTests {

        @Test
        @DisplayName("Devrait retourner les participants d'une épreuve")
        void getParticipants_shouldReturnParticipants() throws Exception {
            when(participantService.getTrialParticipants(1)).thenReturn(Optional.of(trialParticipantsDTO));

            mockMvc.perform(get("/commissaire/trials/1/participants"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trialId").value(1))
                    .andExpect(jsonPath("$.participants[0].name").value("John Doe"));

            verify(participantService).getTrialParticipants(1);
        }

        @Test
        @DisplayName("Devrait retourner 404 si l'épreuve n'existe pas")
        void getParticipants_shouldReturn404WhenTrialNotFound() throws Exception {
            when(participantService.getTrialParticipants(999)).thenReturn(Optional.empty());

            mockMvc.perform(get("/commissaire/trials/999/participants"))
                    .andExpect(status().isNotFound());
        }
    }

    // ===== TESTS GET TRIAL PARTICIPANTS FULL =====

    @Nested
    @DisplayName("GET /commissaire/trials/{trialId}/participants/full")
    class GetTrialParticipantsFullTests {

        @Test
        @DisplayName("Devrait retourner tous les détails des participants")
        void getParticipantsFull_shouldReturnFullDetails() throws Exception {
            when(participantService.getTrialParticipantsFull(1)).thenReturn(Optional.of(trialParticipantsFullDTO));

            mockMvc.perform(get("/commissaire/trials/1/participants/full"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trialId").value(1))
                    .andExpect(jsonPath("$.canChangeType").value(false))
                    .andExpect(jsonPath("$.potentialAthletes").isArray())
                    .andExpect(jsonPath("$.potentialTeams").isArray());

            verify(participantService).getTrialParticipantsFull(1);
        }

        @Test
        @DisplayName("Devrait retourner 404 si l'épreuve n'existe pas")
        void getParticipantsFull_shouldReturn404WhenTrialNotFound() throws Exception {
            when(participantService.getTrialParticipantsFull(999)).thenReturn(Optional.empty());

            mockMvc.perform(get("/commissaire/trials/999/participants/full"))
                    .andExpect(status().isNotFound());
        }
    }

    // ===== TESTS ADD PARTICIPANT =====

    @Nested
    @DisplayName("POST /commissaire/trials/{trialId}/participants")
    class AddParticipantTests {

        @Test
        @DisplayName("Devrait ajouter un athlète avec succès")
        void addParticipant_shouldAddAthlete() throws Exception {
            AddParticipantRequest request = new AddParticipantRequest();
            request.setParticipantId(10);
            request.setParticipantType("ATHLETE");

            when(participantService.addAthleteToTrial(1, 10)).thenReturn(athleteParticipant);

            mockMvc.perform(post("/commissaire/trials/1/participants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.name").value("John Doe"))
                    .andExpect(jsonPath("$.type").value("ATHLETE"));

            verify(participantService).addAthleteToTrial(1, 10);
        }

        @Test
        @DisplayName("Devrait ajouter une équipe avec succès")
        void addParticipant_shouldAddTeam() throws Exception {
            AddParticipantRequest request = new AddParticipantRequest();
            request.setParticipantId(1);
            request.setParticipantType("TEAM");

            when(participantService.addTeamToTrial(1, 1)).thenReturn(teamParticipant);

            mockMvc.perform(post("/commissaire/trials/1/participants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Team France"))
                    .andExpect(jsonPath("$.type").value("TEAM"));

            verify(participantService).addTeamToTrial(1, 1);
        }

        @Test
        @DisplayName("Devrait retourner 400 pour type invalide")
        void addParticipant_shouldReturn400ForInvalidType() throws Exception {
            AddParticipantRequest request = new AddParticipantRequest();
            request.setParticipantId(10);
            request.setParticipantType("INVALID");

            mockMvc.perform(post("/commissaire/trials/1/participants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Devrait retourner 400 si l'athlète est déjà inscrit")
        void addParticipant_shouldReturn400WhenAlreadyRegistered() throws Exception {
            AddParticipantRequest request = new AddParticipantRequest();
            request.setParticipantId(10);
            request.setParticipantType("ATHLETE");

            when(participantService.addAthleteToTrial(1, 10))
                    .thenThrow(new IllegalArgumentException("L'athlète est déjà inscrit à cette épreuve"));

            mockMvc.perform(post("/commissaire/trials/1/participants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("L'athlète est déjà inscrit à cette épreuve"));
        }
    }

    // ===== TESTS FORFEIT =====

    @Nested
    @DisplayName("POST /commissaire/trials/{trialId}/forfeit")
    class ForfeitTests {

        @Test
        @DisplayName("Devrait déclarer un athlète forfait")
        void forfeit_shouldForfeitAthlete() throws Exception {
            ForfeitRequest request = new ForfeitRequest();
            request.setParticipantId(10);
            request.setParticipantType("ATHLETE");

            ParticipantDTO forfeitedAthlete = new ParticipantDTO();
            forfeitedAthlete.setId(10);
            forfeitedAthlete.setName("John Doe");
            forfeitedAthlete.setType("ATHLETE");
            forfeitedAthlete.setForfeit(true);

            when(participantService.forfeitAthlete(1, 10)).thenReturn(forfeitedAthlete);

            mockMvc.perform(post("/commissaire/trials/1/forfeit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.forfeit").value(true));

            verify(participantService).forfeitAthlete(1, 10);
        }

        @Test
        @DisplayName("Devrait déclarer une équipe forfait")
        void forfeit_shouldForfeitTeam() throws Exception {
            ForfeitRequest request = new ForfeitRequest();
            request.setParticipantId(1);
            request.setParticipantType("TEAM");

            ParticipantDTO forfeitedTeam = new ParticipantDTO();
            forfeitedTeam.setId(1);
            forfeitedTeam.setName("Team France");
            forfeitedTeam.setType("TEAM");
            forfeitedTeam.setForfeit(true);

            when(participantService.forfeitTeam(1, 1)).thenReturn(forfeitedTeam);

            mockMvc.perform(post("/commissaire/trials/1/forfeit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.forfeit").value(true));

            verify(participantService).forfeitTeam(1, 1);
        }

        @Test
        @DisplayName("Devrait retourner 400 si participant non inscrit")
        void forfeit_shouldReturn400WhenNotRegistered() throws Exception {
            ForfeitRequest request = new ForfeitRequest();
            request.setParticipantId(999);
            request.setParticipantType("ATHLETE");

            when(participantService.forfeitAthlete(1, 999))
                    .thenThrow(new IllegalArgumentException("L'athlète n'est pas inscrit à cette épreuve"));

            mockMvc.perform(post("/commissaire/trials/1/forfeit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("L'athlète n'est pas inscrit à cette épreuve"));
        }
    }

    // ===== TESTS UNFORFEIT =====

    @Nested
    @DisplayName("POST /commissaire/trials/{trialId}/unforfeit")
    class UnforfeitTests {

        @Test
        @DisplayName("Devrait annuler le forfait d'un athlète")
        void unforfeit_shouldUnforfeitAthlete() throws Exception {
            ForfeitRequest request = new ForfeitRequest();
            request.setParticipantId(10);
            request.setParticipantType("ATHLETE");

            ParticipantDTO unforfeitedAthlete = new ParticipantDTO();
            unforfeitedAthlete.setId(10);
            unforfeitedAthlete.setName("John Doe");
            unforfeitedAthlete.setType("ATHLETE");
            unforfeitedAthlete.setForfeit(false);

            when(participantService.unforfeitAthlete(1, 10)).thenReturn(unforfeitedAthlete);

            mockMvc.perform(post("/commissaire/trials/1/unforfeit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.forfeit").value(false));

            verify(participantService).unforfeitAthlete(1, 10);
        }

        @Test
        @DisplayName("Devrait annuler le forfait d'une équipe")
        void unforfeit_shouldUnforfeitTeam() throws Exception {
            ForfeitRequest request = new ForfeitRequest();
            request.setParticipantId(1);
            request.setParticipantType("TEAM");

            ParticipantDTO unforfeitedTeam = new ParticipantDTO();
            unforfeitedTeam.setId(1);
            unforfeitedTeam.setName("Team France");
            unforfeitedTeam.setType("TEAM");
            unforfeitedTeam.setForfeit(false);

            when(participantService.unforfeitTeam(1, 1)).thenReturn(unforfeitedTeam);

            mockMvc.perform(post("/commissaire/trials/1/unforfeit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.forfeit").value(false));

            verify(participantService).unforfeitTeam(1, 1);
        }
    }

    // ===== TESTS REMOVE PARTICIPANT =====

    @Nested
    @DisplayName("DELETE /commissaire/trials/{trialId}/participants")
    class RemoveParticipantTests {

        @Test
        @DisplayName("Devrait retirer un athlète avec succès")
        void removeParticipant_shouldRemoveAthlete() throws Exception {
            doNothing().when(participantService).removeAthleteFromTrial(1, 10);

            mockMvc.perform(delete("/commissaire/trials/1/participants")
                            .param("participantId", "10")
                            .param("participantType", "ATHLETE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Participant retiré avec succès"));

            verify(participantService).removeAthleteFromTrial(1, 10);
        }

        @Test
        @DisplayName("Devrait retirer une équipe avec succès")
        void removeParticipant_shouldRemoveTeam() throws Exception {
            doNothing().when(participantService).removeTeamFromTrial(1, 1);

            mockMvc.perform(delete("/commissaire/trials/1/participants")
                            .param("participantId", "1")
                            .param("participantType", "TEAM"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Participant retiré avec succès"));

            verify(participantService).removeTeamFromTrial(1, 1);
        }

        @Test
        @DisplayName("Devrait retourner 400 pour type invalide")
        void removeParticipant_shouldReturn400ForInvalidType() throws Exception {
            mockMvc.perform(delete("/commissaire/trials/1/participants")
                            .param("participantId", "10")
                            .param("participantType", "INVALID"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Devrait retourner 400 si participant non inscrit")
        void removeParticipant_shouldReturn400WhenNotRegistered() throws Exception {
            doThrow(new IllegalArgumentException("L'athlète n'est pas inscrit à cette épreuve"))
                    .when(participantService).removeAthleteFromTrial(1, 999);

            mockMvc.perform(delete("/commissaire/trials/1/participants")
                            .param("participantId", "999")
                            .param("participantType", "ATHLETE"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("L'athlète n'est pas inscrit à cette épreuve"));
        }
    }
}
