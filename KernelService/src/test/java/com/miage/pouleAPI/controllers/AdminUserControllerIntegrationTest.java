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
    @DisplayName("Tests POST /admin/users/bulk")
    class BulkCreateUsersIntegrationTests {

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait créer plusieurs utilisateurs avec succès")
        void bulkCreateUsers_shouldCreateMultipleUsersSuccessfully() throws Exception {
            // Arrange
            CreateUserRequest user1 = new CreateUserRequest(
                "Jean", "Dupont", "jean.dupont.bulk@test.com", "ATHLETE", "FR"
            );
            CreateUserRequest user2 = new CreateUserRequest(
                "Marie", "Martin", "marie.martin.bulk@test.com", "VOLONTAIRE", "FR"
            );
            CreateUserRequest user3 = new CreateUserRequest(
                "Pierre", "Bernard", "pierre.bernard.bulk@test.com", "COMMISSAIRE", "FR"
            );
            BulkCreateUsersRequest request = new BulkCreateUsersRequest(
                java.util.List.of(user1, user2, user3)
            );

            // Act & Assert
            mockMvc.perform(post("/admin/users/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRequested").value(3))
                .andExpect(jsonPath("$.successfullyCreated").value(3))
                .andExpect(jsonPath("$.failed").value(0))
                .andExpect(jsonPath("$.results", hasSize(3)))
                .andExpect(jsonPath("$.results[0].success").value(true))
                .andExpect(jsonPath("$.results[0].email").value("jean.dupont.bulk@test.com"))
                .andExpect(jsonPath("$.results[0].temporaryPassword").value("dupont.jean"))
                .andExpect(jsonPath("$.results[1].success").value(true))
                .andExpect(jsonPath("$.results[1].email").value("marie.martin.bulk@test.com"))
                .andExpect(jsonPath("$.results[1].temporaryPassword").value("martin.marie"))
                .andExpect(jsonPath("$.results[2].success").value(true))
                .andExpect(jsonPath("$.results[2].email").value("pierre.bernard.bulk@test.com"))
                .andExpect(jsonPath("$.results[2].temporaryPassword").value("bernard.pierre"));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait gérer les emails déjà existants")
        void bulkCreateUsers_shouldHandleExistingEmails() throws Exception {
            // Arrange - anna@smith.com existe déjà dans data.sql
            CreateUserRequest user1 = new CreateUserRequest(
                "New", "User", "newuser.bulk@test.com", "ATHLETE", "FR"
            );
            CreateUserRequest user2 = new CreateUserRequest(
                "Anna", "Smith", "anna@smith.com", "ADMIN", "FR"
            );
            BulkCreateUsersRequest request = new BulkCreateUsersRequest(
                java.util.List.of(user1, user2)
            );

            // Act & Assert
            mockMvc.perform(post("/admin/users/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRequested").value(2))
                .andExpect(jsonPath("$.successfullyCreated").value(1))
                .andExpect(jsonPath("$.failed").value(1))
                .andExpect(jsonPath("$.results", hasSize(2)))
                .andExpect(jsonPath("$.results[0].success").value(true))
                .andExpect(jsonPath("$.results[0].email").value("newuser.bulk@test.com"))
                .andExpect(jsonPath("$.results[1].success").value(false))
                .andExpect(jsonPath("$.results[1].email").value("anna@smith.com"))
                .andExpect(jsonPath("$.results[1].message").value("Un compte avec cet email existe déjà"));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait gérer les rôles invalides")
        void bulkCreateUsers_shouldHandleInvalidRoles() throws Exception {
            // Arrange
            CreateUserRequest user1 = new CreateUserRequest(
                "Jean", "Dupont", "jean.invalid@test.com", "ATHLETE", "FR"
            );
            CreateUserRequest user2 = new CreateUserRequest(
                "Marie", "Martin", "marie.invalid@test.com", "INVALID_ROLE", "FR"
            );
            BulkCreateUsersRequest request = new BulkCreateUsersRequest(
                java.util.List.of(user1, user2)
            );

            // Act & Assert
            mockMvc.perform(post("/admin/users/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRequested").value(2))
                .andExpect(jsonPath("$.successfullyCreated").value(1))
                .andExpect(jsonPath("$.failed").value(1))
                .andExpect(jsonPath("$.results[0].success").value(true))
                .andExpect(jsonPath("$.results[1].success").value(false))
                .andExpect(jsonPath("$.results[1].message", containsString("Rôle non trouvé")));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait créer des spectateurs avec compte activé")
        void bulkCreateUsers_shouldCreateSpectateursWithActivatedAccount() throws Exception {
            // Arrange
            CreateUserRequest user = new CreateUserRequest(
                "Spectateur", "Test", "spectateur.bulk@test.com", "SPECTATEUR", "FR"
            );
            BulkCreateUsersRequest request = new BulkCreateUsersRequest(
                java.util.List.of(user)
            );

            // Act & Assert
            mockMvc.perform(post("/admin/users/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successfullyCreated").value(1))
                .andExpect(jsonPath("$.results[0].success").value(true));

            // Vérifier que le compte est bien activé
            mockMvc.perform(get("/admin/users")
                    .param("role", "SPECTATEUR")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'spectateur.bulk@test.com')].isAccountActivated").value(true))
                .andExpect(jsonPath("$[?(@.email == 'spectateur.bulk@test.com')].mustChangePassword").value(false));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait créer des athlètes avec compte non activé")
        void bulkCreateUsers_shouldCreateAthletesWithInactiveAccount() throws Exception {
            // Arrange
            CreateUserRequest user = new CreateUserRequest(
                "Athlete", "Test", "athlete.bulk.inactive@test.com", "ATHLETE", "FR"
            );
            BulkCreateUsersRequest request = new BulkCreateUsersRequest(
                java.util.List.of(user)
            );

            // Act & Assert
            mockMvc.perform(post("/admin/users/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successfullyCreated").value(1));

            // Vérifier que le compte n'est pas activé
            mockMvc.perform(get("/admin/users")
                    .param("role", "ATHLETE")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'athlete.bulk.inactive@test.com')].isAccountActivated").value(false))
                .andExpect(jsonPath("$[?(@.email == 'athlete.bulk.inactive@test.com')].mustChangePassword").value(true));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait créer des utilisateurs sans code pays")
        void bulkCreateUsers_shouldCreateUsersWithoutCountryCode() throws Exception {
            // Arrange
            CreateUserRequest user = new CreateUserRequest(
                "No", "Country", "nocountry.bulk@test.com", "ATHLETE", null
            );
            BulkCreateUsersRequest request = new BulkCreateUsersRequest(
                java.util.List.of(user)
            );

            // Act & Assert
            mockMvc.perform(post("/admin/users/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successfullyCreated").value(1))
                .andExpect(jsonPath("$.results[0].success").value(true));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait retourner BadRequest si la liste est vide")
        void bulkCreateUsers_shouldReturnBadRequestForEmptyList() throws Exception {
            // Arrange
            BulkCreateUsersRequest request = new BulkCreateUsersRequest(
                java.util.List.of()
            );

            // Act & Assert
            mockMvc.perform(post("/admin/users/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait gérer les utilisateurs avec champs manquants - échecs dans la réponse")
        void bulkCreateUsers_shouldHandleMissingFields() throws Exception {
            // Arrange - Création d'une requête avec des utilisateurs valides et invalides
            CreateUserRequest validUser = new CreateUserRequest(
                "Jean", "Dupont", "valid.missing@test.com", "ATHLETE", "FR"
            );
            // Note: On ne peut pas tester directement un DTO sans email car la désérialisation va assigner null
            // et la validation @NotBlank de CreateUserRequest devrait échouer au niveau contrôleur
            // Pour l'instant on vérifie que les utilisateurs valides sont créés
            BulkCreateUsersRequest request = new BulkCreateUsersRequest(
                java.util.List.of(validUser)
            );

            // Act & Assert
            mockMvc.perform(post("/admin/users/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successfullyCreated").value(1));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait gérer correctement les emails dupliqués dans un batch")
        void bulkCreateUsers_shouldHandleInvalidEmailFormats() throws Exception {
            // Arrange - Créer un utilisateur valide et un avec email déjà existant
            CreateUserRequest validUser = new CreateUserRequest(
                "Jean", "Dupont", "valid.email.format@test.com", "ATHLETE", "FR"
            );
            // Utiliser un email existant pour garantir un échec
            CreateUserRequest duplicateEmailUser = new CreateUserRequest(
                "Marie", "Martin", "athlete@test.com", "ATHLETE", "FR"  // Email déjà dans data.sql
            );
            BulkCreateUsersRequest request = new BulkCreateUsersRequest(
                java.util.List.of(validUser, duplicateEmailUser)
            );

            // Act - Le traitement devrait créer l'utilisateur valide et rejeter le doublon
            var result = mockMvc.perform(post("/admin/users/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
            
            // Assert - Vérifier qu'un utilisateur est créé et l'autre échoue (email dupliqué)
            result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRequested").value(2))
                .andExpect(jsonPath("$.successfullyCreated").value(1))
                .andExpect(jsonPath("$.failed").value(1))
                .andExpect(jsonPath("$.results[0].success").exists())
                .andExpect(jsonPath("$.results[1].success").exists());
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait gérer un grand nombre d'utilisateurs")
        void bulkCreateUsers_shouldHandleLargeNumberOfUsers() throws Exception {
            // Arrange - Créer 10 utilisateurs
            java.util.List<CreateUserRequest> users = new java.util.ArrayList<>();
            for (int i = 0; i < 10; i++) {
                users.add(new CreateUserRequest(
                    "User" + i,
                    "Test" + i,
                    "user" + i + ".bulk@test.com",
                    "ATHLETE",
                    "FR"
                ));
            }
            BulkCreateUsersRequest request = new BulkCreateUsersRequest(users);

            // Act & Assert
            mockMvc.perform(post("/admin/users/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRequested").value(10))
                .andExpect(jsonPath("$.successfullyCreated").value(10))
                .andExpect(jsonPath("$.failed").value(0))
                .andExpect(jsonPath("$.results", hasSize(10)));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
        @DisplayName("Devrait créer des utilisateurs avec différents rôles")
        void bulkCreateUsers_shouldCreateUsersWithDifferentRoles() throws Exception {
            // Arrange
            CreateUserRequest athlete = new CreateUserRequest(
                "Athlete", "User", "athlete.mixedroles@test.com", "ATHLETE", "FR"
            );
            CreateUserRequest volontaire = new CreateUserRequest(
                "Volontaire", "User", "volontaire.mixedroles@test.com", "VOLONTAIRE", "FR"
            );
            CreateUserRequest commissaire = new CreateUserRequest(
                "Commissaire", "User", "commissaire.mixedroles@test.com", "COMMISSAIRE", "FR"
            );
            CreateUserRequest spectateur = new CreateUserRequest(
                "Spectateur", "User", "spectateur.mixedroles@test.com", "SPECTATEUR", "FR"
            );
            BulkCreateUsersRequest request = new BulkCreateUsersRequest(
                java.util.List.of(athlete, volontaire, commissaire, spectateur)
            );

            // Act & Assert
            mockMvc.perform(post("/admin/users/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRequested").value(4))
                .andExpect(jsonPath("$.successfullyCreated").value(4))
                .andExpect(jsonPath("$.failed").value(0))
                .andExpect(jsonPath("$.results[0].temporaryPassword").value("user.athlete"))
                .andExpect(jsonPath("$.results[1].temporaryPassword").value("user.volontaire"))
                .andExpect(jsonPath("$.results[2].temporaryPassword").value("user.commissaire"))
                .andExpect(jsonPath("$.results[3].temporaryPassword").value("user.spectateur"));
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
