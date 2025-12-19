package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
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
class CompetitionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCompetitionsByChampionship_returnsTwoFromDataSql() throws Exception {
        var mvcResult = mockMvc.perform(
                        get("/public/championship/{id}/comp", 1)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        List<CompetitionDTO> competitions =
                objectMapper.readValue(json, new TypeReference<>() {});

        assertThat(competitions)
                .isNotEmpty()
                .hasSize(2);

        assertThat(competitions)
                .extracting(CompetitionDTO::getId)
                .containsExactlyInAnyOrder(1, 2);

        assertThat(competitions)
                .extracting(CompetitionDTO::getName)
                .containsExactlyInAnyOrder("100m Sprint", "Marathon");
    }

    @Test
    void getCompetitionById_existing() throws Exception {
        var mvcResult = mockMvc.perform(
                        get("/public/championship/{id}/comp/{idComp}", 1, 2)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        CompetitionDTO comp =
                objectMapper.readValue(json, CompetitionDTO.class);

        assertThat(comp.getId()).isEqualTo(2);
        assertThat(comp.getName()).isEqualTo("Marathon");
        assertThat(comp.getDescription()).isEqualTo("Long distance run");
        assertThat(comp.getChampionshipId()).isEqualTo(1);
    }

    @Test
    void getCompetitionById_notFound() throws Exception {
        mockMvc.perform(
                        get("/public/championship/{id}/comp/{idComp}", 1, 999)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }
}
