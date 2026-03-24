package com.miage.pouleAPI.dtos.incident;

import jakarta.validation.constraints.NotBlank;

public record CreateIncidentRequestDTO(
        @NotBlank(message = "Le titre est obligatoire")
        String title,

        @NotBlank(message = "La description est obligatoire")
        String description,

        @NotBlank(message = "Le niveau de sévérité est obligatoire")
        String severity,

        Integer eventId,
        Integer placeId,
        Integer competitionId,

        /**
         * Impact scope:
         * - "COMMISSAIRES": seulement les commissaires
         * - "COMMISSAIRES_ATHLETES": commissaires + athlètes
         * - "TOUS": tous les observateurs
         */
        String audienceScope
) {}
