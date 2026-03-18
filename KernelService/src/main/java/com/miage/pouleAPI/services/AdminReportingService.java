package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.admin.ReportingMetricsResponse;
import com.miage.pouleAPI.dtos.admin.ReportingPeriod;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminReportingService {

    private final NotificationRepository notificationRepository;
    private final ApplicationUserRepository applicationUserRepository;

    public ReportingMetricsResponse getMetrics(ReportingPeriod period, LocalDate selectedDate) {
        LocalDate date = selectedDate != null ? selectedDate : LocalDate.now();
        TimeRange timeRange = computeRange(period, date);

        Map<String, Long> sentByType = new LinkedHashMap<>();
        notificationRepository.countSentByTypeBetween(timeRange.start(), timeRange.end())
                .forEach(row -> sentByType.put(normalizeType(row.getType()), row.getCount()));

        long newAccounts = applicationUserRepository.countCreatedBetween(timeRange.start(), timeRange.end());
        long connections = applicationUserRepository.countConnectedBetween(timeRange.start(), timeRange.end());

        Map<String, Long> accountsByRole = new LinkedHashMap<>();
        applicationUserRepository.countUsersByRole()
                .forEach(row -> accountsByRole.put(row.getRoleName(), row.getCount()));

        long totalSentNotifications = sentByType.values().stream().mapToLong(Long::longValue).sum();

        return new ReportingMetricsResponse(
                period.name(),
                timeRange.fromDate(),
                timeRange.toDate(),
                sentByType,
                totalSentNotifications,
                newAccounts,
                connections,
                accountsByRole
        );
    }

    private TimeRange computeRange(ReportingPeriod period, LocalDate date) {
        if (period == ReportingPeriod.WEEKLY) {
            LocalDate fromDate = date.minusDays(6);
            LocalDate toDate = date;
            return new TimeRange(
                    fromDate.atStartOfDay(),
                    toDate.plusDays(1).atStartOfDay(),
                    fromDate,
                    toDate
            );
        }

        return new TimeRange(
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay(),
                date,
                date
        );
    }

    private String normalizeType(String type) {
        return type == null || type.isBlank() ? "UNKNOWN" : type;
    }

    private record TimeRange(LocalDateTime start, LocalDateTime end, LocalDate fromDate, LocalDate toDate) {
    }
}
