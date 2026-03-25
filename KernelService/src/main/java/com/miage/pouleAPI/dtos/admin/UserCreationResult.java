package com.miage.pouleAPI.dtos.admin;

public record UserCreationResult(
        String email,
        boolean success,
        String message,
        String temporaryPassword
) {}