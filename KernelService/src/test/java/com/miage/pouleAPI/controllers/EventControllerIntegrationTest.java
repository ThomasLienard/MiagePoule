package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.dtos.event.EventDetailDTO;
import com.miage.pouleAPI.dtos.event.EventSummaryDTO;
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
@DisplayName("Intégration EventController avec data.sql de test")
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /public/events retourne une liste de 2 EventSummaryDTO")
    void getAllEvents_integration() throws Exception {
        var mvcResult = mockMvc.perform(get("/public/events")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        List<EventSummaryDTO> events = objectMapper.readValue(json, new TypeReference<>() {});

        assertThat(events).isNotEmpty().hasSize(5);
        assertThat(events)
            .extracting(EventSummaryDTO::getId)
            .containsExactlyInAnyOrder(1, 2, 3, 4, 5);
    }

    @Test
    @DisplayName("GET /public/events/2 retourne 'Final Sprint Race'")
    void getEventById_integration_existing() throws Exception {
        var mvcResult = mockMvc.perform(get("/public/events/{id}", 2)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        EventDetailDTO event = objectMapper.readValue(json, EventDetailDTO.class);

        assertThat(event.getId()).isEqualTo(2);
        assertThat(event.getName()).isEqualTo("Final Sprint Race");
        assertThat(event.getDescription()).isEqualTo("Official competition");
    }

    @Test
    @DisplayName("GET /public/championships/1/comp/1/events retourne les événements de la compétition")
    void getEventsByChampionshipAndCompetition_integration() throws Exception {
        var mvcResult = mockMvc.perform(get("/public/championships/1/comp/1/events")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        List<EventSummaryDTO> events = objectMapper.readValue(json, new TypeReference<>() {});

        assertThat(events).isNotEmpty();
        assertThat(events).allMatch(e -> e.getId() > 0 && e.getName() != null);
    }

    @Test
    @DisplayName("GET /public/championships/1/comp/999/events retourne une liste vide pour une compétition inexistante")
    void getEventsByChampionshipAndCompetition_integration_emptyResult() throws Exception {
        var mvcResult = mockMvc.perform(get("/public/championships/1/comp/999/events")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        List<EventSummaryDTO> events = objectMapper.readValue(json, new TypeReference<>() {});

        assertThat(events).isEmpty();
    }
}
