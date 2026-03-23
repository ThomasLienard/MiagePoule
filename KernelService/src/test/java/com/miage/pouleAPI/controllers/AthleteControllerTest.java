package com.miage.pouleAPI.controllers;


import com.miage.pouleAPI.dtos.participant.AthleteDTO;
import com.miage.pouleAPI.dtos.participant.ParticipantDTO;
import com.miage.pouleAPI.services.interfaces.ParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests AthleteController - Gestion des forfaits pour les sportifs")
class AthleteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ParticipantService participantService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AthleteController athleteController;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(athleteController).build();
    }

    @Nested
    @DisplayName("POST /athlete/trials/{trialId}/forfeit")
    class DeclareWithdrawalTests {

        @Test
        @DisplayName("Devrait permettre à un athlète de déclarer forfait")
        void declareWithdrawal_shouldSucceed() throws Exception {
            ParticipantDTO result = new ParticipantDTO();
            result.setId(1);
            result.setName("John Doe");
            result.setType("ATHLETE");
            result.setForfeit(true);

            when(authentication.getName()).thenReturn("athlete@test.com");
            when(participantService.athleteDeclareWithdrawal(1, "athlete@test.com")).thenReturn(result);

            mockMvc.perform(post("/athlete/trials/1/forfeit")
                            .principal(authentication))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.forfeit").value(true));

            verify(participantService).athleteDeclareWithdrawal(1, "athlete@test.com");
        }

        @Test
        @DisplayName("Devrait retourner 400 si l'athlète n'est pas inscrit")
        void declareWithdrawal_shouldReturn400WhenNotRegistered() throws Exception {
            when(authentication.getName()).thenReturn("athlete@test.com");
            when(participantService.athleteDeclareWithdrawal(1, "athlete@test.com"))
                    .thenThrow(new IllegalArgumentException("Vous n'êtes pas inscrit à cette épreuve"));

            mockMvc.perform(post("/athlete/trials/1/forfeit")
                            .principal(authentication))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Vous n'êtes pas inscrit à cette épreuve"));
        }

        @Test
        @DisplayName("Devrait retourner 400 si l'épreuve est déjà terminée")
        void declareWithdrawal_shouldReturn400WhenTrialFinished() throws Exception {
            when(authentication.getName()).thenReturn("athlete@test.com");
            when(participantService.athleteDeclareWithdrawal(1, "athlete@test.com"))
                    .thenThrow(new IllegalStateException("Impossible de déclarer forfait : l'épreuve est déjà terminée"));

            mockMvc.perform(post("/athlete/trials/1/forfeit")
                            .principal(authentication))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Impossible de déclarer forfait : l'épreuve est déjà terminée"));
        }

        @Test
        @DisplayName("Devrait retourner 400 si déjà forfait")
        void declareWithdrawal_shouldReturn400WhenAlreadyForfeit() throws Exception {
            when(authentication.getName()).thenReturn("athlete@test.com");
            when(participantService.athleteDeclareWithdrawal(1, "athlete@test.com"))
                    .thenThrow(new IllegalStateException("Vous êtes déjà déclaré forfait pour cette épreuve"));

            mockMvc.perform(post("/athlete/trials/1/forfeit")
                            .principal(authentication))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Vous êtes déjà déclaré forfait pour cette épreuve"));
        }
    }

    @Nested
    @DisplayName("GET /public/athlete/{athleteId}")
    class GetAthleteByIdTests {

        @Test
        @DisplayName("Devrait renvoyer un athlete existant")
        void getAthleteById_shouldSucceed() throws Exception {
            AthleteDTO result = new AthleteDTO();
            result.setId(1);
            result.setFullName("Jean Poule");
            result.setCountry("FR");

            when(participantService.getAthleteById(1)).thenReturn(Optional.of(result));

            mockMvc.perform(get("/public/athlete/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(result.getId()))
                    .andExpect(jsonPath("$.fullName").value(result.getFullName()))
                    .andExpect(jsonPath("$.country").value(result.getCountry()));

            verify(participantService).getAthleteById(1);
        }

        @Test
        @DisplayName("Devrait renvoyer une erreur 404 quand l'athlète n'existe pas")
        void getAthleteById_shouldFail() throws Exception {
            when(participantService.getAthleteById(999)).thenReturn(Optional.empty());

            mockMvc.perform(get("/public/athlete/999"))
                    .andExpect(status().isNotFound());

            verify(participantService).getAthleteById(999);
        }
    }
}
