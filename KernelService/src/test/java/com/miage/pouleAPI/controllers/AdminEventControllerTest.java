package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.auth.jwt.JwtService;
import com.miage.pouleAPI.config.SecurityConfig;
import com.miage.pouleAPI.dtos.event.CreateEventRequestDTO;
import com.miage.pouleAPI.services.interfaces.AdminEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminEventController.class)
@Import(SecurityConfig.class)
class AdminEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminEventService adminEventService;

    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_ShouldReturnCreated_WhenPayloadIsValid() throws Exception {
        CreateEventRequestDTO request = new CreateEventRequestDTO(
            "Test Event", "Desc", "TRIAL", 1,
            LocalDateTime.now(), LocalDateTime.now().plusHours(1),
            "Stade", "Paris", "Rue", "10", "75000", "Détails", 
            0.0, 0.0, true
        );

        doNothing().when(adminEventService).createEvent(any(CreateEventRequestDTO.class));

        mockMvc.perform(post("/admin/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(adminEventService).createEvent(any(CreateEventRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        mockMvc.perform(post("/admin/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }
}