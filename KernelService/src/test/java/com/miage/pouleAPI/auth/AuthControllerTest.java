package com.miage.pouleAPI.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.auth.dto.LoginRequest;
import com.miage.pouleAPI.auth.dto.SignUpRequest;
import com.miage.pouleAPI.auth.dto.SignUpResponse;
import com.miage.pouleAPI.services.AdminUserService;
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
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AdminUserService adminUserService;

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        AuthService.LoginResponseWithStatus response = 
            new AuthService.LoginResponseWithStatus("jwt-token-123", false, true, true);

        when(authService.loginWithStatus(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"));
    }

    @Test
    void testLogin_EmptyRequest() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("", "");
        AuthService.LoginResponseWithStatus response = 
            new AuthService.LoginResponseWithStatus("token", false, false, false);

        when(authService.loginWithStatus(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testSignUp_Success() throws Exception {
        // Arrange
        SignUpRequest request = new SignUpRequest(
                "newuser@example.com",
                "Password123!",
                "John",
                "Doe",
                "FR",
                "ATHLETE"
        );

        SignUpResponse response = new SignUpResponse(
                "jwt-token-123",
                "newuser@example.com",
                "John",
                "Doe",
                "ATHLETE",
                "User registered successfully"
        );

        when(authService.signUp(any(SignUpRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"))
                .andExpect(jsonPath("$.role").value("ATHLETE"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void testSignUp_EmailAlreadyExists() throws Exception {
        // Arrange
        SignUpRequest request = new SignUpRequest(
                "existing@example.com",
                "Password123!",
                "John",
                "Doe",
                "FR",
                "ATHLETE"
        );

        SignUpResponse response = new SignUpResponse(
                null,
                "existing@example.com",
                null,
                null,
                null,
                "Email already exists"
        );

        when(authService.signUp(any(SignUpRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("existing@example.com"))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }
}