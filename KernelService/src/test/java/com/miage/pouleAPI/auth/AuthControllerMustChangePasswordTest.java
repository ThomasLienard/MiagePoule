package com.miage.pouleAPI.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.auth.dto.LoginRequest;
import com.miage.pouleAPI.auth.dto.LoginResponseWithStatus;
import com.miage.pouleAPI.services.AdminUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(classes = {AuthController.class, TestSecurityConfig.class})
@DisplayName("Tests Controller Auth - Fonctionnalité MustChangePassword")
class AuthControllerMustChangePasswordTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AdminUserService adminUserService;

    @Nested
    @DisplayName("Tests POST /auth/login avec mustChangePassword")
    class LoginWithMustChangePasswordTests {

        @Test
        @DisplayName("Devrait retourner mustChangePassword=true dans la réponse")
        void login_shouldReturnMustChangePasswordTrue() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("newuser@example.com", "doe.john");
            LoginResponseWithStatus response =
                new LoginResponseWithStatus("jwt-token", true, false, true);

            when(authService.loginWithStatus(any(LoginRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.mustChangePassword").value(true))
                .andExpect(jsonPath("$.isAccountActivated").value(false));
        }

        @Test
        @DisplayName("Devrait retourner mustChangePassword=false pour utilisateur existant")
        void login_shouldReturnMustChangePasswordFalse() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("existing@example.com", "password123");
            LoginResponseWithStatus response =
                new LoginResponseWithStatus("jwt-token", false, true, true);

            when(authService.loginWithStatus(any(LoginRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.mustChangePassword").value(false))
                .andExpect(jsonPath("$.isAccountActivated").value(true));
        }

        @Test
        @DisplayName("Devrait retourner les 3 champs token, mustChangePassword et isAccountActivated")
        void login_shouldReturnAllRequiredFields() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("user@example.com", "password");
            LoginResponseWithStatus response =
                new LoginResponseWithStatus("some-token", true, true, true);

            when(authService.loginWithStatus(any(LoginRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.mustChangePassword").exists())
                .andExpect(jsonPath("$.isAccountActivated").exists());
        }
    }
}
