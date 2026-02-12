package com.miage.pouleAPI.dtos.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkCreateUsersRequest(
    @NotEmpty(message = "La liste d'utilisateurs ne peut pas être vide")
    @Valid
    List<CreateUserRequest> users
) {}
