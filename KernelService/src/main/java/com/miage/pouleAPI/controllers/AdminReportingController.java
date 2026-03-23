package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.admin.ReportingMetricsResponse;
import com.miage.pouleAPI.dtos.admin.ReportingPeriod;
import com.miage.pouleAPI.services.AdminReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/reporting")
@RequiredArgsConstructor
public class AdminReportingController {

    private final AdminReportingService adminReportingService;

    @GetMapping("/metrics")
    public ResponseEntity<ReportingMetricsResponse> getMetrics(
            @RequestParam(defaultValue = "DAILY") ReportingPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(adminReportingService.getMetrics(period, date));
    }
}
