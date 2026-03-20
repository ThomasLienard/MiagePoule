package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.adapters.UserAdapter;
import com.miage.pouleAPI.auth.AuthService;
import com.miage.pouleAPI.dtos.profile.UpdateProfileRequestDTO;
import com.miage.pouleAPI.dtos.profile.UpdateProfileResponse;
import com.miage.pouleAPI.dtos.profile.UserProfileResponseDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
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
    @Mock private UserAdapter userAdapter;
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
    void getProfile_ShouldReturnDto() {
        UserProfileResponseDTO expectedDto = new UserProfileResponseDTO();
        expectedDto.setEmail("test@miage.fr");

        when(authentication.getName()).thenReturn("test@miage.fr");
        when(userRepo.findByEmail("test@miage.fr")).thenReturn(Optional.of(mockUser));
        when(userAdapter.toResponseDTO(mockUser)).thenReturn(expectedDto);

        ResponseEntity<UserProfileResponseDTO> response = profileController.getProfile(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("test@miage.fr", response.getBody().getEmail());
    }

    @Test
    void updateProfile_ShouldCallServiceAndReturnNewToken() {
        UpdateProfileRequestDTO requestDto = new UpdateProfileRequestDTO();
        requestDto.setEmail("new@mail.com");

        UserProfileResponseDTO userResponseDto = new UserProfileResponseDTO();
        userResponseDto.setEmail("new@mail.com");

        UpdateProfileResponse serviceResponse = new UpdateProfileResponse(userResponseDto, "new.jwt.token");

        when(authentication.getName()).thenReturn("test@miage.fr");
        when(userRepo.findByEmail("test@miage.fr")).thenReturn(Optional.of(mockUser));
        when(authService.updateProfile(eq(1), any(UpdateProfileRequestDTO.class))).thenReturn(serviceResponse);

        ResponseEntity<UpdateProfileResponse> response = profileController.updateProfile(requestDto, authentication);

        verify(authService).updateProfile(eq(1), any(UpdateProfileRequestDTO.class));

        assertNotNull(response.getBody());
        assertEquals(200, response.getStatusCode().value());
        assertEquals("new@mail.com", response.getBody().user().getEmail());
        assertEquals("new.jwt.token", response.getBody().token());
    }
}