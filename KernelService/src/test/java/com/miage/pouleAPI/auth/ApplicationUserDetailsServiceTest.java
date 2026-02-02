package com.miage.pouleAPI.auth;

import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Role;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationUserDetailsServiceTest {

    @Mock
    private ApplicationUserRepository userRepository;

    @InjectMocks
    private ApplicationUserDetailsService userDetailsService;

    @Test
    void testLoadUserByUsername_Success() {
        // Arrange
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        Role role = new Role();
        role.setRoleName("ATHLETE");
        user.setRole(role);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ATHLETE")));
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nonexistent@example.com")
        );

        assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void testLoadUserByUsername_WithAdminRole() {
        // Arrange
        ApplicationUser user = new ApplicationUser();
        user.setEmail("admin@example.com");
        user.setPassword("encodedAdminPassword");

        Role role = new Role();
        role.setRoleName("ADMIN");
        user.setRole(role);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("admin@example.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsername_WithCommissaireRole() {
        // Arrange
        ApplicationUser user = new ApplicationUser();
        user.setEmail("commissaire@example.com");
        user.setPassword("encodedCommissairePassword");

        Role role = new Role();
        role.setRoleName("COMMISSAIRE");
        user.setRole(role);

        when(userRepository.findByEmail("commissaire@example.com")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("commissaire@example.com");

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_COMMISSAIRE")));
    }

    @Test
    void testLoadUserByUsername_WithVolontaireRole() {
        // Arrange
        ApplicationUser user = new ApplicationUser();
        user.setEmail("volontaire@example.com");
        user.setPassword("encodedVolontairePassword");

        Role role = new Role();
        role.setRoleName("VOLONTAIRE");
        user.setRole(role);

        when(userRepository.findByEmail("volontaire@example.com")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("volontaire@example.com");

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_VOLONTAIRE")));
    }
}