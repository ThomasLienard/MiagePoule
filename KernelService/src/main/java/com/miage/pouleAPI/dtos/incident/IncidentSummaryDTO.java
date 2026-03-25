package com.miage.pouleAPI.dtos.incident;

import java.time.LocalDateTime;

public record IncidentSummaryDTO(
        Integer id,
        String title,
        String severity,
        LocalDateTime emissionDate
) {}
