package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.auth.repository.ApplicationUserRepository;
import com.miage.pouleAPI.dtos.profile.ChangePasswordRequestDTO;
import com.miage.pouleAPI.dtos.profile.UpdateProfileRequestDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
                .andExpect(jsonPath("$.name").value("NouveauNom"))
                .andExpect(jsonPath("$.lastname").value("NouveauPrenom"))
                .andExpect(jsonPath("$.email").value("nouveau.email@test.com"));

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
                .andExpect(jsonPath("$.name").value("NouveauNomSeulement"))
                .andExpect(jsonPath("$.email").value(CURRENT_EMAIL)); // L'email ne doit pas avoir changé

        // 3. Vérification en base
        ApplicationUser userInDb = userRepo.findByEmail(CURRENT_EMAIL).orElseThrow();
        assertEquals("NouveauNomSeulement", userInDb.getName());
        assertNotNull(userInDb.getLastname()); // Vérifie que le nom de famille n'a pas été effacé
    }
}