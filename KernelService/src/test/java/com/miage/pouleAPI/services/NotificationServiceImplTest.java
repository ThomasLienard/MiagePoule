package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.NotificationDTO;
import com.miage.pouleAPI.entity.*;
import com.miage.pouleAPI.repositories.CompetitionObserverRepository;
import com.miage.pouleAPI.repositories.NotificationRepository;
import com.miage.pouleAPI.repositories.SeverityRepository;
import com.miage.pouleAPI.services.interfaces.SseNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires NotificationService")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SseNotificationService sseNotificationService;

    @Mock
    private SeverityRepository severityRepository;

    @Mock
    private CompetitionObserverRepository competitionObserverRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Event testEvent;
    private Competition testCompetition;
    private CompetitionObserver observer1;
    private CompetitionObserver observer2;

    @BeforeEach
    void setUp() {
        // Création des données de test
        testCompetition = new Competition();
        testCompetition.setId(1);

        testEvent = new Event();
        testEvent.setId(1);
        testEvent.setName("Test Event");
        testEvent.setCompetition(testCompetition);

        // Création des observateurs
        ApplicationUser user1 = new ApplicationUser();
        user1.setId(1);

        ApplicationUser user2 = new ApplicationUser();
        user2.setId(2);

        CompetitionObserverId id1 = new CompetitionObserverId();
        id1.setUserId(1);
        id1.setCompetitionId(1);

        CompetitionObserverId id2 = new CompetitionObserverId();
        id2.setUserId(2);
        id2.setCompetitionId(1);

        observer1 = new CompetitionObserver();
        observer1.setId(id1);
        observer1.setUser(user1);
        observer1.setCompetition(testCompetition);

        observer2 = new CompetitionObserver();
        observer2.setId(id2);
        observer2.setUser(user2);
        observer2.setCompetition(testCompetition);
    }

    @Test
    @DisplayName("notifyEventStart() - Devrait créer et envoyer une notification de début d'événement")
    void testNotifyEventStart() {
        // Given
        Collection<CompetitionObserver> observers = Arrays.asList(observer1, observer2);
        when(competitionObserverRepository.findByCompetition(testCompetition)).thenReturn(observers);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        ArgumentCaptor<Severity> severityCaptor = ArgumentCaptor.forClass(Severity.class);

        // When
        notificationService.notifyEventStart(testEvent);

        // Then
        // Vérifier que la notification a été sauvegardée
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification savedNotification = notificationCaptor.getValue();

        assertThat(savedNotification.getTitle()).isEqualTo("Début de l'épreuve Test Event");
        assertThat(savedNotification.getDescription()).isEqualTo("L'épreuve Test Event va commencer.");
        assertThat(savedNotification.getType()).isEqualTo(TypeNotification.INFO);
        assertThat(savedNotification.getEvent()).isEqualTo(testEvent);
        assertThat(savedNotification.getEmissionDate()).isBeforeOrEqualTo(LocalDateTime.now());

        // Vérifier que la sévérité a été sauvegardée
        verify(severityRepository).save(severityCaptor.capture());
        Severity savedSeverity = severityCaptor.getValue();
        assertThat(savedSeverity.getName()).isEqualTo("info");
        assertThat(savedSeverity.getDescription()).isEqualTo("start event");

        // Vérifier que la notification SSE a été envoyée aux observateurs
        ArgumentCaptor<NotificationDTO> dtoCaptor = ArgumentCaptor.forClass(NotificationDTO.class);
        verify(sseNotificationService, times(2)).sendNotification(any(Integer.class), dtoCaptor.capture());

        NotificationDTO sentDto = dtoCaptor.getValue();
        assertThat(sentDto.getDescription()).isEqualTo("L'épreuve Test Event va commencer.");
        assertThat(sentDto.getType()).isEqualTo("INFO");
        assertThat(sentDto.getEventId()).isEqualTo(1);
        assertThat(sentDto.getSeverity()).isEqualTo("info");
    }

    @Test
    @DisplayName("notifyEventResults() - Devrait créer et envoyer une notification de résultats disponibles")
    void testNotifyEventResults() {
        // Given
        Collection<CompetitionObserver> observers = Arrays.asList(observer1, observer2);
        when(competitionObserverRepository.findByCompetition(testCompetition)).thenReturn(observers);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        ArgumentCaptor<Severity> severityCaptor = ArgumentCaptor.forClass(Severity.class);

        // When
        notificationService.notifyEventResults(testEvent);

        // Then
        // Vérifier que la notification a été sauvegardée
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification savedNotification = notificationCaptor.getValue();

        assertThat(savedNotification.getTitle()).isEqualTo("Résultats disponibles : Test Event");
        assertThat(savedNotification.getDescription()).isEqualTo("Les résultats de Test Event sont disponibles.");
        assertThat(savedNotification.getType()).isEqualTo(TypeNotification.RESULT);
        assertThat(savedNotification.getEvent()).isEqualTo(testEvent);
        assertThat(savedNotification.getEmissionDate()).isBeforeOrEqualTo(LocalDateTime.now());

        // Vérifier que la sévérité a été sauvegardée
        verify(severityRepository).save(severityCaptor.capture());
        Severity savedSeverity = severityCaptor.getValue();
        assertThat(savedSeverity.getName()).isEqualTo("info");
        assertThat(savedSeverity.getDescription()).isEqualTo("results available");

        // Vérifier que la notification SSE a été envoyée aux observateurs
        ArgumentCaptor<NotificationDTO> dtoCaptor = ArgumentCaptor.forClass(NotificationDTO.class);
        verify(sseNotificationService, times(2)).sendNotification(any(Integer.class), dtoCaptor.capture());

        NotificationDTO sentDto = dtoCaptor.getValue();
        assertThat(sentDto.getDescription()).isEqualTo("Les résultats de Test Event sont disponibles.");
        assertThat(sentDto.getType()).isEqualTo("RESULT");
        assertThat(sentDto.getEventId()).isEqualTo(1);
        assertThat(sentDto.getSeverity()).isEqualTo("info");
    }

    @Test
    @DisplayName("notifyEventStart() - Devrait gérer le cas où il n'y a pas d'observateurs")
    void testNotifyEventStart_NoObservers() {
        // Given
        when(competitionObserverRepository.findByCompetition(testCompetition)).thenReturn(List.of());

        // When
        notificationService.notifyEventStart(testEvent);

        // Then
        verify(notificationRepository).save(any(Notification.class));
        verify(severityRepository).save(any(Severity.class));
        verify(sseNotificationService, never()).sendNotification(any(Integer.class), any(NotificationDTO.class));
    }

    @Test
    @DisplayName("notifyEventResults() - Devrait gérer le cas où il n'y a pas d'observateurs")
    void testNotifyEventResults_NoObservers() {
        // Given
        when(competitionObserverRepository.findByCompetition(testCompetition)).thenReturn(List.of());

        // When
        notificationService.notifyEventResults(testEvent);

        // Then
        verify(notificationRepository).save(any(Notification.class));
        verify(severityRepository).save(any(Severity.class));
        verify(sseNotificationService, never()).sendNotification(any(Integer.class), any(NotificationDTO.class));
    }
}