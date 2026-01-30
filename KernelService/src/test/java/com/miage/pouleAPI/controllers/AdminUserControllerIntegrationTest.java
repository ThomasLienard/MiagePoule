package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.dtos.admin.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests d'intégration AdminUserController")
class AdminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Tests GET /admin/users")
    class GetAllUsersIntegrationTests {

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait retourner tous les utilisateurs de data.sql")
        void getAllUsers_shouldReturnUsersFromDataSql() throws Exception {
            mockMvc.perform(get("/admin/users")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(7))))
                .andExpect(jsonPath("$[?(@.email == 'anna@smith.com')]").exists())
                .andExpect(jsonPath("$[?(@.email == 'athlete@test.com')]").exists());
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait filtrer par rôle ATHLETE")
        void getAllUsers_shouldFilterByRoleAthlete() throws Exception {
            mockMvc.perform(get("/admin/users")
                    .param("role", "ATHLETE")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].roleName", everyItem(equalTo("ATHLETE"))));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait filtrer par rôle COMMISSAIRE")
        void getAllUsers_shouldFilterByRoleCommissaire() throws Exception {
            mockMvc.perform(get("/admin/users")
                    .param("role", "COMMISSAIRE")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].roleName", everyItem(equalTo("COMMISSAIRE"))));
        }
    }

    @Nested
    @DisplayName("Tests GET /admin/users/{id}")
    class GetUserByIdIntegrationTests {

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait retourner l'utilisateur Anna Smith (ID 1)")
        void getUserById_shouldReturnAnnaSmith() throws Exception {
            mockMvc.perform(get("/admin/users/1")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Anna"))
                .andExpect(jsonPath("$.lastname").value("Smith"))
                .andExpect(jsonPath("$.email").value("anna@smith.com"))
                .andExpect(jsonPath("$.roleName").value("ADMIN"));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait retourner 404 pour ID inexistant")
        void getUserById_shouldReturn404ForNonExistentId() throws Exception {
            mockMvc.perform(get("/admin/users/99999")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Tests POST /admin/users (Création)")
    class CreateUserIntegrationTests {

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait créer un nouvel utilisateur")
        void createUser_shouldCreateNewUser() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                "TestPrenom", "TestNom", "test.creation@example.com", "SPECTATEUR", "FR"
            );

            mockMvc.perform(post("/admin/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("TestPrenom"))
                .andExpect(jsonPath("$.lastname").value("TestNom"))
                .andExpect(jsonPath("$.email").value("test.creation@example.com"))
                .andExpect(jsonPath("$.roleName").value("SPECTATEUR"))
                .andExpect(jsonPath("$.temporaryPassword").value("testnom.testprenom"))
                .andExpect(jsonPath("$.message").value("Compte créé avec succès"));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait retourner erreur si email existe déjà")
        void createUser_shouldReturnErrorIfEmailExists() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                "Test", "User", "anna@smith.com", "SPECTATEUR", "FR"
            );

            mockMvc.perform(post("/admin/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Un compte avec cet email existe déjà"));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait retourner erreur si rôle invalide")
        void createUser_shouldReturnErrorIfRoleInvalid() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                "Test", "User", "test.invalid.role@example.com", "INVALID_ROLE", "FR"
            );

            mockMvc.perform(post("/admin/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests PUT /admin/users/{id}")
    class UpdateUserIntegrationTests {

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait mettre à jour le nom de l'utilisateur")
        void updateUser_shouldUpdateName() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest(
                "NouveauNom", null, null, null, null
            );

            mockMvc.perform(put("/admin/users/3")  // Marie Athlete
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NouveauNom"));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait mettre à jour l'email de l'utilisateur")
        void updateUser_shouldUpdateEmail() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest(
                null, null, "updated.email@test.com", null, null
            );

            mockMvc.perform(put("/admin/users/4")  // Jean Volontaire
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated.email@test.com"));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait retourner erreur si email déjà utilisé")
        void updateUser_shouldReturnErrorIfEmailUsed() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest(
                null, null, "anna@smith.com", null, null  // Email d'Anna
            );

            mockMvc.perform(put("/admin/users/3")  // Marie Athlete
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cet email est déjà utilisé"));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait changer le rôle de l'utilisateur")
        void updateUser_shouldUpdateRole() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest(
                null, null, null, "SPECTATEUR", null
            );

            mockMvc.perform(put("/admin/users/4")  // Jean Volontaire -> Spectateur
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("SPECTATEUR"));
        }
    }

    @Nested
    @DisplayName("Tests POST /admin/users/{id}/deactivate")
    class DeactivateUserIntegrationTests {

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait désactiver un utilisateur")
        void deactivateUser_shouldDeactivateUser() throws Exception {
            DeactivateUserRequest request = new DeactivateUserRequest("Violation des règles");

            mockMvc.perform(post("/admin/users/5/deactivate")  // John Doe
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.deactivationReason").value("Violation des règles"));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Ne devrait pas désactiver un administrateur")
        void deactivateUser_shouldNotDeactivateAdmin() throws Exception {
            DeactivateUserRequest request = new DeactivateUserRequest("Test raison");

            mockMvc.perform(post("/admin/users/1/deactivate")  // Anna Smith (ADMIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Impossible de désactiver un compte administrateur"));
        }
    }

    @Nested
    @DisplayName("Tests POST /admin/users/{id}/reactivate")
    class ReactivateUserIntegrationTests {

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait réactiver un utilisateur désactivé")
        void reactivateUser_shouldReactivateUser() throws Exception {
            // D'abord désactiver
            DeactivateUserRequest deactivateRequest = new DeactivateUserRequest("Test");
            mockMvc.perform(post("/admin/users/5/deactivate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(deactivateRequest)))
                .andExpect(status().isOk());

            // Puis réactiver
            mockMvc.perform(post("/admin/users/5/reactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.deactivationReason").isEmpty());
        }
    }

    @Nested
    @DisplayName("Tests POST /admin/users/{id}/reset-password")
    class ResetPasswordIntegrationTests {

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait réinitialiser le mot de passe")
        void resetPassword_shouldResetPassword() throws Exception {
            mockMvc.perform(post("/admin/users/3/reset-password"))  // Marie Athlete
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Mot de passe réinitialisé"))
                .andExpect(jsonPath("$.temporaryPassword").value("athlete.marie"));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait retourner erreur pour utilisateur inexistant")
        void resetPassword_shouldReturnErrorForNonExistentUser() throws Exception {
            mockMvc.perform(post("/admin/users/99999/reset-password"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests de sécurité")
    class SecurityTests {

        @Test
        @DisplayName("Devrait refuser l'accès sans authentification")
        void shouldDenyAccessWithoutAuth() throws Exception {
            mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
        }
    }
}
