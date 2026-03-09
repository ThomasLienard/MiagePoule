package com.miage.pouleAPI.dtos.incident;

import jakarta.validation.constraints.NotBlank;

public record CreateIncidentRequestDTO(
        @NotBlank(message = "Le titre est obligatoire")
        String title,

        @NotBlank(message = "La description est obligatoire")
        String description,

        @NotBlank(message = "Le niveau d'alerte est obligatoire")
        String alertLevel,

        Integer eventId,

        Integer placeId
) {}
