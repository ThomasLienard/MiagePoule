package com.miage.pouleAPI.auth;

import com.miage.pouleAPI.adapters.UserAdapter;
import com.miage.pouleAPI.auth.dto.LoginRequest;
import com.miage.pouleAPI.auth.dto.SignUpRequest;
import com.miage.pouleAPI.auth.dto.SignUpResponse;
import com.miage.pouleAPI.auth.jwt.JwtService;
import com.miage.pouleAPI.dtos.profile.UpdateProfileRequestDTO;
import com.miage.pouleAPI.dtos.profile.UpdateProfileResponse;
import com.miage.pouleAPI.dtos.profile.UserProfileResponseDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Country;
import com.miage.pouleAPI.entity.Role;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
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
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword123");
        when(userRepo.save(any(ApplicationUser.class))).thenAnswer(invocation -> {
            ApplicationUser user = invocation.getArgument(0);
            user.setId(1); // Simuler l'auto-génération de l'ID
            return user;
        });
        when(jwtService.generateToken(1, "newuser@example.com", "ATHLETE"))
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
        verify(passwordEncoder, times(1)).encode("Password123!");
        verify(jwtService, times(1)).generateToken(anyInt(), eq("newuser@example.com"), eq("ATHLETE"));
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
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepo.save(any(ApplicationUser.class))).thenAnswer(invocation -> {
            ApplicationUser user = invocation.getArgument(0);
            user.setId(1); // Simuler l'auto-génération de l'ID
            return user;
        });
        when(jwtService.generateToken(1, "firstuser@example.com", "ATHLETE"))
                .thenReturn("first-jwt-token");

        // Act
        SignUpResponse response = authService.signUp(request);

        // Assert
        assertNotNull(response);
        assertEquals("first-jwt-token", response.token());
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
    void updateProfile_ShouldUpdateNameEmailAndCountry_AndReturnNewToken() {
        Integer userId = 1;

        Role userRole = new Role();
        userRole.setRoleName("USER");

        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setId(userId);
        existingUser.setName("AncienNom");
        existingUser.setEmail("ancien@mail.com");
        existingUser.setRole(userRole);

        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        dto.setName("NouveauNom");
        dto.setEmail("nouveau@mail.com");
        dto.setCountryCode("FR");

        Country mockCountry = new Country();
        mockCountry.setCode("FR");

        UserProfileResponseDTO mockResponseDTO = new UserProfileResponseDTO();
        mockResponseDTO.setEmail("nouveau@mail.com");

        when(userRepo.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepo.findByEmail("nouveau@mail.com")).thenReturn(Optional.empty()); // Email libre
        when(countryRepository.findById("FR")).thenReturn(Optional.of(mockCountry));
        when(jwtService.generateToken(any(), any(), any())).thenReturn("nouveau.jwt.token");
        when(userAdapter.toResponseDTO(any(ApplicationUser.class))).thenReturn(mockResponseDTO);
        when(userRepo.save(any(ApplicationUser.class))).thenAnswer(i -> i.getArguments()[0]);

        UpdateProfileResponse response = authService.updateProfile(userId, dto);

        verify(userAdapter).updateEntityFromDto(dto, existingUser);

        assertEquals("nouveau@mail.com", existingUser.getEmail());

        assertEquals("FR", existingUser.getCountry().getCode());

        assertNotNull(response);
        assertEquals("nouveau.jwt.token", response.token());
        assertEquals("nouveau@mail.com", response.user().getEmail());

        verify(userRepo).save(existingUser);
        verify(jwtService).generateToken(eq(userId), eq("nouveau@mail.com"), eq("USER"));
    }

    @Test
    void updateProfile_ShouldThrowException_WhenEmailAlreadyExists() {
        Integer userId = 1;
        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setId(userId);
        existingUser.setEmail("old@test.com");

        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        dto.setEmail("taken@test.com");

        when(userRepo.findById(userId)).thenReturn(Optional.of(existingUser));
        // On simule qu'un AUTRE utilisateur possède déjà cet email
        when(userRepo.findByEmail("taken@test.com")).thenReturn(Optional.of(new ApplicationUser()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            authService.updateProfile(userId, dto);
        });

        assertEquals("Email incorrect", ex.getMessage());
    }

    @Test
    void updateProfile_ShouldReturnNewToken_WhenEmailIsUpdated() {
        Integer userId = 1;
        ApplicationUser user = new ApplicationUser();
        user.setId(userId);
        user.setEmail("old@test.com");
        user.setRole(new Role("USER"));

        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        dto.setEmail("new@test.com");

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(userRepo.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(jwtService.generateToken(any(), any(), any())).thenReturn("new.jwt.token");
        when(userAdapter.toResponseDTO(any())).thenReturn(new UserProfileResponseDTO());

        UpdateProfileResponse response = authService.updateProfile(userId, dto);

        assertEquals("new.jwt.token", response.token());
        verify(jwtService).generateToken(eq(userId), eq("new@test.com"), any());
    }

    @Test
    void updateProfile_ShouldThrowException_WhenCountryNotFound() {
        Integer userId = 1;
        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        dto.setCountryCode("INVALID");

        when(userRepo.findById(userId)).thenReturn(Optional.of(new ApplicationUser()));
        when(countryRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            authService.updateProfile(userId, dto);
        });
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