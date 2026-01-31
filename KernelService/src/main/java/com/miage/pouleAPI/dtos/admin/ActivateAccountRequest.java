package com.miage.pouleAPI.dtos.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivateAccountRequest(
    @NotBlank(message = "Le nouveau mot de passe est requis")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    String newPassword
) {}
