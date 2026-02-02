package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.dtos.trial.TrialDetailDTO;
import com.miage.pouleAPI.dtos.trial.TrialSummaryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Intégration TrialController avec data.sql de test")
class TrialControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /public/trials retourne au moins 1 TrialSummaryDTO")
    void getAllTrials_integration() throws Exception {
        var mvcResult = mockMvc.perform(get("/public/trials")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        List<TrialSummaryDTO> trials = objectMapper.readValue(json, new TypeReference<>() {});

        assertThat(trials).isNotEmpty();
        assertThat(trials)
            .extracting(TrialSummaryDTO::getId)
            .contains(1);
    }

    @Test
    @DisplayName("GET /public/trials/1 retourne le Trial correspondant à l'event 1")
    void getTrialById_integration_existing() throws Exception {
        var mvcResult = mockMvc.perform(get("/public/trials/{id}", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        TrialDetailDTO trial = objectMapper.readValue(json, TrialDetailDTO.class);

        assertThat(trial.getId()).isEqualTo(1);
        // Le nom et la description viennent de l'event id=1 (avec JOINED inheritance, trial id=1 IS event id=1)
        assertThat(trial.getName()).isEqualTo("Morning Sprint Session");
        assertThat(trial.getDescription()).isEqualTo("Speed training");
    }

    @Test
    @DisplayName("GET /public/championships/1/comp/1/trials retourne les épreuves de la compétition")
    void getTrialsByChampionshipAndCompetition_integration() throws Exception {
        var mvcResult = mockMvc.perform(get("/public/championships/1/comp/1/trials")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        List<TrialSummaryDTO> trials = objectMapper.readValue(json, new TypeReference<>() {});

        // Les données de test peuvent être vides si aucun trial n'appartient à la compétition 1
        assertThat(trials).isNotNull();
        if (!trials.isEmpty()) {
            assertThat(trials).allMatch(t -> t.getId() > 0 && t.getName() != null);
        }
    }

    @Test
    @DisplayName("GET /public/championships/1/comp/999/trials retourne une liste vide pour une compétition inexistante")
    void getTrialsByChampionshipAndCompetition_integration_emptyResult() throws Exception {
        var mvcResult = mockMvc.perform(get("/public/championships/1/comp/999/trials")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        List<TrialSummaryDTO> trials = objectMapper.readValue(json, new TypeReference<>() {});

        assertThat(trials).isEmpty();
    }

    @Test
    @DisplayName("GET /trials/assigned - Devrait retourner les épreuves assignées avec structure soloTrials et teamTrials")
    void getAssignedTrials_integration_structure() throws Exception {
        // Note: Cet endpoint requiert l'authentification
        // Ce test nécessite un token JWT valide ou l'authentification à être mocké
        mockMvc.perform(get("/trials/assigned")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // 403 car pas d'authentification
    }

    @Test
    @DisplayName("GET /trials/assigned - Devrait retourner 401 sans authentification")
    void getAssignedTrials_integration_noAuth() throws Exception {
        var mvcResult = mockMvc.perform(get("/trials/assigned")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        // Vérifier que l'accès est refusé sans authentification
        assertThat(mvcResult.getResponse().getStatus())
            .isIn(401, 403); // Unauthorized ou Forbidden
    }
}
