package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.domains.ChampionshipModel;
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
class ChampionshipControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllChampionships_returnsTwoFromDataSql() throws Exception {
        var mvcResult = mockMvc.perform(
                        get("/public/championship")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        List<ChampionshipModel> championships =
                objectMapper.readValue(json, new TypeReference<>() {});

        assertThat(championships)
                .isNotEmpty()
                .hasSize(2);

        assertThat(championships)
                .extracting(ChampionshipModel::getId)
                .containsExactlyInAnyOrder(1, 2);

        assertThat(championships)
                .extracting(ChampionshipModel::getName)
                .containsExactlyInAnyOrder("World Cup", "National League");
    }

    @Test
    void getChampionshipById_existing() throws Exception {
        var mvcResult = mockMvc.perform(
                        get("/public/championship/{id}", 1)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        ChampionshipModel champ =
                objectMapper.readValue(json, ChampionshipModel.class);

        assertThat(champ.getId()).isEqualTo(1);
        assertThat(champ.getName()).isEqualTo("World Cup");
        assertThat(champ.getDescription()).isEqualTo("World level championship");
    }

    @Test
    void getChampionshipById_notFound() throws Exception {
        mockMvc.perform(
                        get("/public/championship/{id}", 999)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }
}
