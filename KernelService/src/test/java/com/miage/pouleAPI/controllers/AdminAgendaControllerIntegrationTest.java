package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.dtos.agenda.AgendaUploadItemDTO;
import com.miage.pouleAPI.dtos.agenda.TaskUploadItemDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tests d'intégration AdminAgendaController")
class AdminAgendaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    @DisplayName("POST /admin/agenda/upload - devrait retourner 400 si aucun événement n'est planifié pour demain")
    void uploadAgenda_shouldReturnBadRequest_whenNoTomorrowEvent() throws Exception {
        List<AgendaUploadItemDTO> request = List.of(
                new AgendaUploadItemDTO(
                        "volontaire@test.com",
                        List.of(new TaskUploadItemDTO(
                                "Préparer la piste",
                                "Test integration",
                                "100m Sprint",
                                "Morning Sprint Session"
                        ))
                )
        );

        mockMvc.perform(post("/admin/agenda/upload")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.totalVolunteers").value(1))
                .andExpect(jsonPath("$.successfullyProcessed").value(0))
                .andExpect(jsonPath("$.failed").value(1));
    }

    @Test
    @WithMockUser(username = "athlete@test.com", roles = {"ATHLETE"})
    @DisplayName("POST /admin/agenda/upload - devrait retourner 403 pour un non-admin")
    void uploadAgenda_shouldReturnForbidden_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(post("/admin/agenda/upload")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /admin/agenda/upload - devrait retourner 403 sans authentification")
    void uploadAgenda_shouldReturnForbidden_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/admin/agenda/upload")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isForbidden());
    }
}
