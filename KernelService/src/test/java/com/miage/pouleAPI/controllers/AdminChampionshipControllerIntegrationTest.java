package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.dtos.championship.ChampionshipDTO;
import com.miage.pouleAPI.dtos.championship.CreateChampionshipRequestDTO;
import com.miage.pouleAPI.entity.Championship;
import com.miage.pouleAPI.repositories.ChampionshipRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminChampionshipControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ChampionshipRepository championshipRepo;
    @Autowired private ObjectMapper objectMapper;

    private static final String TEST_PREFIX = "IT_ADMIN_CHAMP_";
    private final List<Integer> createdIds = new ArrayList<>();

    @AfterEach
    void cleanup() {
        createdIds.forEach(id -> championshipRepo.findById(id).ifPresent(championshipRepo::delete));
        createdIds.clear();
        championshipRepo.findAll().stream()
                .filter(c -> c.getName() != null && c.getName().startsWith(TEST_PREFIX))
                .forEach(championshipRepo::delete);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldPersistChampionship_WithAllFields() throws Exception {
        String name = TEST_PREFIX + "NEW_CHAMP_";
        CreateChampionshipRequestDTO req = new CreateChampionshipRequestDTO(
                "Description complète", name, LocalDate.now(), LocalDate.now().plusDays(30)
        );

        mockMvc.perform(post("/admin/champs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());


        assertTrue(
                championshipRepo.findAll()
                        .stream()
                        .anyMatch(c -> c.getName().equals(name))
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn400_WhenNameIsMissing() throws Exception {
        CreateChampionshipRequestDTO req = new CreateChampionshipRequestDTO(
                null, "Description", LocalDate.now(), LocalDate.now().plusDays(30)
        );

        mockMvc.perform(post("/admin/champs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_shouldReturn403_WhenUserIsNotAdmin() throws Exception {
        mockMvc.perform(post("/admin/champs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_shouldReturn403_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/admin/champs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldFillEmptyFieldsFromDatabase() throws Exception {
        // Insertion directe en base (hors contexte HTTP)
        Championship existing = new Championship();
        existing.setName(TEST_PREFIX + "Nom_Initial");
        existing.setDescription("Description Initiale");
        existing.setStart(LocalDate.now());
        existing.setEnd(LocalDate.now().plusDays(10));
        existing = championshipRepo.saveAndFlush(existing);
        createdIds.add(existing.getId());

        ChampionshipDTO updateReq = new ChampionshipDTO("", null, existing.getId(), "", null);

        mockMvc.perform(put("/admin/champs/" + existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isAccepted());

        Championship result = championshipRepo.findById(existing.getId()).orElseThrow();
        assertThat(result.getName()).isEqualTo(TEST_PREFIX + "Nom_Initial");
        assertThat(result.getDescription()).isEqualTo("Description Initiale");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldModifyFields_WhenPayloadIsComplete() throws Exception {
        Championship existing = new Championship();
        existing.setName(TEST_PREFIX + "Ancien_Nom");
        existing.setDescription("Ancienne Description");
        existing.setStart(LocalDate.now());
        existing.setEnd(LocalDate.now().plusDays(10));
        existing = championshipRepo.saveAndFlush(existing);
        createdIds.add(existing.getId());

        ChampionshipDTO updateReq = new ChampionshipDTO(
                "Nouvelle Description" ,  LocalDate.now().plusDays(1), existing.getId(),
                "Nouveau Nom" , LocalDate.now().plusDays(20)
        );

        mockMvc.perform(put("/admin/champs/" + existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isAccepted());

        Championship result = championshipRepo.findById(existing.getId()).orElseThrow();
        assertThat(result.getName()).isEqualTo("Nouveau Nom");
        assertThat(result.getDescription()).isEqualTo("Nouvelle Description");
    }

    @Test
    @WithMockUser(roles = "USER")
    void update_shouldReturn403_WhenUserIsNotAdmin() throws Exception {
        mockMvc.perform(put("/admin/champs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}