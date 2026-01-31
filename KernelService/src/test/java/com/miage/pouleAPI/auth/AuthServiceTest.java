package com.miage.pouleAPI.auth;

import com.miage.pouleAPI.adapters.UserAdapter;
import com.miage.pouleAPI.auth.dto.LoginRequest;
import com.miage.pouleAPI.auth.dto.SignUpRequest;
import com.miage.pouleAPI.auth.dto.SignUpResponse;
import com.miage.pouleAPI.auth.jwt.JwtService;
import com.miage.pouleAPI.auth.repository.ApplicationUserRepository;
import com.miage.pouleAPI.dtos.profile.UpdateProfileRequestDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Country;
import com.miage.pouleAPI.entity.Role;
import com.miage.pouleAPI.repositories.CountryRepository;
import com.miage.pouleAPI.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ApplicationUserRepository userRepo;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserAdapter userAdapter;

    @InjectMocks
    private AuthService authService;

    private ApplicationUser testUser;
    private Role testRole;
    private Country testCountry;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setRoleName("ATHLETE");

        testCountry = new Country();
        testCountry.setCode("FR");

        testUser = new ApplicationUser();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setName("John");
        testUser.setLastname("Doe");
        testUser.setRole(testRole);
        testUser.setCountry(testCountry);
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(1, "test@example.com", "ATHLETE"))
                .thenReturn("jwt-token");

        // Act
        String token = authService.login(request);

        // Assert
        assertNotNull(token);
        assertEquals("jwt-token", token);
        verify(userRepo, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(jwtService, times(1)).generateToken(1, "test@example.com", "ATHLETE");
    }

    @Test
    void testLogin_UserNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");
        when(userRepo.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(userRepo, times(1)).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyInt(), anyString(), anyString());
    }

    @Test
    void testLogin_WrongPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(userRepo, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("wrongpassword", "encodedPassword");
        verify(jwtService, never()).generateToken(anyInt(), anyString(), anyString());
    }

    @Test
    void testSignUp_Success() {
        // Arrange
        SignUpRequest request = new SignUpRequest(
                "newuser@example.com",
                "Password123!",
                "Jane",
                "Smith",
                "FR",
                "ATHLETE"
        );

        when(userRepo.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(testRole));
        when(countryRepository.findById("FR")).thenReturn(Optional.of(testCountry));
        when(userRepo.findMaxId()).thenReturn(100);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword123");
        when(jwtService.generateToken(101, "newuser@example.com", "ATHLETE"))
                .thenReturn("new-jwt-token");

        // Act
        SignUpResponse response = authService.signUp(request);

        // Assert
        assertNotNull(response);
        assertEquals("new-jwt-token", response.token());
        assertEquals("newuser@example.com", response.email());
        assertEquals("Jane", response.name());
        assertEquals("Smith", response.lastname());
        assertEquals("ATHLETE", response.role());
        assertEquals("User registered successfully", response.message());

        verify(userRepo, times(1)).save(any(ApplicationUser.class));
        verify(userRepo, times(1)).findMaxId();
        verify(passwordEncoder, times(1)).encode("Password123!");
        verify(jwtService, times(1)).generateToken(101, "newuser@example.com", "ATHLETE");
    }

    @Test
    void testSignUp_EmailAlreadyExists() {
        // Arrange
        SignUpRequest request = new SignUpRequest(
                "existing@example.com",
                "Password123!",
                "Jane",
                "Smith",
                "FR",
                "ATHLETE"
        );

        when(userRepo.findByEmail("existing@example.com")).thenReturn(Optional.of(testUser));

        // Act
        SignUpResponse response = authService.signUp(request);

        // Assert
        assertNotNull(response);
        assertNull(response.token());
        assertEquals("existing@example.com", response.email());
        assertEquals("Email already exists", response.message());

        verify(userRepo, never()).save(any(ApplicationUser.class));
        verify(roleRepository, never()).findById(anyString());
        verify(countryRepository, never()).findById(anyString());
        verify(jwtService, never()).generateToken(anyInt(), anyString(), anyString());
    }

    @Test
    void testSignUp_RoleNotFound() {
        // Arrange
        SignUpRequest request = new SignUpRequest(
                "newuser@example.com",
                "Password123!",
                "Jane",
                "Smith",
                "FR",
                "INVALID_ROLE"
        );

        when(userRepo.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findById("INVALID_ROLE")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.signUp(request));
        verify(userRepo, never()).save(any(ApplicationUser.class));
        verify(countryRepository, never()).findById(anyString());
        verify(jwtService, never()).generateToken(anyInt(), anyString(), anyString());
    }

    @Test
    void testSignUp_CountryNotFound() {
        // Arrange
        SignUpRequest request = new SignUpRequest(
                "newuser@example.com",
                "Password123!",
                "Jane",
                "Smith",
                "XX", // Pays invalide
                "ATHLETE"
        );

        when(userRepo.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(testRole));
        when(countryRepository.findById("XX")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.signUp(request));
        verify(userRepo, never()).save(any(ApplicationUser.class));
        verify(jwtService, never()).generateToken(anyInt(), anyString(), anyString());
    }

    @Test
    void testSignUp_FirstUser() {
        // Arrange
        SignUpRequest request = new SignUpRequest(
                "firstuser@example.com",
                "Password123!",
                "First",
                "User",
                "FR",
                "ATHLETE"
        );

        when(userRepo.findByEmail("firstuser@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(testRole));
        when(countryRepository.findById("FR")).thenReturn(Optional.of(testCountry));
        when(userRepo.findMaxId()).thenReturn(null); // Aucun utilisateur existant
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(jwtService.generateToken(1, "firstuser@example.com", "ATHLETE"))
                .thenReturn("first-jwt-token");

        // Act
        SignUpResponse response = authService.signUp(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.token().startsWith("first-jwt-token") ? 1 : 0);
        assertEquals("firstuser@example.com", response.email());
        assertEquals("First", response.name());
        assertEquals("User", response.lastname());

        verify(userRepo, times(1)).save(any(ApplicationUser.class));
        verify(jwtService, times(1)).generateToken(1, "firstuser@example.com", "ATHLETE");
    }

    @Test
    void testUpdateProfile_WithMapStruct() {
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        dto.setName("Nouveau");
        when(userRepo.findById(1)).thenReturn(Optional.of(testUser));

        authService.updateProfile(1, dto);

        // On vérifie que le mapper a bien été appelé pour fusionner les données
        verify(userAdapter).updateEntityFromDto(eq(dto), any(ApplicationUser.class));
        verify(userRepo).save(any(ApplicationUser.class));
    }

    @Test
    void testChangePassword_Success() {
        Integer userId = 1;
        String currentPassword = "current";
        String newPassword = "newPass";

        when(userRepo.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNew");

        authService.changePassword(userId, currentPassword, newPassword);

        assertEquals("encodedNew", testUser.getPassword());
        verify(userRepo).save(testUser);
    }

    @Test
    void testChangePassword_InvalidCurrent_ThrowsException() {
        Integer userId = 1;
        String currentPassword = "wrong";
        String newPassword = "newPass";

        when(userRepo.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.changePassword(userId, currentPassword, newPassword));

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepo, never()).save(any(ApplicationUser.class));
    }
}