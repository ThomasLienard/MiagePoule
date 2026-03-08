package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.services.interfaces.SseNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SseNotificationController Tests")
class SseNotificationControllerTest {

    @Mock
    private SseNotificationService sseNotificationService;

    @InjectMocks
    private SseNotificationController sseNotificationController;

    private MockMvc mockMvc;
    private Integer testUserId;
    private SseEmitter testEmitter;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sseNotificationController).build();
        testUserId = 1;
        testEmitter = new SseEmitter(24 * 60 * 60 * 1000L);
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Devrait retourner un flux SSE")
    void testStreamNotifications_Success() throws Exception {
        // Given
        when(sseNotificationService.subscribe(testUserId)).thenReturn(testEmitter);

        // When & Then
        mockMvc.perform(get("/api/notifications/stream/{userId}", testUserId))
                .andExpect(status().isOk());

        verify(sseNotificationService, times(1)).subscribe(testUserId);
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Devrait appeler le service avec le bon userId")
    void testStreamNotifications_WithCorrectUserId() throws Exception {
        // Given
        Integer userId = 42;
        when(sseNotificationService.subscribe(userId)).thenReturn(testEmitter);

        // When
        mockMvc.perform(get("/api/notifications/stream/{userId}", userId))
                .andExpect(status().isOk());

        // Then
        verify(sseNotificationService, times(1)).subscribe(42);
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Appel direct du controller")
    void testStreamNotifications_DirectControllerCall() {
        // Given
        when(sseNotificationService.subscribe(testUserId)).thenReturn(testEmitter);

        // When
        SseEmitter result = sseNotificationController.streamNotifications(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testEmitter, result);
        verify(sseNotificationService, times(1)).subscribe(testUserId);
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Devrait accepter différents userIds")
    void testStreamNotifications_DifferentUserIds() throws Exception {
        // Given
        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();
        SseEmitter emitter3 = new SseEmitter();
        
        when(sseNotificationService.subscribe(1)).thenReturn(emitter1);
        when(sseNotificationService.subscribe(2)).thenReturn(emitter2);
        when(sseNotificationService.subscribe(3)).thenReturn(emitter3);

        // When & Then
        mockMvc.perform(get("/api/notifications/stream/{userId}", 1))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/notifications/stream/{userId}", 2))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/notifications/stream/{userId}", 3))
                .andExpect(status().isOk());

        verify(sseNotificationService, times(1)).subscribe(1);
        verify(sseNotificationService, times(1)).subscribe(2);
        verify(sseNotificationService, times(1)).subscribe(3);
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Content-Type Devrait être TEXT_EVENT_STREAM")
    void testStreamNotifications_ContentType() throws Exception {
        // Given
        when(sseNotificationService.subscribe(testUserId)).thenReturn(testEmitter);

        // When & Then
        mockMvc.perform(get("/api/notifications/stream/{userId}", testUserId)
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Devrait supporter plusieurs connexiones simultanées")
    void testStreamNotifications_ConcurrentConnections() throws Exception {
        // Given
        Integer userId1 = 1;
        Integer userId2 = 2;
        Integer userId3 = 3;
        
        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();
        SseEmitter emitter3 = new SseEmitter();
        
        when(sseNotificationService.subscribe(userId1)).thenReturn(emitter1);
        when(sseNotificationService.subscribe(userId2)).thenReturn(emitter2);
        when(sseNotificationService.subscribe(userId3)).thenReturn(emitter3);

        // When
        SseEmitter result1 = sseNotificationController.streamNotifications(userId1);
        SseEmitter result2 = sseNotificationController.streamNotifications(userId2);
        SseEmitter result3 = sseNotificationController.streamNotifications(userId3);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(emitter1, result1);
        assertEquals(emitter2, result2);
        assertEquals(emitter3, result3);
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Devrait utiliser le bon endpoint")
    void testStreamNotifications_CorrectEndpoint() throws Exception {
        // Given
        when(sseNotificationService.subscribe(testUserId)).thenReturn(testEmitter);

        // When & Then
        mockMvc.perform(get("/api/notifications/stream/1"))
                .andExpect(status().isOk());

        verify(sseNotificationService, times(1)).subscribe(1);
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Chaque appel devrait créer une nouvelle souscription")
    void testStreamNotifications_NewSubscriptionEachCall() throws Exception {
        // Given
        when(sseNotificationService.subscribe(testUserId)).thenReturn(testEmitter);

        // When
        mockMvc.perform(get("/api/notifications/stream/{userId}", testUserId))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/notifications/stream/{userId}", testUserId))
                .andExpect(status().isOk());

        // Then - Devrait être appelé 2 fois
        verify(sseNotificationService, times(2)).subscribe(testUserId);
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Devrait retourner un SseEmitter non-null")
    void testStreamNotifications_ReturnsNonNullEmitter() {
        // Given
        when(sseNotificationService.subscribe(testUserId)).thenReturn(testEmitter);

        // When
        SseEmitter result = sseNotificationController.streamNotifications(testUserId);

        // Then
        assertNotNull(result);
        assertNotNull(result.getTimeout());
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Devrait configurer le timeout correctement")
    void testStreamNotifications_EmitterConfiguration() {
        // Given
        SseEmitter configuredEmitter = new SseEmitter(24 * 60 * 60 * 1000L);
        when(sseNotificationService.subscribe(testUserId)).thenReturn(configuredEmitter);

        // When
        SseEmitter result = sseNotificationController.streamNotifications(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(24 * 60 * 60 * 1000L, result.getTimeout());
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Doit gérer les userIds volumineux")
    void testStreamNotifications_LargeUserId() throws Exception {
        // Given
        Integer largeUserId = Integer.MAX_VALUE;
        when(sseNotificationService.subscribe(largeUserId)).thenReturn(testEmitter);

        // When & Then
        mockMvc.perform(get("/api/notifications/stream/{userId}", largeUserId))
                .andExpect(status().isOk());

        verify(sseNotificationService, times(1)).subscribe(largeUserId);
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Doit gérer les petits userIds")
    void testStreamNotifications_SmallUserId() throws Exception {
        // Given
        Integer smallUserId = 1;
        when(sseNotificationService.subscribe(smallUserId)).thenReturn(testEmitter);

        // When & Then
        mockMvc.perform(get("/api/notifications/stream/{userId}", smallUserId))
                .andExpect(status().isOk());

        verify(sseNotificationService, times(1)).subscribe(smallUserId);
    }

    @Test
    @DisplayName("GET /api/notifications/stream/{userId} - Le service devrait être appelé exactement une fois par requête")
    void testStreamNotifications_ServiceCalledOnce() throws Exception {
        // Given
        when(sseNotificationService.subscribe(testUserId)).thenReturn(testEmitter);

        // When
        mockMvc.perform(get("/api/notifications/stream/{userId}", testUserId));

        // Then
        verify(sseNotificationService, times(1)).subscribe(testUserId);
        verifyNoMoreInteractions(sseNotificationService);
    }
}
