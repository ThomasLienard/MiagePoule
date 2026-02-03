package com.miage.pouleAPI.dtos.event;

import java.time.LocalDateTime;

public record CreateEventRequestDTO(
    String name,
    String description,
    String typeEventName,
    Integer competitionId,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String placeName,
    String city,
    String street,
    String number,
    String zipCode,
    String descriptionPlace,
    Double latitude,
    Double longitude,
    boolean hasParking
) {}