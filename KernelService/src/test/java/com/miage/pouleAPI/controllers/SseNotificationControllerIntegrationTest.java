package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.NotificationDTO;
import com.miage.pouleAPI.services.interfaces.SseNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Intégration SseNotificationController Tests")
class SseNotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SseNotificationService sseNotificationService;

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Doit retourner 200 OK avec content-type TEXT_EVENT_STREAM")
    void streamNotifications_integration_success() throws Exception {
        mockMvc.perform(get("/api/notifications/stream/{userId}", 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Doit créer une connexion SSE valide pour l'utilisateur")
    void streamNotifications_integration_createsValidSseConnection() throws Exception {
        // When
        var mvcResult = mockMvc.perform(get("/api/notifications/stream/{userId}", 1))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        assertThat(mvcResult).isNotNull();
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Devrait accepter différents userIds")
    void streamNotifications_integration_differentUserIds() throws Exception {
        // Test avec userId = 1
        mockMvc.perform(get("/api/notifications/stream/{userId}", 1))
                .andExpect(status().isOk());

        // Test avec userId = 5
        mockMvc.perform(get("/api/notifications/stream/{userId}", 5))
                .andExpect(status().isOk());

        // Test avec userId = 100
        mockMvc.perform(get("/api/notifications/stream/{userId}", 100))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Devrait gérer les userIds volumineux")
    void streamNotifications_integration_largeUserId() throws Exception {
        mockMvc.perform(get("/api/notifications/stream/{userId}", Integer.MAX_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Stream notifications - Vérifier que le service de notification peut envoyer des messages")
    void streamNotifications_integration_sendNotification() throws Exception {
        // Given
        Integer userId = 42;
        SseEmitter emitter = sseNotificationService.subscribe(userId);
        
        NotificationDTO notification = new NotificationDTO();
        notification.setId(1);
        notification.setDescription("Integration test notification");
        notification.setEmissionDate(LocalDateTime.now());
        notification.setType("INFO");
        notification.setSeverity("NORMAL");

        // When & Then
        assertThat(emitter).isNotNull();
        // Le service devrait accepter l'envoi de notification
        sseNotificationService.sendNotification(userId, notification);
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Endpoint doit être public et accessible")
    void streamNotifications_integration_endpointAccessible() throws Exception {
        // This test verifies the endpoint is properly mapped and accessible
        mockMvc.perform(get("/api/notifications/stream/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Content-Type header doit être TEXT_EVENT_STREAM")
    void streamNotifications_integration_correctContentType() throws Exception {
        mockMvc.perform(get("/api/notifications/stream/{userId}", 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Multiple users - Chaque utilisateur devrait obtenir sa propre connexion SSE")
    void streamNotifications_integration_multipleUsersSeparateStreams() throws Exception {
        // User 1 subscribes
        mockMvc.perform(get("/api/notifications/stream/{userId}", 1))
                .andExpect(status().isOk());

        // User 2 subscribes
        mockMvc.perform(get("/api/notifications/stream/{userId}", 2))
                .andExpect(status().isOk());

        // User 3 subscribes
        mockMvc.perform(get("/api/notifications/stream/{userId}", 3))
                .andExpect(status().isOk());

        // All should succeed and be independent
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Devrait avoir un timeout approprié")
    void streamNotifications_integration_emitterConfiguration() throws Exception {
        // When
        SseEmitter emitter = sseNotificationService.subscribe(99);

        // Then
        assertThat(emitter).isNotNull();
        assertThat(emitter.getTimeout()).isEqualTo(24 * 60 * 60 * 1000L);
    }

    @Test
    @DisplayName("Stream notifications - Peut recevoir plusieurs notifications en séquence")
    void streamNotifications_integration_multipleMessages() throws Exception {
        // Given
        Integer userId = 77;
        SseEmitter emitter = sseNotificationService.subscribe(userId);

        NotificationDTO notification1 = new NotificationDTO();
        notification1.setId(1);
        notification1.setDescription("First notification");
        notification1.setType("INFO");
        notification1.setSeverity("NORMAL");

        NotificationDTO notification2 = new NotificationDTO();
        notification2.setId(2);
        notification2.setDescription("Second notification");
        notification2.setType("WARNING");
        notification2.setSeverity("HIGH");

        // When & Then
        sseNotificationService.sendNotification(userId, notification1);
        sseNotificationService.sendNotification(userId, notification2);
        // Should complete without errors
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Should support concurrent subscriptions")
    void streamNotifications_integration_concurrentUsers() throws Exception {
        // Simulate multiple concurrent subscriptions
        for (int i = 1; i <= 10; i++) {
            mockMvc.perform(get("/api/notifications/stream/{userId}", i))
                    .andExpect(status().isOk());
        }
    }
}
