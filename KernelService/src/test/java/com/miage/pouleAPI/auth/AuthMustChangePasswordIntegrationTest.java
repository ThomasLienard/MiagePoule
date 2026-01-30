package com.miage.pouleAPI.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.auth.dto.LoginRequest;
import com.miage.pouleAPI.dtos.admin.ActivateAccountRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tests d'intégration Auth - Fonctionnalité MustChangePassword")
class AuthMustChangePasswordIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Tests login complet avec base de données")
    class LoginIntegrationTests {

        @Test
        @DisplayName("Login d'un utilisateur existant (data.sql) devrait retourner mustChangePassword=false")
        void login_existingUser_shouldReturnMustChangePasswordFalse() throws Exception {
            // L'utilisateur anna@smith.com dans data.sql a is_account_activated=true et must_change_password=false
            // mot de passe: test123
            LoginRequest request = new LoginRequest("anna@smith.com", "test123");

            mockMvc.perform(post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.mustChangePassword").value(false))
                .andExpect(jsonPath("$.isAccountActivated").value(true));
        }

        @Test
        @DisplayName("Login devrait échouer pour un utilisateur inexistant")
        void login_nonexistentUser_shouldFail() throws Exception {
            LoginRequest request = new LoginRequest("nonexistent@example.com", "password");

            // Le serveur retourne 403 (Forbidden) quand BadCredentialsException est lancé
            mockMvc.perform(post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Login devrait retourner tous les champs requis")
        void login_shouldReturnAllFields() throws Exception {
            // mot de passe: test123
            LoginRequest request = new LoginRequest("anna@smith.com", "test123");

            mockMvc.perform(post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.mustChangePassword").isBoolean())
                .andExpect(jsonPath("$.isAccountActivated").isBoolean());
        }
    }

    @Nested
    @DisplayName("Tests activation de compte")
    @Transactional
    class ActivateAccountIntegrationTests {

        @Test
        @DisplayName("Activation devrait réussir avec email et nouveau mot de passe valides")
        void activateAccount_shouldSucceed() throws Exception {
            // Utiliser l'utilisateur non activé créé dans data.sql (newuser@test.com avec is_account_activated = false)
            ActivateAccountRequest activateRequest = new ActivateAccountRequest("newSecurePassword123");
            
            mockMvc.perform(post("/auth/activate")
                    .with(csrf())
                    .param("email", "newuser@test.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(activateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Activation devrait échouer pour un compte déjà activé")
        void activateAccount_shouldFailForAlreadyActivatedAccount() throws Exception {
            // Utiliser un utilisateur déjà activé
            ActivateAccountRequest activateRequest = new ActivateAccountRequest("newPassword123");
            
            mockMvc.perform(post("/auth/activate")
                    .with(csrf())
                    .param("email", "athlete@test.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(activateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ce compte est déjà activé"));
        }

        @Test
        @DisplayName("Activation devrait échouer pour un email invalide")
        void activateAccount_shouldFailForInvalidEmail() throws Exception {
            ActivateAccountRequest activateRequest = new ActivateAccountRequest("newPassword123");
            
            mockMvc.perform(post("/auth/activate")
                    .with(csrf())
                    .param("email", "nonexistent@example.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(activateRequest)))
                .andExpect(status().isBadRequest());
        }
    }
}
