package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.dtos.admin.*;
import com.miage.pouleAPI.services.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires AdminUserController")
class AdminUserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AdminUserController adminUserController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminUserController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private UserDto createUserDto(Integer id, String name, String lastname, String email, String role) {
        return new UserDto(
            id, name, lastname, email, role, "FR",
            true, true, false, false,
            LocalDateTime.now(), "admin@test.com",
            null, null
        );
    }

    @Nested
    @DisplayName("Tests GET /admin/users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Devrait retourner tous les utilisateurs")
        void getAllUsers_shouldReturnAllUsers() throws Exception {
            List<UserDto> users = Arrays.asList(
                createUserDto(1, "John", "Doe", "john@test.com", "ATHLETE"),
                createUserDto(2, "Jane", "Smith", "jane@test.com", "SPECTATEUR")
            );
            when(adminUserService.getAllUsers()).thenReturn(users);

            mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[1].name").value("Jane"));
        }

        @Test
        @DisplayName("Devrait filtrer par rôle")
        void getAllUsers_shouldFilterByRole() throws Exception {
            List<UserDto> athletes = List.of(
                createUserDto(1, "John", "Doe", "john@test.com", "ATHLETE")
            );
            when(adminUserService.getUsersByRole("ATHLETE")).thenReturn(athletes);

            mockMvc.perform(get("/admin/users").param("role", "ATHLETE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].roleName").value("ATHLETE"));
        }

        @Test
        @DisplayName("Devrait ignorer rôle vide et retourner tous")
        void getAllUsers_shouldIgnoreBlankRole() throws Exception {
            List<UserDto> users = Arrays.asList(
                createUserDto(1, "John", "Doe", "john@test.com", "ATHLETE"),
                createUserDto(2, "Jane", "Smith", "jane@test.com", "SPECTATEUR")
            );
            when(adminUserService.getAllUsers()).thenReturn(users);

            mockMvc.perform(get("/admin/users").param("role", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("Tests GET /admin/users/{id}")
    class GetUserByIdTests {

        @Test
        @DisplayName("Devrait retourner l'utilisateur par ID")
        void getUserById_shouldReturnUser() throws Exception {
            UserDto user = createUserDto(1, "John", "Doe", "john@test.com", "ATHLETE");
            when(adminUserService.getUserById(1)).thenReturn(user);

            mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"));
        }

        @Test
        @DisplayName("Devrait retourner 404 si utilisateur non trouvé")
        void getUserById_shouldReturn404IfNotFound() throws Exception {
            when(adminUserService.getUserById(999))
                .thenThrow(new IllegalArgumentException("Utilisateur non trouvé"));

            mockMvc.perform(get("/admin/users/999"))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Tests PUT /admin/users/{id}")
    class UpdateUserTests {

        @Test
        @DisplayName("Devrait mettre à jour l'utilisateur")
        void updateUser_shouldUpdateUser() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest("NewName", "NewLastname", null, null, null);
            UserDto updated = createUserDto(1, "NewName", "NewLastname", "john@test.com", "ATHLETE");
            
            when(adminUserService.updateUser(eq(1), any(UpdateUserRequest.class))).thenReturn(updated);

            mockMvc.perform(put("/admin/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"))
                .andExpect(jsonPath("$.lastname").value("NewLastname"));
        }

        @Test
        @DisplayName("Devrait retourner 400 si erreur de mise à jour")
        void updateUser_shouldReturn400OnError() throws Exception {
            UpdateUserRequest request = new UpdateUserRequest(null, null, "used@test.com", null, null);
            
            when(adminUserService.updateUser(eq(1), any(UpdateUserRequest.class)))
                .thenThrow(new IllegalArgumentException("Cet email est déjà utilisé"));

            mockMvc.perform(put("/admin/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cet email est déjà utilisé"));
        }
    }

    @Nested
    @DisplayName("Tests POST /admin/users/{id}/deactivate")
    class DeactivateUserTests {

        @Test
        @DisplayName("Devrait désactiver l'utilisateur")
        void deactivateUser_shouldDeactivate() throws Exception {
            DeactivateUserRequest request = new DeactivateUserRequest("Violation des règles");
            UserDto deactivated = new UserDto(
                1, "John", "Doe", "john@test.com", "ATHLETE", "FR",
                false, true, false, false,
                LocalDateTime.now(), "admin@test.com",
                LocalDateTime.now(), "Violation des règles"
            );
            
            when(adminUserService.deactivateUser(1, "Violation des règles")).thenReturn(deactivated);

            mockMvc.perform(post("/admin/users/1/deactivate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.deactivationReason").value("Violation des règles"));
        }

        @Test
        @DisplayName("Devrait retourner 400 si désactivation admin")
        void deactivateUser_shouldReturn400ForAdmin() throws Exception {
            DeactivateUserRequest request = new DeactivateUserRequest("Test");
            
            when(adminUserService.deactivateUser(1, "Test"))
                .thenThrow(new IllegalArgumentException("Impossible de désactiver un compte administrateur"));

            mockMvc.perform(post("/admin/users/1/deactivate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Impossible de désactiver un compte administrateur"));
        }
    }

    @Nested
    @DisplayName("Tests POST /admin/users/{id}/reactivate")
    class ReactivateUserTests {

        @Test
        @DisplayName("Devrait réactiver l'utilisateur")
        void reactivateUser_shouldReactivate() throws Exception {
            UserDto reactivated = createUserDto(1, "John", "Doe", "john@test.com", "ATHLETE");
            when(adminUserService.reactivateUser(1)).thenReturn(reactivated);

            mockMvc.perform(post("/admin/users/1/reactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
        }

        @Test
        @DisplayName("Devrait retourner 400 si utilisateur non trouvé")
        void reactivateUser_shouldReturn400IfNotFound() throws Exception {
            when(adminUserService.reactivateUser(999))
                .thenThrow(new IllegalArgumentException("Utilisateur non trouvé"));

            mockMvc.perform(post("/admin/users/999/reactivate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Utilisateur non trouvé"));
        }
    }

    @Nested
    @DisplayName("Tests POST /admin/users/{id}/reset-password")
    class ResetPasswordTests {

        @Test
        @DisplayName("Devrait réinitialiser le mot de passe")
        void resetPassword_shouldResetPassword() throws Exception {
            when(adminUserService.resetPassword(1)).thenReturn("doe.john");

            mockMvc.perform(post("/admin/users/1/reset-password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Mot de passe réinitialisé"))
                .andExpect(jsonPath("$.temporaryPassword").value("doe.john"));
        }

        @Test
        @DisplayName("Devrait retourner 400 si utilisateur non trouvé")
        void resetPassword_shouldReturn400IfNotFound() throws Exception {
            when(adminUserService.resetPassword(999))
                .thenThrow(new IllegalArgumentException("Utilisateur non trouvé"));

            mockMvc.perform(post("/admin/users/999/reset-password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Utilisateur non trouvé"));
        }
    }

    @Nested
    @DisplayName("Tests POST /admin/users/{id}/validate-account")
    class ValidateAccountTests {

        @Test
        @DisplayName("Devrait valider le compte utilisateur")
        void validateAccount_shouldValidateAccount() throws Exception {
            UserDto validated = new UserDto(
                1, "John", "Doe", "john@test.com", "ATHLETE", "FR",
                true, true, true, false,
                LocalDateTime.now(), "admin@test.com",
                null, null
            );
            when(adminUserService.validateUserAccount(1)).thenReturn(validated);

            mockMvc.perform(post("/admin/users/1/validate-account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAccountValidated").value(true));
        }

        @Test
        @DisplayName("Devrait retourner 400 si compte déjà validé")
        void validateAccount_shouldReturn400IfAlreadyValidated() throws Exception {
            when(adminUserService.validateUserAccount(1))
                .thenThrow(new IllegalStateException("Ce compte est déjà validé"));

            mockMvc.perform(post("/admin/users/1/validate-account"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ce compte est déjà validé"));
        }

        @Test
        @DisplayName("Devrait retourner 400 si utilisateur non trouvé")
        void validateAccount_shouldReturn400IfNotFound() throws Exception {
            when(adminUserService.validateUserAccount(999))
                .thenThrow(new IllegalArgumentException("Utilisateur non trouvé avec l'ID: 999"));

            mockMvc.perform(post("/admin/users/999/validate-account"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests POST /admin/users/{id}/invalidate-account")
    class InvalidateAccountTests {

        @Test
        @DisplayName("Devrait invalider le compte utilisateur")
        void invalidateAccount_shouldInvalidateAccount() throws Exception {
            UserDto invalidated = new UserDto(
                1, "John", "Doe", "john@test.com", "ATHLETE", "FR",
                true, true, false, false,
                LocalDateTime.now(), "admin@test.com",
                null, null
            );
            when(adminUserService.invalidateUserAccount(1)).thenReturn(invalidated);

            mockMvc.perform(post("/admin/users/1/invalidate-account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAccountValidated").value(false));
        }

        @Test
        @DisplayName("Devrait retourner 400 si compte déjà invalidé")
        void invalidateAccount_shouldReturn400IfAlreadyInvalidated() throws Exception {
            when(adminUserService.invalidateUserAccount(1))
                .thenThrow(new IllegalStateException("Ce compte est déjà invalidé"));

            mockMvc.perform(post("/admin/users/1/invalidate-account"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ce compte est déjà invalidé"));
        }

        @Test
        @DisplayName("Devrait retourner 400 si utilisateur non trouvé")
        void invalidateAccount_shouldReturn400IfNotFound() throws Exception {
            when(adminUserService.invalidateUserAccount(999))
                .thenThrow(new IllegalArgumentException("Utilisateur non trouvé avec l'ID: 999"));

            mockMvc.perform(post("/admin/users/999/invalidate-account"))
                .andExpect(status().isBadRequest());
        }
    }
}
