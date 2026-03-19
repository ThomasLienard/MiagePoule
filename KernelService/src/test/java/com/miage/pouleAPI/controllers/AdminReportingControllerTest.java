package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.dtos.admin.ReportingMetricsResponse;
import com.miage.pouleAPI.dtos.admin.ReportingPeriod;
import com.miage.pouleAPI.services.AdminReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires AdminReportingController")
class AdminReportingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminReportingService adminReportingService;

    @InjectMocks
    private AdminReportingController adminReportingController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminReportingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("GET /admin/reporting/metrics should use DAILY by default")
    void getMetrics_shouldUseDefaultDailyPeriod() throws Exception {
        ReportingMetricsResponse response = new ReportingMetricsResponse(
                "DAILY",
                LocalDate.of(2026, 3, 19),
                LocalDate.of(2026, 3, 19),
                Map.of("INFO", 2L),
                2L,
                1L,
                3L,
                Map.of("ATHLETE", 10L)
        );

        when(adminReportingService.getMetrics(eq(ReportingPeriod.DAILY), isNull()))
                .thenReturn(response);

        mockMvc.perform(get("/admin/reporting/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodType").value("DAILY"))
                .andExpect(jsonPath("$.totalSentNotifications").value(2))
                .andExpect(jsonPath("$.newAccounts").value(1))
                .andExpect(jsonPath("$.connections").value(3))
                .andExpect(jsonPath("$.sentNotificationsByType.INFO").value(2))
                .andExpect(jsonPath("$.accountsByRole.ATHLETE").value(10));
    }

    @Test
    @DisplayName("GET /admin/reporting/metrics should parse WEEKLY and date")
    void getMetrics_shouldHandleWeeklyAndDateParam() throws Exception {
        LocalDate inputDate = LocalDate.of(2026, 3, 19);

        ReportingMetricsResponse response = new ReportingMetricsResponse(
                "WEEKLY",
                LocalDate.of(2026, 3, 13),
                LocalDate.of(2026, 3, 19),
                Map.of(),
                0L,
                5L,
                8L,
                Map.of("VOLONTAIRE", 4L)
        );

        when(adminReportingService.getMetrics(eq(ReportingPeriod.WEEKLY), eq(inputDate)))
                .thenReturn(response);

        mockMvc.perform(get("/admin/reporting/metrics")
                        .param("period", "WEEKLY")
                        .param("date", "2026-03-19"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodType").value("WEEKLY"))
                .andExpect(jsonPath("$.fromDate[0]").value(2026))
                .andExpect(jsonPath("$.fromDate[1]").value(3))
                .andExpect(jsonPath("$.fromDate[2]").value(13))
                .andExpect(jsonPath("$.toDate[0]").value(2026))
                .andExpect(jsonPath("$.toDate[1]").value(3))
                .andExpect(jsonPath("$.toDate[2]").value(19))
                .andExpect(jsonPath("$.newAccounts").value(5))
                .andExpect(jsonPath("$.connections").value(8))
                .andExpect(jsonPath("$.accountsByRole.VOLONTAIRE").value(4));
    }
}
