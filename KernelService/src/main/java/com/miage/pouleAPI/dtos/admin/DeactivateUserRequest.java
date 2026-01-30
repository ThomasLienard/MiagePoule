package com.miage.pouleAPI.dtos.admin;

import jakarta.validation.constraints.NotBlank;

public record DeactivateUserRequest(
    @NotBlank(message = "La raison de désactivation est requise")
    String reason
) {}
