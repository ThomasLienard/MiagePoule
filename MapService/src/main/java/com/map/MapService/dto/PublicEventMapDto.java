package com.map.MapService.dto;

import java.math.BigDecimal;

public record PublicEventMapDto(
        Integer id,
        String eventName,
        String competitionName,
        String placeName,
        String city,
        String street,
        BigDecimal latitude,
        BigDecimal longitude,
        String startTime,
        String endTime
) {}
