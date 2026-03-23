package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.auth.jwt.JwtService;
import com.miage.pouleAPI.config.SecurityConfig;
import com.miage.pouleAPI.dtos.agenda.AgendaUploadItemDTO;
import com.miage.pouleAPI.dtos.agenda.TaskUploadItemDTO;
import com.miage.pouleAPI.dtos.agenda.UploadAgendaResponse;
import com.miage.pouleAPI.dtos.agenda.VolunteerProcessResult;
import com.miage.pouleAPI.services.interfaces.VolunteerAgendaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAgendaController.class)
@Import(SecurityConfig.class)
class AdminAgendaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VolunteerAgendaService volunteerAgendaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /admin/agenda/upload - retourne 201 quand l'upload réussit")
    void uploadAgendas_shouldReturnCreated_whenServiceProcessesAllItems() throws Exception {
        List<AgendaUploadItemDTO> request = List.of(
                new AgendaUploadItemDTO(
                        "volontaire@test.com",
                        List.of(new TaskUploadItemDTO(
                                "Préparer la piste demain",
                                "Test webmvc",
                                "Marathon",
                                "Final Sprint Race"
                        ))
                )
        );

        UploadAgendaResponse response = new UploadAgendaResponse(
                1,
                1,
                0,
                List.of(new VolunteerProcessResult("volontaire@test.com", true, 1, "OK"))
        );

        when(volunteerAgendaService.uploadAgendas(anyList())).thenReturn(response);

        mockMvc.perform(post("/admin/agenda/upload")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalVolunteers").value(1))
                .andExpect(jsonPath("$.successfullyProcessed").value(1))
                .andExpect(jsonPath("$.failed").value(0));
    }
}
