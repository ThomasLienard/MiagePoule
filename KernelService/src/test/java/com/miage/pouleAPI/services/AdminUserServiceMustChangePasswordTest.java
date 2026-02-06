package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.admin.CreateUserRequest;
import com.miage.pouleAPI.dtos.admin.CreateUserResponse;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Country;
import com.miage.pouleAPI.entity.Role;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.CountryRepository;
import com.miage.pouleAPI.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires AdminUserService - Fonctionnalité MustChangePassword")
class AdminUserServiceMustChangePasswordTest {

    @Mock
    private ApplicationUserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserService adminUserService;

    private Role athleteRole;
    private Country france;

    @BeforeEach
    void setUp() {
        athleteRole = new Role();
        athleteRole.setRoleName("ATHLETE");

        france = new Country();
        france.setCode("FR");
    }

    @Nested
    @DisplayName("Tests création utilisateur avec mustChangePassword")
    class CreateUserTests {

        @Test
        @DisplayName("Devrait créer un utilisateur non-spectateur avec mustChangePassword=true")
        void createUser_shouldSetMustChangePasswordTrue_forNonSpectateur() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                "John", "Doe", "john.doe@example.com", "ATHLETE", "FR"
            );

            when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            adminUserService.createUser(request, "admin@example.com");

            // Assert
            ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
            verify(userRepository).save(userCaptor.capture());
            
            ApplicationUser savedUser = userCaptor.getValue();
            assertThat(savedUser.getMustChangePassword()).isTrue();
            assertThat(savedUser.getIsAccountActivated()).isFalse();
            assertThat(savedUser.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Devrait créer un spectateur automatiquement validé")
        void createUser_shouldAutoValidateSpectateur() {
            // Arrange
            Role spectateurRole = new Role();
            spectateurRole.setRoleName("SPECTATEUR");
            
            CreateUserRequest request = new CreateUserRequest(
                "Thomas", "Lienard", "thomas@gmail.com", "SPECTATEUR", "FR"
            );

            when(userRepository.existsByEmail("thomas@gmail.com")).thenReturn(false);
            when(roleRepository.findById("SPECTATEUR")).thenReturn(Optional.of(spectateurRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            adminUserService.createUser(request, "admin@example.com");

            // Assert
            ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
            verify(userRepository).save(userCaptor.capture());
            
            ApplicationUser savedUser = userCaptor.getValue();
            assertThat(savedUser.getMustChangePassword()).isFalse();
            assertThat(savedUser.getIsAccountActivated()).isTrue();
            assertThat(savedUser.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Devrait créer un utilisateur non-spectateur avec isAccountActivated=false")
        void createUser_shouldSetIsAccountActivatedFalse_forNonSpectateur() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                "Jane", "Smith", "jane.smith@example.com", "VOLONTAIRE", "FR"
            );

            Role volontaireRole = new Role();
            volontaireRole.setRoleName("VOLONTAIRE");

            when(userRepository.existsByEmail("jane.smith@example.com")).thenReturn(false);
            when(roleRepository.findById("VOLONTAIRE")).thenReturn(Optional.of(volontaireRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            adminUserService.createUser(request, "admin@example.com");

            // Assert
            ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
            verify(userRepository).save(userCaptor.capture());
            
            ApplicationUser savedUser = userCaptor.getValue();
            assertThat(savedUser.getIsAccountActivated()).isFalse();
        }

        @Test
        @DisplayName("Devrait générer un mot de passe temporaire nom.prenom")
        void createUser_shouldGenerateTemporaryPassword() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                "Pierre", "Dupont", "pierre.dupont@example.com", "ATHLETE", null
            );

            when(userRepository.existsByEmail("pierre.dupont@example.com")).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(passwordEncoder.encode("dupont.pierre")).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            CreateUserResponse response = adminUserService.createUser(request, "admin@example.com");

            // Assert
            assertThat(response.temporaryPassword()).isEqualTo("dupont.pierre");
            verify(passwordEncoder).encode("dupont.pierre");
        }
    }

    @Nested
    @DisplayName("Tests activation de compte")
    class ActivateAccountTests {

        @Test
        @DisplayName("Devrait mettre mustChangePassword à false après activation")
        void activateAccount_shouldSetMustChangePasswordFalse() {
            // Arrange
            ApplicationUser user = new ApplicationUser();
            user.setId(1);
            user.setEmail("test@example.com");
            user.setMustChangePassword(true);
            user.setIsAccountActivated(false);
            user.setRole(athleteRole);

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            adminUserService.activateAccount("test@example.com", "newPassword123");

            // Assert
            ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
            verify(userRepository).save(userCaptor.capture());
            
            ApplicationUser savedUser = userCaptor.getValue();
            assertThat(savedUser.getMustChangePassword()).isFalse();
            assertThat(savedUser.getIsAccountActivated()).isTrue();
        }

        @Test
        @DisplayName("Devrait encoder le nouveau mot de passe")
        void activateAccount_shouldEncodeNewPassword() {
            // Arrange
            ApplicationUser user = new ApplicationUser();
            user.setId(1);
            user.setEmail("test@example.com");
            user.setMustChangePassword(true);
            user.setIsAccountActivated(false);
            user.setRole(athleteRole);

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("securePassword")).thenReturn("encodedSecurePassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            adminUserService.activateAccount("test@example.com", "securePassword");

            // Assert
            verify(passwordEncoder).encode("securePassword");
            ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("encodedSecurePassword");
        }
    }

    @Nested
    @DisplayName("Tests réinitialisation de mot de passe")
    class ResetPasswordTests {

        @Test
        @DisplayName("Devrait mettre mustChangePassword à true après reset")
        void resetPassword_shouldSetMustChangePasswordTrue() {
            // Arrange
            ApplicationUser user = new ApplicationUser();
            user.setId(1);
            user.setName("John");
            user.setLastname("Doe");
            user.setEmail("john.doe@example.com");
            user.setMustChangePassword(false);
            user.setIsAccountActivated(true);
            user.setIsActive(true);
            user.setRole(athleteRole);

            when(userRepository.findById(1)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("doe.john")).thenReturn("encodedTempPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            adminUserService.resetPassword(1);

            // Assert
            ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
            verify(userRepository).save(userCaptor.capture());
            
            ApplicationUser savedUser = userCaptor.getValue();
            assertThat(savedUser.getMustChangePassword()).isTrue();
            assertThat(savedUser.getIsAccountActivated()).isFalse();
        }
    }
}
