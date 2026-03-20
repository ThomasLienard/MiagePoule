package com.miage.pouleAPI.dtos.agenda;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AgendaUploadItemDTO(
        @NotBlank(message = "L'email du bénévole est obligatoire")
        @Email(message = "L'email du bénévole n'est pas valide")
        String volunteerEmail,

        @NotEmpty(message = "La liste des tâches ne peut pas être vide")
        @Valid
        List<TaskUploadItemDTO> tasks
) {}
