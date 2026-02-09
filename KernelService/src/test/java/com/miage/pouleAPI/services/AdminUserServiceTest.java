package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.admin.*;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires AdminUserService - Gestion complète des utilisateurs")
class AdminUserServiceTest {

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
    private Role adminRole;
    private Role spectateurRole;
    private Country france;
    private ApplicationUser testUser;

    @BeforeEach
    void setUp() {
        athleteRole = new Role();
        athleteRole.setRoleName("ATHLETE");

        adminRole = new Role();
        adminRole.setRoleName("ADMIN");

        spectateurRole = new Role();
        spectateurRole.setRoleName("SPECTATEUR");

        france = new Country();
        france.setCode("FR");

        testUser = createTestUser(1, "John", "Doe", "john.doe@test.com", athleteRole, france);
    }

    private ApplicationUser createTestUser(Integer id, String name, String lastname, String email, Role role, Country country) {
        ApplicationUser user = new ApplicationUser();
        user.setId(id);
        user.setName(name);
        user.setLastname(lastname);
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setRole(role);
        user.setCountry(country);
        user.setIsActive(true);
        user.setIsAccountActivated(true);
        user.setMustChangePassword(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy("admin@test.com");
        return user;
    }

    @Nested
    @DisplayName("Tests createUser")
    class CreateUserTests {

        @Test
        @DisplayName("Devrait créer un utilisateur avec succès")
        void createUser_shouldCreateUserSuccessfully() {
            CreateUserRequest request = new CreateUserRequest(
                "Jane", "Smith", "jane.smith@test.com", "ATHLETE", "FR"
            );

            when(userRepository.existsByEmail("jane.smith@test.com")).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> i.getArguments()[0]);

            CreateUserResponse response = adminUserService.createUser(request, "admin@test.com");

            assertThat(response.name()).isEqualTo("Jane");
            assertThat(response.lastname()).isEqualTo("Smith");
            assertThat(response.email()).isEqualTo("jane.smith@test.com");
            assertThat(response.temporaryPassword()).isEqualTo("smith.jane");
            assertThat(response.message()).isEqualTo("Compte créé avec succès");
        }

        @Test
        @DisplayName("Devrait retourner erreur si email existe déjà")
        void createUser_shouldReturnErrorIfEmailExists() {
            CreateUserRequest request = new CreateUserRequest(
                "John", "Doe", "existing@test.com", "ATHLETE", "FR"
            );

            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            CreateUserResponse response = adminUserService.createUser(request, "admin@test.com");

            assertThat(response.id()).isNull();
            assertThat(response.message()).isEqualTo("Un compte avec cet email existe déjà");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait lever une exception si rôle non trouvé")
        void createUser_shouldThrowIfRoleNotFound() {
            CreateUserRequest request = new CreateUserRequest(
                "John", "Doe", "john@test.com", "INVALID_ROLE", "FR"
            );

            when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
            when(roleRepository.findById("INVALID_ROLE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.createUser(request, "admin@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rôle non trouvé");
        }

        @Test
        @DisplayName("Devrait créer un utilisateur sans pays")
        void createUser_shouldCreateUserWithoutCountry() {
            CreateUserRequest request = new CreateUserRequest(
                "Jane", "Smith", "jane@test.com", "ATHLETE", null
            );

            when(userRepository.existsByEmail("jane@test.com")).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> i.getArguments()[0]);

            CreateUserResponse response = adminUserService.createUser(request, "admin@test.com");

            assertThat(response.email()).isEqualTo("jane@test.com");
            verify(countryRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("Devrait créer un utilisateur avec pays vide")
        void createUser_shouldCreateUserWithEmptyCountry() {
            CreateUserRequest request = new CreateUserRequest(
                "Jane", "Smith", "jane@test.com", "ATHLETE", ""
            );

            when(userRepository.existsByEmail("jane@test.com")).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> i.getArguments()[0]);

            CreateUserResponse response = adminUserService.createUser(request, "admin@test.com");

            assertThat(response.message()).isEqualTo("Compte créé avec succès");
        }
    }

    @Nested
    @DisplayName("Tests getAllUsers")
    class GetAllUsersTests {

        @Test
        @DisplayName("Devrait retourner tous les utilisateurs")
        void getAllUsers_shouldReturnAllUsers() {
            ApplicationUser user2 = createTestUser(2, "Jane", "Smith", "jane@test.com", spectateurRole, france);
            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

            List<UserDto> users = adminUserService.getAllUsers();

            assertThat(users).hasSize(2);
            assertThat(users.get(0).name()).isEqualTo("John");
            assertThat(users.get(1).name()).isEqualTo("Jane");
        }

        @Test
        @DisplayName("Devrait retourner une liste vide si aucun utilisateur")
        void getAllUsers_shouldReturnEmptyListIfNoUsers() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserDto> users = adminUserService.getAllUsers();

            assertThat(users).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests getUsersByRole")
    class GetUsersByRoleTests {

        @Test
        @DisplayName("Devrait retourner les utilisateurs par rôle")
        void getUsersByRole_shouldReturnUsersByRole() {
            ApplicationUser user2 = createTestUser(2, "Jane", "Smith", "jane@test.com", athleteRole, france);
            ApplicationUser user3 = createTestUser(3, "Bob", "Brown", "bob@test.com", spectateurRole, france);
            
            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2, user3));

            List<UserDto> athletes = adminUserService.getUsersByRole("ATHLETE");

            assertThat(athletes).hasSize(2);
            assertThat(athletes).allMatch(u -> "ATHLETE".equals(u.roleName()));
        }

        @Test
        @DisplayName("Devrait retourner liste vide si aucun utilisateur avec ce rôle")
        void getUsersByRole_shouldReturnEmptyIfNoUsersWithRole() {
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            List<UserDto> admins = adminUserService.getUsersByRole("COMMISSAIRE");

            assertThat(admins).isEmpty();
        }

        @Test
        @DisplayName("Devrait gérer les utilisateurs sans rôle")
        void getUsersByRole_shouldHandleUsersWithoutRole() {
            ApplicationUser userWithoutRole = createTestUser(2, "No", "Role", "norole@test.com", null, france);
            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, userWithoutRole));

            List<UserDto> athletes = adminUserService.getUsersByRole("ATHLETE");

            assertThat(athletes).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Tests getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("Devrait retourner l'utilisateur par ID")
        void getUserById_shouldReturnUser() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

            UserDto user = adminUserService.getUserById(1);

            assertThat(user.id()).isEqualTo(1);
            assertThat(user.email()).isEqualTo("john.doe@test.com");
        }

        @Test
        @DisplayName("Devrait lever une exception si utilisateur non trouvé")
        void getUserById_shouldThrowIfNotFound() {
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.getUserById(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
        }
    }

    @Nested
    @DisplayName("Tests updateUser")
    class UpdateUserTests {

        @Test
        @DisplayName("Devrait mettre à jour le nom")
        void updateUser_shouldUpdateName() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            UpdateUserRequest request = new UpdateUserRequest("NewName", null, null, null, null);
            UserDto updated = adminUserService.updateUser(1, request);

            assertThat(updated.name()).isEqualTo("NewName");
        }

        @Test
        @DisplayName("Devrait mettre à jour le nom de famille")
        void updateUser_shouldUpdateLastname() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            UpdateUserRequest request = new UpdateUserRequest(null, "NewLastname", null, null, null);
            UserDto updated = adminUserService.updateUser(1, request);

            assertThat(updated.lastname()).isEqualTo("NewLastname");
        }

