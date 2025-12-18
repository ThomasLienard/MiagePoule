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
    @DisplayName("GET /public/trials/1 retourne le Trial lié à l'event 2")
    void getTrialById_integration_existing() throws Exception {
        var mvcResult = mockMvc.perform(get("/public/trials/{id}", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        TrialDetailDTO trial = objectMapper.readValue(json, TrialDetailDTO.class);

        assertThat(trial.getId()).isEqualTo(1);
        // Le nom et la description viennent de l'event id=2
        assertThat(trial.getName()).isEqualTo("Final Sprint Race");
        assertThat(trial.getDescription()).isEqualTo("Official competition");
    }
}
