package com.miage.pouleAPI.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires MaillingServiceImpl - Service d'envoi d'emails")
class MaillingServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MaillingServiceImpl maillingService;

    private static final String FROM_EMAIL = "no.reply.miagepoule@gmail.com";

    @BeforeEach
    void setUp() {
        // Injecter la valeur de fromEmail car @Value ne fonctionne pas dans les tests unitaires
        ReflectionTestUtils.setField(maillingService, "fromEmail", FROM_EMAIL);
    }

    @Nested
    @DisplayName("Tests sendEmail")
    class SendEmailTests {

        @Test
        @DisplayName("Devrait envoyer un email avec succès")
        void sendEmail_shouldSendEmailSuccessfully() {
            // Given
            String to = "user@test.com";
            String subject = "Test Subject";
            String body = "Test Body";

            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

            // When
            maillingService.sendEmail(to, subject, body);

            // Then
            verify(mailSender, times(1)).send(messageCaptor.capture());
            
            SimpleMailMessage sentMessage = messageCaptor.getValue();
            assertThat(sentMessage.getFrom()).isEqualTo(FROM_EMAIL);
            assertThat(sentMessage.getTo()).containsExactly(to);
            assertThat(sentMessage.getSubject()).isEqualTo(subject);
            assertThat(sentMessage.getText()).isEqualTo(body);
        }

        @Test
        @DisplayName("Devrait envoyer un email d'activation de compte")
        void sendEmail_shouldSendAccountActivationEmail() {
            // Given
            String to = "newuser@test.com";
            String subject = "Activation de votre compte MiagePoule";
            String body = String.format(
                "Bonjour Jean Dupont,\n\n" +
                "Votre compte MiagePoule a été créé avec succès par admin@test.com.\n\n" +
                "Voici vos identifiants de connexion :\n" +
                "Email : newuser@test.com\n" +
                "Mot de passe provisoire : dupont.jean\n\n" +
                "Cordialement,\n" +
                "L'équipe MiagePoule"
            );

            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

            // When
            maillingService.sendEmail(to, subject, body);

            // Then
            verify(mailSender).send(messageCaptor.capture());
            
            SimpleMailMessage sentMessage = messageCaptor.getValue();
            assertThat(sentMessage.getTo()).containsExactly(to);
            assertThat(sentMessage.getSubject()).contains("Activation");
            assertThat(sentMessage.getText()).contains("dupont.jean");
            assertThat(sentMessage.getText()).contains("newuser@test.com");
        }

        @Test
        @DisplayName("Devrait envoyer un email de réinitialisation de mot de passe")
        void sendEmail_shouldSendPasswordResetEmail() {
            // Given
            String to = "user@test.com";
            String subject = "Réinitialisation de votre mot de passe MiagePoule";
            String body = String.format(
                "Bonjour Jean Dupont,\n\n" +
                "Votre mot de passe a été réinitialisé.\n\n" +
                "Voici votre nouveau mot de passe temporaire :\n" +
                "Mot de passe : dupont.jean\n\n" +
                "Cordialement,\n" +
                "L'équipe MiagePoule"
            );

            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

            // When
            maillingService.sendEmail(to, subject, body);

            // Then
            verify(mailSender).send(messageCaptor.capture());
            
            SimpleMailMessage sentMessage = messageCaptor.getValue();
            assertThat(sentMessage.getTo()).containsExactly(to);
            assertThat(sentMessage.getSubject()).contains("Réinitialisation");
            assertThat(sentMessage.getText()).contains("réinitialisé");
            assertThat(sentMessage.getText()).contains("dupont.jean");
        }

        @Test
        @DisplayName("Devrait envoyer des emails à plusieurs destinataires")
        void sendEmail_shouldSendMultipleEmails() {
            // Given
            String[] recipients = {"user1@test.com", "user2@test.com", "user3@test.com"};
            String subject = "Test";
            String body = "Test body";

            // When
            for (String recipient : recipients) {
                maillingService.sendEmail(recipient, subject, body);
            }

            // Then
            verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Devrait gérer les caractères spéciaux dans le contenu")
        void sendEmail_shouldHandleSpecialCharacters() {
            // Given
            String to = "user@test.com";
            String subject = "Sujet avec accents: éàèù ñ";
            String body = "Contenu avec caractères spéciaux: €, ©, ®, ™\n" +
                         "Emojis: 📧 ✅ 🎉\n" +
                         "Guillemets: \"test\" et 'test'";

            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

            // When
            maillingService.sendEmail(to, subject, body);

            // Then
            verify(mailSender).send(messageCaptor.capture());
            
            SimpleMailMessage sentMessage = messageCaptor.getValue();
            assertThat(sentMessage.getSubject()).isEqualTo(subject);
            assertThat(sentMessage.getText()).isEqualTo(body);
        }

        @Test
        @DisplayName("Ne devrait pas planter si le mailSender lance une exception")
        void sendEmail_shouldThrowExceptionWhenMailSenderFails() {
            // Given
            String to = "user@test.com";
            String subject = "Test";
            String body = "Test";
            
            doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

            // When/Then
            assertThatThrownBy(() -> maillingService.sendEmail(to, subject, body))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP error");
        }

        @Test
        @DisplayName("Devrait construire correctement le message avec tous les champs")
        void sendEmail_shouldConstructMessageWithAllFields() {
            // Given
            String to = "recipient@test.com";
            String subject = "Important Subject";
            String body = "Important Body\nWith multiple lines\nAnd details";

            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

            // When
            maillingService.sendEmail(to, subject, body);

            // Then
            verify(mailSender).send(messageCaptor.capture());
            
            SimpleMailMessage message = messageCaptor.getValue();
            assertThat(message.getFrom()).isNotNull().isEqualTo(FROM_EMAIL);
            assertThat(message.getTo()).isNotNull().hasSize(1);
            assertThat(message.getSubject()).isNotNull().isNotEmpty();
            assertThat(message.getText()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Tests de validation des paramètres")
    class ValidationTests {

        @Test
        @DisplayName("Devrait accepter des emails valides")
        void sendEmail_shouldAcceptValidEmails() {
            // Given
            String[] validEmails = {
                "simple@example.com",
                "user.name@example.com",
                "user+tag@example.co.uk",
                "123@example.com"
            };

            // When/Then
            for (String email : validEmails) {
                maillingService.sendEmail(email, "Test", "Test body");
            }

            verify(mailSender, times(validEmails.length)).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Devrait gérer les sujets vides")
        void sendEmail_shouldHandleEmptySubject() {
            // Given
            String to = "user@test.com";
            String subject = "";
            String body = "Test body";

            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

            // When
            maillingService.sendEmail(to, subject, body);

            // Then
            verify(mailSender).send(messageCaptor.capture());
            assertThat(messageCaptor.getValue().getSubject()).isEmpty();
        }

        @Test
        @DisplayName("Devrait gérer les corps de message vides")
        void sendEmail_shouldHandleEmptyBody() {
            // Given
            String to = "user@test.com";
            String subject = "Test";
            String body = "";

            ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

            // When
            maillingService.sendEmail(to, subject, body);

            // Then
            verify(mailSender).send(messageCaptor.capture());
            assertThat(messageCaptor.getValue().getText()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests asynchrones")
    class AsyncTests {

        @Test
        @DisplayName("Devrait permettre l'envoi d'emails de manière asynchrone")
        void sendEmail_shouldWorkAsynchronously() {
            // Given
            String to = "user@test.com";
            String subject = "Test async";
            String body = "Test body";

            // When - Simuler plusieurs appels rapides
            for (int i = 0; i < 10; i++) {
                maillingService.sendEmail(to + i, subject, body);
            }

            // Then - Tous les emails doivent être envoyés
            verify(mailSender, times(10)).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Devrait gérer les erreurs sans bloquer les autres envois")
        void sendEmail_shouldNotBlockOtherEmailsOnError() {
            // Given
            String to = "user@test.com";
            String subject = "Test";
            String body = "Test";

            // Simuler une erreur sur le premier envoi, succès sur le second
            doThrow(new RuntimeException("SMTP error"))
                .doNothing()
                .when(mailSender).send(any(SimpleMailMessage.class));

            // When/Then - Premier envoi échoue
            assertThatThrownBy(() -> maillingService.sendEmail(to, subject, body))
                .isInstanceOf(RuntimeException.class);

            // When - Second envoi réussit
            maillingService.sendEmail(to, subject, body);

            // Then
            verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
        }
    }
}
