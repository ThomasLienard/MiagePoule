package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.domains.ChampionshipModel;
import com.miage.pouleAPI.services.ChampionshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChampionshipControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChampionshipService championshipService;

    private Integer champId;

    @BeforeEach
    void setUp() {
        ChampionshipModel model = new ChampionshipModel(
                "Champ test",
                LocalDate.of(2026, 5, 20),
                4,
                "ChampTest",
                LocalDate.of(2026, 5, 10)
        );
        champId = championshipService.save(model).getId();
    }

    @Test
    void shouldReturnAllChampionships() throws Exception {
        mockMvc.perform(get("/public/championship"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void shouldReturnChampionshipById() throws Exception {
        mockMvc.perform(get("/public/championship/{id}", champId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(champId))
                .andExpect(jsonPath("$.name").value("ChampTest"));
    }

    @Test
    void shouldReturnCompetitionsForChampionship() throws Exception {
        mockMvc.perform(get("/public/championship/{id}/comp", champId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
