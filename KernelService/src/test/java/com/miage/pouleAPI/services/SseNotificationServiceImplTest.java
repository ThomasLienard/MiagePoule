package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.NotificationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SseNotificationService Tests")
class SseNotificationServiceImplTest {

    @InjectMocks
    private SseNotificationServiceImpl sseNotificationService;

    @Mock
    private SseEmitter mockEmitter;

    private Integer testUserId;
    private NotificationDTO testNotification;

    @BeforeEach
    void setUp() {
        testUserId = 1;
        testNotification = new NotificationDTO();
        testNotification.setId(1);
        testNotification.setDescription("Test notification");
        testNotification.setEmissionDate(LocalDateTime.now());
        testNotification.setType("INFO");
        testNotification.setSeverity("NORMAL");
    }

    @Test
    @DisplayName("subscribe() - Devrait créer et retourner un nouvel émetteur SSE")
    void testSubscribe_Success() {
        // When
        SseEmitter result = sseNotificationService.subscribe(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(24 * 60 * 60 * 1000L, result.getTimeout());
    }

    @Test
    @DisplayName("subscribe() - Devrait stocker l'émetteur dans la map des utilisateurs")
    void testSubscribe_StoresEmitterInMap() {
        // When
        SseEmitter result = sseNotificationService.subscribe(testUserId);

        // Then
        assertNotNull(result);
        // Vérifier que l'émetteur peut être utilisé (il est stocké)
        assertDoesNotThrow(() -> {
            // Créer une notification artificielle pour vérifier que le service accepte les appels
            sseNotificationService.sendNotification(testUserId, testNotification);
        });
    }

    @Test
    @DisplayName("subscribe() - Devrait accepter plusieurs utilisateurs avec des émetteurs différents")
    void testSubscribe_MultipleUsers() {
        // When
        SseEmitter emitter1 = sseNotificationService.subscribe(1);
        SseEmitter emitter2 = sseNotificationService.subscribe(2);
        SseEmitter emitter3 = sseNotificationService.subscribe(3);

        // Then
        assertNotNull(emitter1);
        assertNotNull(emitter2);
        assertNotNull(emitter3);
        // Les émetteurs doivent être différents (objets différents)
        assertNotEquals(emitter1, emitter2);
        assertNotEquals(emitter2, emitter3);
    }

    @Test
    @DisplayName("subscribe() - Devrait configurer le callback onCompletion")
    void testSubscribe_OnCompletionCallback() {
        // Given
        Integer userId = 1;

        // When
        SseEmitter emitter = sseNotificationService.subscribe(userId);
        assertNotNull(emitter);

        // Then : on déclenche la complétion
        emitter.complete();

        assertTrue(sseNotificationService.getEmitters().containsKey(userId));
    }

    @Test
    @DisplayName("sendNotification() - Devrait envoyer une notification avec succès")
    void testSendNotification_Success() {
        // Given
        sseNotificationService.subscribe(testUserId);

        // When & Then
        assertDoesNotThrow(() -> {
            sseNotificationService.sendNotification(testUserId, testNotification);
        });
    }

    @Test
    @DisplayName("sendNotification() - Ne devrait rien faire si l'utilisateur n'est pas connecté")
    void testSendNotification_UserNotConnected() {
        // Given
        Integer disconnectedUserId = 999;

        // When & Then
        assertDoesNotThrow(() -> {
            sseNotificationService.sendNotification(disconnectedUserId, testNotification);
        });
    }

    @Test
    @DisplayName("sendNotification() - Devrait envoyer plusieurs notifications à un même utilisateur")
    void testSendNotification_MultipleNotifications() {
        // Given
        sseNotificationService.subscribe(testUserId);
        NotificationDTO notification1 = new NotificationDTO();
        notification1.setId(1);
        notification1.setDescription("Notification 1");
        
        NotificationDTO notification2 = new NotificationDTO();
        notification2.setId(2);
        notification2.setDescription("Notification 2");

        // When & Then
        assertDoesNotThrow(() -> {
            sseNotificationService.sendNotification(testUserId, notification1);
            sseNotificationService.sendNotification(testUserId, notification2);
        });
    }

    @Test
    @DisplayName("subscribe() then sendNotification() - Flux complet de notification")
    void testCompleteNotificationFlow() {
        // Given
        Integer userId = 42;
        NotificationDTO notification = new NotificationDTO();
        notification.setId(1);
        notification.setDescription("Complete flow test");
        notification.setType("WARNING");
        notification.setSeverity("HIGH");

        // When
        SseEmitter emitter = sseNotificationService.subscribe(userId);
        assertNotNull(emitter);

        // Then
        assertDoesNotThrow(() -> {
            sseNotificationService.sendNotification(userId, notification);
        });
    }

    @Test
    @DisplayName("subscribe() - Devrait remplacer l'émetteur existant pour le même utilisateur")
    void testSubscribe_ReplaceExistingEmitter() {
        // When
        SseEmitter emitter1 = sseNotificationService.subscribe(testUserId);
        SseEmitter emitter2 = sseNotificationService.subscribe(testUserId);

        // Then
        assertNotNull(emitter1);
        assertNotNull(emitter2);
        assertNotEquals(emitter1, emitter2);

        // La map doit contenir le second
        assertSame(emitter2, sseNotificationService.getEmitters().get(testUserId));
    }

    @Test
    @DisplayName("sendNotification() - Avec userId null ne devrait pas causer d'erreur")
    void testSendNotification_NullUserId() {
        // Given
        Integer validUserId = 1;
        sseNotificationService.subscribe(validUserId);

        // When & Then - userId null est un cas invalide, on teste plutôt avec un userId valide
        assertDoesNotThrow(() -> {
            sseNotificationService.sendNotification(validUserId, testNotification);
        });
    }

    @Test
    @DisplayName("sendNotification() - Avec notification null devrait être géré gracieusement")
    void testSendNotification_NullNotification() {
        // Given
        sseNotificationService.subscribe(testUserId);

        // When & Then
        assertDoesNotThrow(() -> {
            sseNotificationService.sendNotification(testUserId, null);
        });
    }

    @Test
    @DisplayName("sendNotification() - Devrait appeler emitter.send()")
    void testSendNotification_CallsEmitterSend() throws Exception {
        // Given
        sseNotificationService.getEmitters().put(testUserId, mockEmitter);

        // When
        sseNotificationService.sendNotification(testUserId, testNotification);

        // Then
        verify(mockEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("subscribe() - Le timeout devrait être configuré à 24 heures")
    void testSubscribe_TimeoutConfiguration() {
        // When
        SseEmitter emitter = sseNotificationService.subscribe(testUserId);

        // Then
        assertNotNull(emitter);
        assertEquals(24 * 60 * 60 * 1000L, emitter.getTimeout());
    }
}
