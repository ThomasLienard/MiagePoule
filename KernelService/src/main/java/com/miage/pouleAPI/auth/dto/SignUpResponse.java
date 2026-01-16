package com.miage.pouleAPI.auth.dto;

public record SignUpResponse(
        String token,
        String email,
        String name,
        String lastname,
        String role,
        String message
) {}
