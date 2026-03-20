package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.admin.ReportingMetricsResponse;
import com.miage.pouleAPI.dtos.admin.ReportingPeriod;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReportingServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @InjectMocks
    private AdminReportingService adminReportingService;

    @Test
    void getMetrics_daily_shouldAggregateWithExpectedRange() {
        LocalDate selectedDate = LocalDate.of(2026, 3, 19);

        NotificationRepository.NotificationTypeCountProjection sentInfo =
                org.mockito.Mockito.mock(NotificationRepository.NotificationTypeCountProjection.class);
        when(sentInfo.getType()).thenReturn("INFO");
        when(sentInfo.getCount()).thenReturn(5L);

        NotificationRepository.NotificationTypeCountProjection sentNullType =
                org.mockito.Mockito.mock(NotificationRepository.NotificationTypeCountProjection.class);
        when(sentNullType.getType()).thenReturn(null);
        when(sentNullType.getCount()).thenReturn(2L);

        ApplicationUserRepository.RoleCountProjection roleAthlete =
                org.mockito.Mockito.mock(ApplicationUserRepository.RoleCountProjection.class);
        when(roleAthlete.getRoleName()).thenReturn("ATHLETE");
        when(roleAthlete.getCount()).thenReturn(7L);

        when(notificationRepository.countSentByTypeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(sentInfo, sentNullType));
        when(applicationUserRepository.countCreatedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L);
        when(applicationUserRepository.countConnectedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(4L);
        when(applicationUserRepository.countUsersByRole())
                .thenReturn(List.of(roleAthlete));

        ReportingMetricsResponse response = adminReportingService.getMetrics(ReportingPeriod.DAILY, selectedDate);

        assertEquals("DAILY", response.periodType());
        assertEquals(selectedDate, response.fromDate());
        assertEquals(selectedDate, response.toDate());
        assertEquals(7L, response.totalSentNotifications());
        assertEquals(3L, response.newAccounts());
        assertEquals(4L, response.connections());
        assertEquals(5L, response.sentNotificationsByType().get("INFO"));
        assertEquals(2L, response.sentNotificationsByType().get("UNKNOWN"));
        assertEquals(7L, response.accountsByRole().get("ATHLETE"));

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(notificationRepository).countSentByTypeBetween(startCaptor.capture(), endCaptor.capture());

        assertEquals(selectedDate.atStartOfDay(), startCaptor.getValue());
        assertEquals(selectedDate.plusDays(1).atStartOfDay(), endCaptor.getValue());
    }

    @Test
    void getMetrics_weekly_shouldUseLast7DaysWindow() {
        LocalDate selectedDate = LocalDate.of(2026, 3, 19);

        when(notificationRepository.countSentByTypeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(applicationUserRepository.countCreatedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(applicationUserRepository.countConnectedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(applicationUserRepository.countUsersByRole())
                .thenReturn(List.of());

        ReportingMetricsResponse response = adminReportingService.getMetrics(ReportingPeriod.WEEKLY, selectedDate);

        assertEquals("WEEKLY", response.periodType());
        assertEquals(selectedDate.minusDays(6), response.fromDate());
        assertEquals(selectedDate, response.toDate());
        assertTrue(response.sentNotificationsByType().isEmpty());
        assertEquals(0L, response.totalSentNotifications());

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(notificationRepository).countSentByTypeBetween(startCaptor.capture(), endCaptor.capture());

        assertEquals(selectedDate.minusDays(6).atStartOfDay(), startCaptor.getValue());
        assertEquals(selectedDate.plusDays(1).atStartOfDay(), endCaptor.getValue());
    }
}
