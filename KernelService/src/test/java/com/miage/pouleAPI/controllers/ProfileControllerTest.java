package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.auth.AuthService;
import com.miage.pouleAPI.auth.repository.ApplicationUserRepository;
import com.miage.pouleAPI.dtos.profile.ApplicationUserProfileDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock private AuthService authService;
    @Mock private ApplicationUserRepository userRepo;
    @Mock private Authentication authentication;

    @InjectMocks
    private ProfileController profileController;

    private ApplicationUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new ApplicationUser();
        mockUser.setId(1);
        mockUser.setEmail("test@miage.fr");
    }

    @Test
    void getProfile_ShouldReturnUser() {
        when(authentication.getName()).thenReturn("test@miage.fr");
        when(userRepo.findByEmail("test@miage.fr")).thenReturn(Optional.of(mockUser));

        ResponseEntity<ApplicationUser> response = profileController.getProfile(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("test@miage.fr", response.getBody().getEmail());
    }

    @Test
    void updateProfile_ShouldCallServiceWithCorrectArgs() {
        ApplicationUserProfileDTO dto = new ApplicationUserProfileDTO();
        dto.setEmail("new@mail.com");
        dto.setName("Jean");
        dto.setLastname("Poule");

        when(authentication.getName()).thenReturn("test@miage.fr");
        when(userRepo.findByEmail("test@miage.fr")).thenReturn(Optional.of(mockUser));

        ResponseEntity<String> response = profileController.updateProfile(dto, authentication);

        verify(authService).updateProfile(1, "new@mail.com", "Jean", "Poule", null);
        assertEquals("Updated", response.getBody());
    }

    @Test
    void changePassword_ShouldCallServiceWithCorrectArgs() {
        ApplicationUserProfileDTO dto = new ApplicationUserProfileDTO();
        dto.setCurrentPassword("oldPass");
        dto.setNewPassword("newPass");

        when(authentication.getName()).thenReturn("test@miage.fr");
        when(userRepo.findByEmail("test@miage.fr")).thenReturn(Optional.of(mockUser));

        ResponseEntity<String> response = profileController.changePassword(dto, authentication);

        verify(authService).changePassword(1, "oldPass", "newPass");
        assertEquals("Password changed", response.getBody());
    }
}