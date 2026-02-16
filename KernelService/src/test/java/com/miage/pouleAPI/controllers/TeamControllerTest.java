package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.auth.jwt.JwtService;
import com.miage.pouleAPI.config.SecurityConfig;
import com.miage.pouleAPI.dtos.team.CreateTeamRequestDTO;
import com.miage.pouleAPI.dtos.team.TeamDTO;
import com.miage.pouleAPI.dtos.team.TeamMemberDTO;
import com.miage.pouleAPI.dtos.team.UpdateTeamRequestDTO;
import com.miage.pouleAPI.services.interfaces.TeamService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamController.class)
@Import(SecurityConfig.class)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private TeamDTO teamDTO;
    private Set<TeamMemberDTO> members;

    @BeforeEach
    void setUp() {
        TeamMemberDTO member1 = new TeamMemberDTO();
        member1.setId(1);
        member1.setName("John");
        member1.setLastname("Doe");
        member1.setCountryCode("US");

        TeamMemberDTO member2 = new TeamMemberDTO();
        member2.setId(2);
        member2.setName("Jane");
        member2.setLastname("Smith");
        member2.setCountryCode("FR");

        members = new HashSet<>(Arrays.asList(member1, member2));

        teamDTO = new TeamDTO();
        teamDTO.setId(1);
        teamDTO.setName("Team A");
        teamDTO.setCountryCode("FR");
        teamDTO.setMembers(members);
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void getAllTeams_ShouldReturnAllTeams() throws Exception {
        List<TeamDTO> teams = Arrays.asList(teamDTO);
        when(teamService.findAll()).thenReturn(teams);

        mockMvc.perform(get("/commissaire/teams")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Team A"))
                .andExpect(jsonPath("$[0].countryCode").value("FR"))
                .andExpect(jsonPath("$[0].members", hasSize(2)));

        verify(teamService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void getAllTeams_ShouldReturnEmptyList_WhenNoTeams() throws Exception {
        when(teamService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/commissaire/teams")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(teamService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void getTeamById_ShouldReturnTeam_WhenExists() throws Exception {
        when(teamService.findById(1)).thenReturn(Optional.of(teamDTO));

        mockMvc.perform(get("/commissaire/teams/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Team A"))
                .andExpect(jsonPath("$.countryCode").value("FR"))
                .andExpect(jsonPath("$.members", hasSize(2)));

        verify(teamService, times(1)).findById(1);
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void getTeamById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        when(teamService.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/commissaire/teams/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(teamService, times(1)).findById(999);
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void createTeam_ShouldReturnCreated_WhenPayloadIsValid() throws Exception {
        CreateTeamRequestDTO request = new CreateTeamRequestDTO();
        request.setName("New Team");
        request.setCountryCode("FR");
        request.setMemberIds(new HashSet<>(Arrays.asList(1, 2)));

        when(teamService.create(any(CreateTeamRequestDTO.class))).thenReturn(teamDTO);

        mockMvc.perform(post("/commissaire/teams")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Team A"))
                .andExpect(jsonPath("$.countryCode").value("FR"));

        verify(teamService, times(1)).create(any(CreateTeamRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void createTeam_ShouldReturnBadRequest_WhenNameIsBlank() throws Exception {
        CreateTeamRequestDTO request = new CreateTeamRequestDTO();
        request.setName("");
        request.setCountryCode("FR");
        request.setMemberIds(new HashSet<>());

        mockMvc.perform(post("/commissaire/teams")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(teamService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void createTeam_ShouldReturnBadRequest_WhenCountryNotFound() throws Exception {
        CreateTeamRequestDTO request = new CreateTeamRequestDTO();
        request.setName("New Team");
        request.setCountryCode("XX");
        request.setMemberIds(new HashSet<>());

        when(teamService.create(any(CreateTeamRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Le pays avec le code XX n'existe pas"));

        mockMvc.perform(post("/commissaire/teams")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Le pays avec le code XX n'existe pas"));

        verify(teamService, times(1)).create(any(CreateTeamRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void updateTeam_ShouldReturnUpdatedTeam_WhenPayloadIsValid() throws Exception {
        UpdateTeamRequestDTO request = new UpdateTeamRequestDTO();
        request.setName("Updated Team");
        request.setCountryCode("US");
        request.setMemberIds(new HashSet<>(Arrays.asList(1)));

        TeamDTO updatedTeam = new TeamDTO();
        updatedTeam.setId(1);
        updatedTeam.setName("Updated Team");
        updatedTeam.setCountryCode("US");
        updatedTeam.setMembers(Collections.singleton(members.iterator().next()));

        when(teamService.update(eq(1), any(UpdateTeamRequestDTO.class))).thenReturn(updatedTeam);

        mockMvc.perform(put("/commissaire/teams/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Team"))
                .andExpect(jsonPath("$.countryCode").value("US"));

        verify(teamService, times(1)).update(eq(1), any(UpdateTeamRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void updateTeam_ShouldReturnBadRequest_WhenTeamNotFound() throws Exception {
        UpdateTeamRequestDTO request = new UpdateTeamRequestDTO();
        request.setName("Updated Team");
        request.setCountryCode("FR");
        request.setMemberIds(new HashSet<>());

        when(teamService.update(eq(999), any(UpdateTeamRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("L'équipe avec l'ID 999 n'existe pas"));

        mockMvc.perform(put("/commissaire/teams/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("L'équipe avec l'ID 999 n'existe pas"));

        verify(teamService, times(1)).update(eq(999), any(UpdateTeamRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void deleteTeam_ShouldReturnOk_WhenTeamExists() throws Exception {
        doNothing().when(teamService).delete(1);

        mockMvc.perform(delete("/commissaire/teams/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Équipe supprimée avec succès"));

        verify(teamService, times(1)).delete(1);
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void deleteTeam_ShouldReturnBadRequest_WhenTeamNotFound() throws Exception {
        doThrow(new IllegalArgumentException("L'équipe avec l'ID 999 n'existe pas"))
                .when(teamService).delete(999);

        mockMvc.perform(delete("/commissaire/teams/999")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("L'équipe avec l'ID 999 n'existe pas"));

        verify(teamService, times(1)).delete(999);
    }

    @Test
    @WithMockUser(roles = "ATHLETE")
    void getAllTeams_ShouldReturnForbidden_WhenUserIsNotCommissaire() throws Exception {
        mockMvc.perform(get("/commissaire/teams")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(teamService, never()).findAll();
    }

    @Test
    @WithMockUser(roles = "ATHLETE")
    void createTeam_ShouldReturnForbidden_WhenUserIsNotCommissaire() throws Exception {
        CreateTeamRequestDTO request = new CreateTeamRequestDTO();
        request.setName("New Team");
        request.setCountryCode("FR");
        request.setMemberIds(new HashSet<>());

        mockMvc.perform(post("/commissaire/teams")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(teamService, never()).create(any());
    }

    @Test
    void getAllTeams_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/commissaire/teams")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(teamService, never()).findAll();
    }
}
