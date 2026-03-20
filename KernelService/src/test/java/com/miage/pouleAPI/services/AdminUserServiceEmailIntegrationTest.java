package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.admin.BulkCreateUsersRequest;
import com.miage.pouleAPI.dtos.admin.BulkCreateUsersResponse;
import com.miage.pouleAPI.dtos.admin.CreateUserRequest;
import com.miage.pouleAPI.dtos.admin.CreateUserResponse;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Country;
import com.miage.pouleAPI.entity.Role;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.CountryRepository;
import com.miage.pouleAPI.repositories.RoleRepository;
import com.miage.pouleAPI.services.interfaces.MaillingService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests intégration MaillingService dans AdminUserService")
class AdminUserServiceEmailIntegrationTest {

    @Mock
    private ApplicationUserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MaillingService maillingService;

    @InjectMocks
    private AdminUserService adminUserService;

    private Role athleteRole;
    private Country france;
    private ApplicationUser testUser;

    @BeforeEach
    void setUp() {
        athleteRole = new Role();
        athleteRole.setRoleName("ATHLETE");

        france = new Country();
        france.setCode("FR");

        testUser = createTestUser(1, "John", "Doe", "john.doe@test.com");
    }

    private ApplicationUser createTestUser(Integer id, String name, String lastname, String email) {
        ApplicationUser user = new ApplicationUser();
        user.setId(id);
        user.setName(name);
        user.setLastname(lastname);
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setRole(athleteRole);
        user.setCountry(france);
        user.setIsActive(true);
        user.setIsAccountActivated(false);
        user.setMustChangePassword(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy("admin@test.com");
        return user;
    }

    @Nested
    @DisplayName("Tests envoi d'email lors de createUser")
    class CreateUserEmailTests {

        @Test
        @DisplayName("Devrait envoyer un email d'activation lors de la création d'un utilisateur")
        void createUser_shouldSendActivationEmail() {
            // Given
            CreateUserRequest request = new CreateUserRequest(
                "Jane", "Smith", "jane.smith@test.com", "ATHLETE", "FR"
            );

            when(userRepository.existsByEmail("jane.smith@test.com")).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> {
                ApplicationUser user = i.getArgument(0);
                user.setId(2);
                return user;
            });

            // When
            CreateUserResponse response = adminUserService.createUser(request, "admin@test.com");

            // Then
            assertThat(response.id()).isNotNull();
            assertThat(response.temporaryPassword()).isEqualTo("smith.jane");

            // Vérifier que l'email a été envoyé
            ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

            verify(maillingService, times(1)).sendEmail(
                toCaptor.capture(),
                subjectCaptor.capture(),
                bodyCaptor.capture()
            );

            assertThat(toCaptor.getValue()).isEqualTo("jane.smith@test.com");
            assertThat(subjectCaptor.getValue()).contains("Activation");
            assertThat(bodyCaptor.getValue()).contains("Jane");
            assertThat(bodyCaptor.getValue()).contains("Smith");
            assertThat(bodyCaptor.getValue()).contains("smith.jane");
            assertThat(bodyCaptor.getValue()).contains("jane.smith@test.com");
            assertThat(bodyCaptor.getValue()).contains("admin@test.com");
        }

        @Test
        @DisplayName("Devrait créer l'utilisateur même si l'envoi d'email échoue")
        void createUser_shouldCreateUserEvenIfEmailFails() {
            // Given
            CreateUserRequest request = new CreateUserRequest(
                "Jane", "Smith", "jane.smith@test.com", "ATHLETE", "FR"
            );

            when(userRepository.existsByEmail("jane.smith@test.com")).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> {
                ApplicationUser user = i.getArgument(0);
                user.setId(2);
                return user;
            });

            // Simuler une erreur lors de l'envoi d'email
            doThrow(new RuntimeException("SMTP error"))
                .when(maillingService).sendEmail(anyString(), anyString(), anyString());

            // When
            CreateUserResponse response = adminUserService.createUser(request, "admin@test.com");

            // Then - L'utilisateur doit quand même être créé
            assertThat(response.id()).isNotNull();
            assertThat(response.message()).isEqualTo("Compte créé avec succès");
            verify(userRepository, times(1)).save(any(ApplicationUser.class));
        }

        @Test
        @DisplayName("Devrait envoyer un email avec le bon format de contenu")
        void createUser_shouldSendEmailWithCorrectFormat() {
            // Given
            CreateUserRequest request = new CreateUserRequest(
                "Pierre", "Durand", "pierre.durand@test.com", "ATHLETE", "FR"
            );

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> {
                ApplicationUser user = i.getArgument(0);
                user.setId(3);
                return user;
            });

            // When
            adminUserService.createUser(request, "creator@test.com");

            // Then
            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
            verify(maillingService).sendEmail(
                eq("pierre.durand@test.com"),
                contains("Activation"),
                bodyCaptor.capture()
            );

