package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.dtos.profile.ChangePasswordRequestDTO;
import com.miage.pouleAPI.dtos.profile.UpdateProfileRequestDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationUserRepository userRepo;

    private final String CURRENT_EMAIL = "commissaire@test.com";
    private final String DB_PASSWORD = "test123";

    @Test
    @WithMockUser(username = CURRENT_EMAIL, roles = {"COMMISSAIRE"})
    void changePassword_IntegrationTest() throws Exception {
        ChangePasswordRequestDTO dto = new ChangePasswordRequestDTO();
        dto.setCurrentPassword(DB_PASSWORD);
        dto.setNewPassword("NouveauPass123!");

        mockMvc.perform(put("/account/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent()); // Changé en 204 No Content
    }

    @Test
    @WithMockUser(username = CURRENT_EMAIL, roles = {"COMMISSAIRE"})
    void updateProfile_IntegrationTest() throws Exception {
        UpdateProfileRequestDTO updateDto = new UpdateProfileRequestDTO();
        updateDto.setName("NouveauNom");
        updateDto.setLastname("NouveauPrenom");
        updateDto.setEmail("nouveau.email@test.com");

        mockMvc.perform(put("/account/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value("NouveauNom"))
                .andExpect(jsonPath("$.user.lastname").value("NouveauPrenom"))
                .andExpect(jsonPath("$.user.email").value("nouveau.email@test.com"))
                .andExpect(jsonPath("$.token").exists());

        ApplicationUser updatedUser = userRepo.findByEmail("nouveau.email@test.com").orElseThrow();
        assertEquals("NouveauNom", updatedUser.getName());
    }

    @Test
    @WithMockUser(username = CURRENT_EMAIL, roles = {"COMMISSAIRE"})
    void updateProfile_PartialUpdate_IntegrationTest() throws Exception {
        UpdateProfileRequestDTO partialDto = new UpdateProfileRequestDTO();
        partialDto.setName("NouveauNomSeulement");

        mockMvc.perform(put("/account/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value("NouveauNomSeulement"))
                .andExpect(jsonPath("$.user.email").value(CURRENT_EMAIL));

        ApplicationUser userInDb = userRepo.findByEmail(CURRENT_EMAIL).orElseThrow();
        assertEquals("NouveauNomSeulement", userInDb.getName());
    }

    @Test
    @WithMockUser(username = "athlete@test.com", roles = {"ATHLETE"})
    void signCharter_Athlete_IntegrationTest() throws Exception {

        mockMvc.perform(post("/account/sign-charter"))
                .andExpect(status().isOk());

        ApplicationUser user = userRepo.findByEmail("athlete@test.com").orElseThrow();
        assertEquals(true, user.getHasSignedCharter());
    }

    @Test
    @WithMockUser(username = CURRENT_EMAIL, roles = {"COMMISSAIRE"})
    void signCharter_NonAthlete_ShouldBeForbidden_IntegrationTest() throws Exception {

        mockMvc.perform(post("/account/sign-charter"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}