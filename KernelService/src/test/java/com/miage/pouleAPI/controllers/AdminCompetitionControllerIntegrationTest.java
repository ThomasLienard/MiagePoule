package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import com.miage.pouleAPI.dtos.competition.CreateCompetitionRequestDTO;
import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.repositories.CompetitionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminCompetitionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldPersistCompetition() throws Exception {

        CreateCompetitionRequestDTO request =
                new CreateCompetitionRequestDTO(
                        "IntegrationComp",
                        "Desc",
                        1,
                        LocalDate.now(),
                        LocalDate.now().plusDays(2)
                );

        mockMvc.perform(post("/admin/comps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertTrue(
                competitionRepository.findAll()
                        .stream()
                        .anyMatch(c -> c.getName().equals("IntegrationComp"))
        );
    }

    @Test
    void create_shouldReturn403_whenAnonymous() throws Exception {
        mockMvc.perform(post("/admin/comps"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(post("/admin/comps"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldModifyExistingCompetition() throws Exception {
        Competition existing = competitionRepository.findAll().get(0);
        Integer id = existing.getId();

        CompetitionDTO updateRequest = new CompetitionDTO(
                existing.getChampionship().getId(),
                "Description modifiée",
                existing.getEnd(),
                id,
                "Nouveau Nom Comp",
                existing.getStart()
        );

        mockMvc.perform(put("/admin/comps/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isAccepted());

        Competition updated = competitionRepository.findById(id).orElseThrow();
        assertEquals("Nouveau Nom Comp", updated.getName());
        assertEquals("Description modifiée", updated.getDescription());
    }
}