        @Test
        @DisplayName("Devrait mettre à jour l'email")
        void updateUser_shouldUpdateEmail() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
            when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            UpdateUserRequest request = new UpdateUserRequest(null, null, "new@test.com", null, null);
            UserDto updated = adminUserService.updateUser(1, request);

            assertThat(updated.email()).isEqualTo("new@test.com");
        }

        @Test
        @DisplayName("Devrait lever exception si email déjà utilisé")
        void updateUser_shouldThrowIfEmailAlreadyUsed() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("used@test.com")).thenReturn(true);

            UpdateUserRequest request = new UpdateUserRequest(null, null, "used@test.com", null, null);

            assertThatThrownBy(() -> adminUserService.updateUser(1, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cet email est déjà utilisé");
        }

        @Test
        @DisplayName("Devrait mettre à jour le rôle")
        void updateUser_shouldUpdateRole() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(roleRepository.findById("SPECTATEUR")).thenReturn(Optional.of(spectateurRole));
            when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            UpdateUserRequest request = new UpdateUserRequest(null, null, null, "SPECTATEUR", null);
            UserDto updated = adminUserService.updateUser(1, request);

            assertThat(updated.roleName()).isEqualTo("SPECTATEUR");
        }

        @Test
        @DisplayName("Devrait lever exception si rôle non trouvé")
        void updateUser_shouldThrowIfRoleNotFound() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(roleRepository.findById("INVALID")).thenReturn(Optional.empty());

            UpdateUserRequest request = new UpdateUserRequest(null, null, null, "INVALID", null);

            assertThatThrownBy(() -> adminUserService.updateUser(1, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rôle non trouvé");
        }

        @Test
        @DisplayName("Devrait mettre à jour le pays")
        void updateUser_shouldUpdateCountry() {
            Country germany = new Country();
            germany.setCode("DE");
            
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(countryRepository.findById("DE")).thenReturn(Optional.of(germany));
            when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, "DE");
            UserDto updated = adminUserService.updateUser(1, request);

            assertThat(updated.countryCode()).isEqualTo("DE");
        }

        @Test
        @DisplayName("Devrait gérer pays inexistant en le mettant à null")
        void updateUser_shouldSetCountryToNullIfNotFound() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(countryRepository.findById("XX")).thenReturn(Optional.empty());
            when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, "XX");
            UserDto updated = adminUserService.updateUser(1, request);

            assertThat(updated.countryCode()).isNull();
        }

        @Test
        @DisplayName("Devrait lever exception si utilisateur non trouvé")
        void updateUser_shouldThrowIfUserNotFound() {
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            UpdateUserRequest request = new UpdateUserRequest("Name", null, null, null, null);

            assertThatThrownBy(() -> adminUserService.updateUser(999, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
        }

        @Test
        @DisplayName("Ne devrait pas modifier si les champs sont vides")
        void updateUser_shouldNotUpdateBlankFields() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            UpdateUserRequest request = new UpdateUserRequest("", "", "", "", null);
            UserDto updated = adminUserService.updateUser(1, request);

            assertThat(updated.name()).isEqualTo("John");
            assertThat(updated.lastname()).isEqualTo("Doe");
            assertThat(updated.email()).isEqualTo("john.doe@test.com");
        }

        @Test
        @DisplayName("Devrait permettre mise à jour email vers le même email")
        void updateUser_shouldAllowSameEmail() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            UpdateUserRequest request = new UpdateUserRequest(null, null, "john.doe@test.com", null, null);
            UserDto updated = adminUserService.updateUser(1, request);

            assertThat(updated.email()).isEqualTo("john.doe@test.com");
        }
    }

    @Nested
    @DisplayName("Tests deactivateUser")
    class DeactivateUserTests {

        @Test
        @DisplayName("Devrait désactiver un utilisateur")
        void deactivateUser_shouldDeactivateUser() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            UserDto deactivated = adminUserService.deactivateUser(1, "Violation des règles");

            assertThat(deactivated.isActive()).isFalse();
            assertThat(deactivated.deactivationReason()).isEqualTo("Violation des règles");
        }

        @Test
        @DisplayName("Ne devrait pas désactiver un admin")
        void deactivateUser_shouldNotDeactivateAdmin() {
            ApplicationUser adminUser = createTestUser(1, "Admin", "User", "admin@test.com", adminRole, france);
            when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));

            assertThatThrownBy(() -> adminUserService.deactivateUser(1, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Impossible de désactiver un compte administrateur");
        }

        @Test
        @DisplayName("Devrait lever exception si utilisateur non trouvé")
        void deactivateUser_shouldThrowIfNotFound() {
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.deactivateUser(999, "Reason"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
        }

        @Test
        @DisplayName("Devrait désactiver un utilisateur sans rôle")
        void deactivateUser_shouldDeactivateUserWithoutRole() {
            ApplicationUser userNoRole = createTestUser(1, "No", "Role", "norole@test.com", null, france);
            when(userRepository.findById(1)).thenReturn(Optional.of(userNoRole));
            when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            UserDto deactivated = adminUserService.deactivateUser(1, "Reason");

            assertThat(deactivated.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Tests reactivateUser")
    class ReactivateUserTests {

        @Test
        @DisplayName("Devrait réactiver un utilisateur")
        void reactivateUser_shouldReactivateUser() {
            testUser.setIsActive(false);
            testUser.setDeactivatedAt(LocalDateTime.now());
            testUser.setDeactivationReason("Ancienne raison");
            
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            UserDto reactivated = adminUserService.reactivateUser(1);

            assertThat(reactivated.isActive()).isTrue();
            assertThat(reactivated.deactivationReason()).isNull();
            assertThat(reactivated.deactivatedAt()).isNull();
        }

        @Test
        @DisplayName("Devrait lever exception si utilisateur non trouvé")
        void reactivateUser_shouldThrowIfNotFound() {
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.reactivateUser(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
        }
    }

    @Nested
    @DisplayName("Tests activateAccount")
    class ActivateAccountTests {

        @Test
        @DisplayName("Devrait activer le compte avec nouveau mot de passe")
        void activateAccount_shouldActivateWithNewPassword() {
            testUser.setIsAccountActivated(false);
            testUser.setMustChangePassword(true);
            
            when(userRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
            when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            adminUserService.activateAccount("john.doe@test.com", "newPassword123");

            ArgumentCaptor<ApplicationUser> captor = ArgumentCaptor.forClass(ApplicationUser.class);
            verify(userRepository).save(captor.capture());
            
            ApplicationUser saved = captor.getValue();
            assertThat(saved.getIsAccountActivated()).isTrue();
            assertThat(saved.getMustChangePassword()).isFalse();
            assertThat(saved.getPassword()).isEqualTo("encodedNewPassword");
        }

        @Test
        @DisplayName("Devrait lever exception si utilisateur non trouvé")
        void activateAccount_shouldThrowIfUserNotFound() {
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.activateAccount("unknown@test.com", "password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
        }

        @Test
        @DisplayName("Devrait lever exception si compte déjà activé")
        void activateAccount_shouldThrowIfAlreadyActivated() {
            testUser.setIsAccountActivated(true);
            when(userRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> adminUserService.activateAccount("john.doe@test.com", "password"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ce compte est déjà activé");
        }
    }

    @Nested
    @DisplayName("Tests resetPassword")
    class ResetPasswordTests {

        @Test
        @DisplayName("Devrait réinitialiser le mot de passe")
        void resetPassword_shouldResetPassword() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("doe.john")).thenReturn("encodedTempPassword");
            when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            String tempPassword = adminUserService.resetPassword(1);

            assertThat(tempPassword).isEqualTo("doe.john");
            
            ArgumentCaptor<ApplicationUser> captor = ArgumentCaptor.forClass(ApplicationUser.class);
            verify(userRepository).save(captor.capture());
            
            ApplicationUser saved = captor.getValue();
            assertThat(saved.getMustChangePassword()).isTrue();
            assertThat(saved.getIsAccountActivated()).isFalse();
        }

        @Test
        @DisplayName("Devrait lever exception si utilisateur non trouvé")
        void resetPassword_shouldThrowIfNotFound() {
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.resetPassword(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
        }
    }

    @Nested
    @DisplayName("Tests toUserDto (mapping)")
    class ToUserDtoTests {

        @Test
        @DisplayName("Devrait mapper correctement un utilisateur complet")
        void toUserDto_shouldMapCompleteUser() {
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

            UserDto dto = adminUserService.getUserById(1);

            assertThat(dto.id()).isEqualTo(testUser.getId());
            assertThat(dto.name()).isEqualTo(testUser.getName());
            assertThat(dto.lastname()).isEqualTo(testUser.getLastname());
            assertThat(dto.email()).isEqualTo(testUser.getEmail());
            assertThat(dto.roleName()).isEqualTo("ATHLETE");
            assertThat(dto.countryCode()).isEqualTo("FR");
            assertThat(dto.isActive()).isTrue();
        }

        @Test
        @DisplayName("Devrait mapper utilisateur sans rôle")
        void toUserDto_shouldMapUserWithoutRole() {
            ApplicationUser userNoRole = createTestUser(2, "No", "Role", "norole@test.com", null, france);
            when(userRepository.findById(2)).thenReturn(Optional.of(userNoRole));

            UserDto dto = adminUserService.getUserById(2);

            assertThat(dto.roleName()).isNull();
        }

        @Test
        @DisplayName("Devrait mapper utilisateur sans pays")
        void toUserDto_shouldMapUserWithoutCountry() {
            ApplicationUser userNoCountry = createTestUser(3, "No", "Country", "nocountry@test.com", athleteRole, null);
            when(userRepository.findById(3)).thenReturn(Optional.of(userNoCountry));

            UserDto dto = adminUserService.getUserById(3);

            assertThat(dto.countryCode()).isNull();
        }
    }

    @Nested
    @DisplayName("Tests bulkCreateUsers")
    class BulkCreateUsersTests {

        @Test
        @DisplayName("Devrait créer plusieurs utilisateurs avec succès")
        void bulkCreateUsers_shouldCreateMultipleUsersSuccessfully() {
            // Arrange
            CreateUserRequest request1 = new CreateUserRequest(
                "Jean", "Dupont", "jean.dupont@test.com", "ATHLETE", "FR"
            );
            CreateUserRequest request2 = new CreateUserRequest(
                "Marie", "Martin", "marie.martin@test.com", "VOLONTAIRE", "FR"
            );
            BulkCreateUsersRequest bulkRequest = new BulkCreateUsersRequest(
                Arrays.asList(request1, request2)
            );

            Role volontaireRole = new Role();
            volontaireRole.setRoleName("VOLONTAIRE");

            when(userRepository.existsByEmail("jean.dupont@test.com")).thenReturn(false);
            when(userRepository.existsByEmail("marie.martin@test.com")).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(roleRepository.findById("VOLONTAIRE")).thenReturn(Optional.of(volontaireRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(invocation -> {
                ApplicationUser user = invocation.getArgument(0);
                user.setId((int) (Math.random() * 1000));
                return user;
            });

            // Act
            BulkCreateUsersResponse response = adminUserService.bulkCreateUsers(bulkRequest, "admin@test.com");

            // Assert
            assertThat(response.totalRequested()).isEqualTo(2);
            assertThat(response.successfullyCreated()).isEqualTo(2);
            assertThat(response.failed()).isEqualTo(0);
            assertThat(response.results()).hasSize(2);

            BulkCreateUsersResponse.UserCreationResult result1 = response.results().get(0);
            assertThat(result1.email()).isEqualTo("jean.dupont@test.com");
            assertThat(result1.success()).isTrue();
            assertThat(result1.temporaryPassword()).isEqualTo("dupont.jean");

            BulkCreateUsersResponse.UserCreationResult result2 = response.results().get(1);
            assertThat(result2.email()).isEqualTo("marie.martin@test.com");
            assertThat(result2.success()).isTrue();
            assertThat(result2.temporaryPassword()).isEqualTo("martin.marie");

            verify(userRepository, times(2)).save(any(ApplicationUser.class));
        }

        @Test
        @DisplayName("Devrait gérer les emails déjà existants")
        void bulkCreateUsers_shouldHandleExistingEmails() {
            // Arrange
            CreateUserRequest request1 = new CreateUserRequest(
                "Jean", "Dupont", "jean.dupont@test.com", "ATHLETE", "FR"
            );
            CreateUserRequest request2 = new CreateUserRequest(
                "Marie", "Martin", "existing@test.com", "VOLONTAIRE", "FR"
            );
            BulkCreateUsersRequest bulkRequest = new BulkCreateUsersRequest(
                Arrays.asList(request1, request2)
            );

            when(userRepository.existsByEmail("jean.dupont@test.com")).thenReturn(false);
            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(invocation -> {
                ApplicationUser user = invocation.getArgument(0);
                user.setId((int) (Math.random() * 1000));
                return user;
            });

            // Act
            BulkCreateUsersResponse response = adminUserService.bulkCreateUsers(bulkRequest, "admin@test.com");

            // Assert
            assertThat(response.totalRequested()).isEqualTo(2);
            assertThat(response.successfullyCreated()).isEqualTo(1);
            assertThat(response.failed()).isEqualTo(1);

            BulkCreateUsersResponse.UserCreationResult successResult = response.results().get(0);
            assertThat(successResult.email()).isEqualTo("jean.dupont@test.com");
            assertThat(successResult.success()).isTrue();

            BulkCreateUsersResponse.UserCreationResult failedResult = response.results().get(1);
            assertThat(failedResult.email()).isEqualTo("existing@test.com");
            assertThat(failedResult.success()).isFalse();
            assertThat(failedResult.message()).isEqualTo("Un compte avec cet email existe déjà");

            verify(userRepository, times(1)).save(any(ApplicationUser.class));
        }

        @Test
        @DisplayName("Devrait gérer les rôles invalides")
        void bulkCreateUsers_shouldHandleInvalidRoles() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                "Jean", "Dupont", "jean.dupont@test.com", "INVALID_ROLE", "FR"
            );
            BulkCreateUsersRequest bulkRequest = new BulkCreateUsersRequest(
                List.of(request)
            );

            when(userRepository.existsByEmail("jean.dupont@test.com")).thenReturn(false);
            when(roleRepository.findById("INVALID_ROLE")).thenReturn(Optional.empty());

            // Act
            BulkCreateUsersResponse response = adminUserService.bulkCreateUsers(bulkRequest, "admin@test.com");

            // Assert
            assertThat(response.totalRequested()).isEqualTo(1);
            assertThat(response.successfullyCreated()).isEqualTo(0);
            assertThat(response.failed()).isEqualTo(1);

            BulkCreateUsersResponse.UserCreationResult result = response.results().get(0);
            assertThat(result.email()).isEqualTo("jean.dupont@test.com");
            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Rôle non trouvé");

            verify(userRepository, never()).save(any(ApplicationUser.class));
        }

        @Test
        @DisplayName("Devrait créer des spectateurs avec compte activé")
        void bulkCreateUsers_shouldCreateSpectateursWithActivatedAccount() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                "Jean", "Dupont", "jean.dupont@test.com", "SPECTATEUR", "FR"
            );
            BulkCreateUsersRequest bulkRequest = new BulkCreateUsersRequest(
                List.of(request)
            );

            when(userRepository.existsByEmail("jean.dupont@test.com")).thenReturn(false);
            when(roleRepository.findById("SPECTATEUR")).thenReturn(Optional.of(spectateurRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

            ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
                ApplicationUser user = invocation.getArgument(0);
                user.setId(1);
                return user;
            });

            // Act
            BulkCreateUsersResponse response = adminUserService.bulkCreateUsers(bulkRequest, "admin@test.com");

            // Assert
            assertThat(response.successfullyCreated()).isEqualTo(1);

            ApplicationUser savedUser = userCaptor.getValue();
            assertThat(savedUser.getIsAccountActivated()).isTrue();
            assertThat(savedUser.getMustChangePassword()).isFalse();
        }

        @Test
        @DisplayName("Devrait créer des non-spectateurs avec compte non activé")
        void bulkCreateUsers_shouldCreateNonSpectateursWithInactiveAccount() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                "Jean", "Dupont", "jean.dupont@test.com", "ATHLETE", "FR"
            );
            BulkCreateUsersRequest bulkRequest = new BulkCreateUsersRequest(
                List.of(request)
            );

            when(userRepository.existsByEmail("jean.dupont@test.com")).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

            ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
                ApplicationUser user = invocation.getArgument(0);
                user.setId(1);
                return user;
            });

            // Act
            BulkCreateUsersResponse response = adminUserService.bulkCreateUsers(bulkRequest, "admin@test.com");

            // Assert
            assertThat(response.successfullyCreated()).isEqualTo(1);

            ApplicationUser savedUser = userCaptor.getValue();
            assertThat(savedUser.getIsAccountActivated()).isFalse();
            assertThat(savedUser.getMustChangePassword()).isTrue();
        }

        @Test
        @DisplayName("Devrait créer des utilisateurs sans code pays")
        void bulkCreateUsers_shouldCreateUsersWithoutCountryCode() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                "Jean", "Dupont", "jean.dupont@test.com", "ATHLETE", null
            );
            BulkCreateUsersRequest bulkRequest = new BulkCreateUsersRequest(
                List.of(request)
            );

            when(userRepository.existsByEmail("jean.dupont@test.com")).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

            ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
                ApplicationUser user = invocation.getArgument(0);
                user.setId(1);
                return user;
            });

            // Act
            BulkCreateUsersResponse response = adminUserService.bulkCreateUsers(bulkRequest, "admin@test.com");

            // Assert
            assertThat(response.successfullyCreated()).isEqualTo(1);

            ApplicationUser savedUser = userCaptor.getValue();
            assertThat(savedUser.getCountry()).isNull();
        }

        @Test
        @DisplayName("Devrait gérer un mélange de succès et d'échecs")
        void bulkCreateUsers_shouldHandleMixOfSuccessAndFailures() {
            // Arrange
            CreateUserRequest request1 = new CreateUserRequest(
                "Jean", "Dupont", "jean.dupont@test.com", "ATHLETE", "FR"
            );
            CreateUserRequest request2 = new CreateUserRequest(
                "Marie", "Martin", "existing@test.com", "VOLONTAIRE", "FR"
            );
            CreateUserRequest request3 = new CreateUserRequest(
                "Pierre", "Bernard", "pierre.bernard@test.com", "INVALID_ROLE", "FR"
            );
            CreateUserRequest request4 = new CreateUserRequest(
                "Sophie", "Dubois", "sophie.dubois@test.com", "COMMISSAIRE", "FR"
            );
            BulkCreateUsersRequest bulkRequest = new BulkCreateUsersRequest(
                Arrays.asList(request1, request2, request3, request4)
            );

            Role commissaireRole = new Role();
            commissaireRole.setRoleName("COMMISSAIRE");

            when(userRepository.existsByEmail("jean.dupont@test.com")).thenReturn(false);
            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);
            when(userRepository.existsByEmail("pierre.bernard@test.com")).thenReturn(false);
            when(userRepository.existsByEmail("sophie.dubois@test.com")).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(roleRepository.findById("INVALID_ROLE")).thenReturn(Optional.empty());
            when(roleRepository.findById("COMMISSAIRE")).thenReturn(Optional.of(commissaireRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(invocation -> {
                ApplicationUser user = invocation.getArgument(0);
                user.setId((int) (Math.random() * 1000));
                return user;
            });

            // Act
            BulkCreateUsersResponse response = adminUserService.bulkCreateUsers(bulkRequest, "admin@test.com");

            // Assert
            assertThat(response.totalRequested()).isEqualTo(4);
            assertThat(response.successfullyCreated()).isEqualTo(2);
            assertThat(response.failed()).isEqualTo(2);
            assertThat(response.results()).hasSize(4);

            // Vérifier les succès
            List<BulkCreateUsersResponse.UserCreationResult> successResults = response.results().stream()
                .filter(BulkCreateUsersResponse.UserCreationResult::success)
                .toList();
            assertThat(successResults).hasSize(2);
            assertThat(successResults).extracting(BulkCreateUsersResponse.UserCreationResult::email)
                .containsExactlyInAnyOrder("jean.dupont@test.com", "sophie.dubois@test.com");

            // Vérifier les échecs
            List<BulkCreateUsersResponse.UserCreationResult> failedResults = response.results().stream()
                .filter(r -> !r.success())
                .toList();
            assertThat(failedResults).hasSize(2);
            assertThat(failedResults).extracting(BulkCreateUsersResponse.UserCreationResult::email)
                .containsExactlyInAnyOrder("existing@test.com", "pierre.bernard@test.com");

            verify(userRepository, times(2)).save(any(ApplicationUser.class));
        }
    }
}
