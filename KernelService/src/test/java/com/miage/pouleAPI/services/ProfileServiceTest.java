package com.miage.pouleAPI.services;

import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Role;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ApplicationUserRepository userRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void signCharter_ShouldUpdateUser_WhenUserIsAthlete() {
        String email = "sportif@test.com";
        Role athleteRole = new Role("ATHLETE");
        ApplicationUser user = new ApplicationUser();
        user.setEmail(email);
        user.setRole(athleteRole);
        user.setHasSignedCharter(false);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        profileService.signCharter();

        assertTrue(user.getHasSignedCharter());
        verify(userRepository).save(user);
    }
}