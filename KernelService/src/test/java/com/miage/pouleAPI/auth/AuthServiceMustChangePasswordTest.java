package com.miage.pouleAPI.auth;

import com.miage.pouleAPI.auth.dto.LoginRequest;
import com.miage.pouleAPI.auth.jwt.JwtService;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Role;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires AuthService - Fonctionnalité MustChangePassword")
class AuthServiceMustChangePasswordTest {

    @Mock
    private ApplicationUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private ApplicationUser createTestUser(Boolean mustChangePassword, Boolean isAccountActivated, Boolean isActive) {
        ApplicationUser user = new ApplicationUser();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setMustChangePassword(mustChangePassword);
        user.setIsAccountActivated(isAccountActivated);
        user.setIsActive(isActive);
        
        Role role = new Role();
        role.setRoleName("ATHLETE");
        user.setRole(role);
        
        return user;
    }

    @Nested
    @DisplayName("Tests loginWithStatus")
    class LoginWithStatusTests {

        @Test
        @DisplayName("Devrait retourner mustChangePassword=true pour un nouvel utilisateur")
        void loginWithStatus_shouldReturnMustChangePasswordTrue_forNewUser() {
            // Arrange
            ApplicationUser user = createTestUser(true, false, true);
            LoginRequest request = new LoginRequest("test@example.com", "password");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
            when(jwtService.generateToken(anyInt(), anyString(), anyString())).thenReturn("test-jwt-token");

            // Act
            AuthService.LoginResponseWithStatus response = authService.loginWithStatus(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("test-jwt-token");
            assertThat(response.mustChangePassword()).isTrue();
            assertThat(response.isAccountActivated()).isFalse();
        }

        @Test
        @DisplayName("Devrait retourner mustChangePassword=false pour un utilisateur existant")
        void loginWithStatus_shouldReturnMustChangePasswordFalse_forExistingUser() {
            // Arrange
            ApplicationUser user = createTestUser(false, true, true);
            LoginRequest request = new LoginRequest("test@example.com", "password");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
            when(jwtService.generateToken(anyInt(), anyString(), anyString())).thenReturn("test-jwt-token");

            // Act
            AuthService.LoginResponseWithStatus response = authService.loginWithStatus(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("test-jwt-token");
            assertThat(response.mustChangePassword()).isFalse();
            assertThat(response.isAccountActivated()).isTrue();
        }

        @Test
        @DisplayName("Devrait gérer mustChangePassword null comme false")
        void loginWithStatus_shouldHandleNullMustChangePassword() {
            // Arrange
            ApplicationUser user = createTestUser(null, true, true);
            LoginRequest request = new LoginRequest("test@example.com", "password");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
            when(jwtService.generateToken(anyInt(), anyString(), anyString())).thenReturn("test-jwt-token");

            // Act
            AuthService.LoginResponseWithStatus response = authService.loginWithStatus(request);

            // Assert
            assertThat(response.mustChangePassword()).isFalse();
        }

        @Test
        @DisplayName("Devrait échouer si le compte est désactivé")
        void loginWithStatus_shouldFail_forDeactivatedAccount() {
            // Arrange
            ApplicationUser user = createTestUser(false, true, false);
            LoginRequest request = new LoginRequest("test@example.com", "password");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.loginWithStatus(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Account is deactivated");
        }

        @Test
        @DisplayName("Devrait échouer si le mot de passe est incorrect")
        void loginWithStatus_shouldFail_forWrongPassword() {
            // Arrange
            ApplicationUser user = createTestUser(true, false, true);
            LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.loginWithStatus(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");
        }

        @Test
        @DisplayName("Devrait échouer si l'utilisateur n'existe pas")
        void loginWithStatus_shouldFail_forNonExistentUser() {
            // Arrange
            LoginRequest request = new LoginRequest("nonexistent@example.com", "password");

            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.loginWithStatus(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("User not found");
        }
    }
}
