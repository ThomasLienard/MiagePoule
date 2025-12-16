package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.domains.ChampionshipModel;
import com.miage.pouleAPI.domains.CompetitionModel;
import com.miage.pouleAPI.services.ChampionshipService;
import com.miage.pouleAPI.services.CompetitionService;
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
public class CompetitionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChampionshipService championshipService;

    private Integer champId;
    private Integer compId;

    @BeforeEach
    void setUp() {
        ChampionshipModel champ = new ChampionshipModel(
                "Champ pour comp",
                LocalDate.of(2026, 6, 30),
                1,
                "ChampComp",
                LocalDate.of(2026, 6, 20)
        );
        ChampionshipModel savedChamp = championshipService.save(champ);
        champId = savedChamp.getId();

        CompetitionModel comp = new CompetitionModel(
                champId,
                "Comp test",
                LocalDate.of(2026, 6, 22),
                1,
                "CompTest",
                LocalDate.of(2026, 6, 21)
        );
        compId = comp.getId();
    }

    @Test
    void shouldReturnCompetitionById() throws Exception {
        mockMvc.perform(get("/public/championship/{id}/comp/{idComp}", champId, compId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(champId))
                .andExpect(jsonPath("$.idComp").value(compId))
                .andExpect(jsonPath("$.name").value("CompTest"));
    }
}
