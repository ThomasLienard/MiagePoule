package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.dtos.event.CreateEventRequestDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminEventControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void shouldAcceptValidEventRequest() throws Exception {
        CreateEventRequestDTO request = new CreateEventRequestDTO(
                "Nouveau Test" + System.currentTimeMillis(), "Description", "TRIAL", 1,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(5).plusHours(1),
                "Stade Unique", "Paris", "Avenue", "99", "75000",
                "PMR", 10.0, 10.0, true, null
        );

        mockMvc.perform(post("/admin/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void shouldReturnBadRequestWhenNameIsMissing() throws Exception {
        CreateEventRequestDTO invalidRequest = new CreateEventRequestDTO(
                null, "Description", "TRIAL", 1,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(5).plusHours(1),
                "Stade", "Paris", "Rue", "1", "75000",
                "PMR", 10.0, 10.0, true, null
        );

        mockMvc.perform(post("/admin/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void shouldForbiddenWhenUserIsNotAdmin() throws Exception {
        CreateEventRequestDTO request = new CreateEventRequestDTO(
                "Test Fraude", "Desc", "TRIAL", 1,
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(1),
                "Lieu", "Ville", "Rue", "1", "00000",
                "NONE", 5.0, 5.0, false, null
        );

        mockMvc.perform(post("/admin/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldForbiddenWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/admin/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}