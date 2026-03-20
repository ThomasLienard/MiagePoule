package com.miage.pouleAPI.dtos.admin;

import java.time.LocalDate;
import java.util.Map;

public record ReportingMetricsResponse(
        String periodType,
        LocalDate fromDate,
        LocalDate toDate,
        Map<String, Long> sentNotificationsByType,
        long totalSentNotifications,
        long newAccounts,
        long connections,
        Map<String, Long> accountsByRole
) {}
