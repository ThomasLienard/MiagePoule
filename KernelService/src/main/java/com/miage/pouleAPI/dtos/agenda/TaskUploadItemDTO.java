package com.miage.pouleAPI.dtos.agenda;

import jakarta.validation.constraints.NotBlank;

public record TaskUploadItemDTO(
        @NotBlank(message = "Le nom de la tâche est obligatoire")
        String name,

        String description,

        @NotBlank(message = "Le nom de la compétition est obligatoire")
        String competitionName,

        @NotBlank(message = "Le nom de l'événement est obligatoire")
        String eventName
) {}