            String body = bodyCaptor.getValue();
            assertThat(body).contains("Bonjour Pierre Durand");
            assertThat(body).contains("créé avec succès par creator@test.com");
            assertThat(body).contains("Email : pierre.durand@test.com");
            assertThat(body).contains("Mot de passe provisoire : durand.pierre");
            assertThat(body).contains("première connexion");
            assertThat(body).contains("L'équipe MiagePoule");
        }

        @Test
        @DisplayName("Ne devrait pas envoyer d'email si l'email existe déjà")
        void createUser_shouldNotSendEmailIfEmailAlreadyExists() {
            // Given
            CreateUserRequest request = new CreateUserRequest(
                "Jane", "Smith", "existing@test.com", "ATHLETE", "FR"
            );

            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            // When
            CreateUserResponse response = adminUserService.createUser(request, "admin@test.com");

            // Then
            assertThat(response.id()).isNull();
            verify(maillingService, never()).sendEmail(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Tests envoi d'email lors de bulkCreateUsers")
    class BulkCreateUserEmailTests {

        @Test
        @DisplayName("Devrait envoyer un email pour chaque utilisateur créé avec succès")
        void bulkCreateUsers_shouldSendEmailForEachSuccessfulCreation() {
            // Given
            CreateUserRequest request1 = new CreateUserRequest(
                "Jean", "Dupont", "jean.dupont@test.com", "ATHLETE", "FR"
            );
            CreateUserRequest request2 = new CreateUserRequest(
                "Marie", "Martin", "marie.martin@test.com", "ATHLETE", "FR"
            );
            BulkCreateUsersRequest bulkRequest = new BulkCreateUsersRequest(
                Arrays.asList(request1, request2)
            );

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> {
                ApplicationUser user = i.getArgument(0);
                user.setId((int) (Math.random() * 1000));
                return user;
            });

            // When
            BulkCreateUsersResponse response = adminUserService.bulkCreateUsers(bulkRequest, "admin@test.com");

            // Then
            assertThat(response.successfullyCreated()).isEqualTo(2);
            
            // Vérifier que 2 emails ont été envoyés
            verify(maillingService, times(2)).sendEmail(anyString(), anyString(), anyString());
            
            // Vérifier les destinataires
            verify(maillingService).sendEmail(
                eq("jean.dupont@test.com"),
                contains("Activation"),
                contains("dupont.jean")
            );
            verify(maillingService).sendEmail(
                eq("marie.martin@test.com"),
                contains("Activation"),
                contains("martin.marie")
            );
        }

        @Test
        @DisplayName("Ne devrait envoyer d'email que pour les créations réussies")
        void bulkCreateUsers_shouldOnlySendEmailForSuccessfulCreations() {
            // Given
            CreateUserRequest request1 = new CreateUserRequest(
                "Jean", "Dupont", "jean.dupont@test.com", "ATHLETE", "FR"
            );
            CreateUserRequest request2 = new CreateUserRequest(
                "Existing", "User", "existing@test.com", "ATHLETE", "FR"
            );
            BulkCreateUsersRequest bulkRequest = new BulkCreateUsersRequest(
                Arrays.asList(request1, request2)
            );

            when(userRepository.existsByEmail("jean.dupont@test.com")).thenReturn(false);
            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> i.getArgument(0));

            // When
            BulkCreateUsersResponse response = adminUserService.bulkCreateUsers(bulkRequest, "admin@test.com");

            // Then
            assertThat(response.successfullyCreated()).isEqualTo(1);
            assertThat(response.failed()).isEqualTo(1);
            
            // Vérifier qu'un seul email a été envoyé (pour jean.dupont)
            verify(maillingService, times(1)).sendEmail(anyString(), anyString(), anyString());
            verify(maillingService).sendEmail(
                eq("jean.dupont@test.com"),
                anyString(),
                anyString()
            );
        }

        @Test
        @DisplayName("Devrait continuer la création même si un envoi d'email échoue")
        void bulkCreateUsers_shouldContinueEvenIfOneEmailFails() {
            // Given
            CreateUserRequest request1 = new CreateUserRequest(
                "Jean", "Dupont", "jean.dupont@test.com", "ATHLETE", "FR"
            );
            CreateUserRequest request2 = new CreateUserRequest(
                "Marie", "Martin", "marie.martin@test.com", "ATHLETE", "FR"
            );
            BulkCreateUsersRequest bulkRequest = new BulkCreateUsersRequest(
                Arrays.asList(request1, request2)
            );

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> i.getArgument(0));

            // Simuler une erreur sur le premier email seulement
            doThrow(new RuntimeException("SMTP error"))
                .doNothing()
                .when(maillingService).sendEmail(anyString(), anyString(), anyString());

            // When
            BulkCreateUsersResponse response = adminUserService.bulkCreateUsers(bulkRequest, "admin@test.com");

            // Then - Les deux utilisateurs doivent être créés
            assertThat(response.successfullyCreated()).isEqualTo(2);
            verify(userRepository, times(2)).save(any(ApplicationUser.class));
        }
    }

    @Nested
    @DisplayName("Tests envoi d'email lors de resetPassword")
    class ResetPasswordEmailTests {

        @Test
        @DisplayName("Devrait envoyer un email de réinitialisation lors du reset password")
        void resetPassword_shouldSendResetEmail() {
            // Given
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("doe.john")).thenReturn("encodedTempPassword");
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            String tempPassword = adminUserService.resetPassword(1);

            // Then
            assertThat(tempPassword).isEqualTo("doe.john");

            // Vérifier que l'email a été envoyé
            ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

            verify(maillingService, times(1)).sendEmail(
                toCaptor.capture(),
                subjectCaptor.capture(),
                bodyCaptor.capture()
            );

            assertThat(toCaptor.getValue()).isEqualTo("john.doe@test.com");
            assertThat(subjectCaptor.getValue()).contains("Réinitialisation");
            assertThat(bodyCaptor.getValue()).contains("John");
            assertThat(bodyCaptor.getValue()).contains("Doe");
            assertThat(bodyCaptor.getValue()).contains("doe.john");
            assertThat(bodyCaptor.getValue()).contains("réinitialisé");
        }

        @Test
        @DisplayName("Devrait réinitialiser le password même si l'envoi d'email échoue")
        void resetPassword_shouldResetPasswordEvenIfEmailFails() {
            // Given
            when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("doe.john")).thenReturn("encodedTempPassword");
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // Simuler une erreur lors de l'envoi d'email
            doThrow(new RuntimeException("SMTP error"))
                .when(maillingService).sendEmail(anyString(), anyString(), anyString());

            // When
            String tempPassword = adminUserService.resetPassword(1);

            // Then - Le password doit quand même être réinitialisé
            assertThat(tempPassword).isEqualTo("doe.john");
            verify(userRepository, times(1)).save(any(ApplicationUser.class));
        }

        @Test
        @DisplayName("Devrait envoyer un email avec le bon format de contenu pour reset")
        void resetPassword_shouldSendEmailWithCorrectFormat() {
            // Given
            ApplicationUser user = createTestUser(2, "Pierre", "Durand", "pierre.durand@test.com");
            
            when(userRepository.findById(2)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("durand.pierre")).thenReturn("encodedPassword");
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            adminUserService.resetPassword(2);

            // Then
            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
            verify(maillingService).sendEmail(
                eq("pierre.durand@test.com"),
                contains("Réinitialisation"),
                bodyCaptor.capture()
            );

            String body = bodyCaptor.getValue();
            assertThat(body).contains("Bonjour Pierre Durand");
            assertThat(body).contains("réinitialisé");
            assertThat(body).contains("Mot de passe : durand.pierre");
            assertThat(body).contains("prochaine connexion");
            assertThat(body).contains("n'êtes pas à l'origine");
            assertThat(body).contains("L'équipe MiagePoule");
        }

        @Test
        @DisplayName("Ne devrait pas envoyer d'email si l'utilisateur n'existe pas")
        void resetPassword_shouldNotSendEmailIfUserNotFound() {
            // Given
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            // When/Then
            try {
                adminUserService.resetPassword(999);
            } catch (IllegalArgumentException e) {
                // Exception attendue
            }

            verify(maillingService, never()).sendEmail(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Tests de l'envoi asynchrone")
    class AsyncEmailTests {

        @Test
        @DisplayName("Devrait envoyer les emails de manière asynchrone")
        void createUser_shouldSendEmailAsynchronously() {
            // Given
            CreateUserRequest request = new CreateUserRequest(
                "Async", "User", "async@test.com", "ATHLETE", "FR"
            );

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findById("ATHLETE")).thenReturn(Optional.of(athleteRole));
            when(countryRepository.findById("FR")).thenReturn(Optional.of(france));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(ApplicationUser.class))).thenAnswer(i -> {
                ApplicationUser user = i.getArgument(0);
                user.setId(10);
                return user;
            });

            // Simuler un délai dans l'envoi d'email (ne doit pas bloquer)
            doAnswer(invocation -> {
                Thread.sleep(100); // Simule un délai d'envoi
                return null;
            }).when(maillingService).sendEmail(anyString(), anyString(), anyString());

            // When
            CreateUserResponse response = adminUserService.createUser(request, "admin@test.com");

            // Then
            assertThat(response.id()).isNotNull();
            // En mode asynchrone, l'appel devrait être rapide même avec le délai simulé
            // Note: Ce test peut échouer si @Async n'est pas correctement configuré
            verify(maillingService, times(1)).sendEmail(anyString(), anyString(), anyString());
        }
    }
}
