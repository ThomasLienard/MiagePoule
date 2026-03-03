package com.miage.pouleAPI.dtos.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateEventRequestDTO(
        @NotBlank(message = "Le nom est obligatoire")
        String name,

        @NotBlank(message = "La description est obligatoire")
        String description,

        @NotBlank(message = "Le type d'événement est obligatoire")
        String typeEventName,

        @NotNull(message = "L'ID de compétition est obligatoire")
        Integer competitionId,

        @NotNull(message = "La date de début est obligatoire")
        LocalDateTime startTime,

        @NotNull(message = "La date de fin est obligatoire")
        LocalDateTime endTime,

        String placeName,
        String city,
        String street,
        String number,
        String zipCode,
        String descriptionPlace,
        Double latitude,
        Double longitude,
        boolean hasParking,
        
        String typeScoreName  // Optional: "TIME", "POINTS", etc. Si null, valeur par défaut selon le type d'événement
) {}