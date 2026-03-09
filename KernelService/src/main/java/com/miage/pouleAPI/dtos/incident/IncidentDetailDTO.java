package com.miage.pouleAPI.dtos.incident;

import java.time.LocalDateTime;

public record IncidentDetailDTO(
        Integer id,
        String title,
        String description,
        String alertLevel,
        Integer eventId,
        String eventName,
        Integer placeId,
        String placeName,
        String createdByUsername,
        LocalDateTime createdAt
) {}
