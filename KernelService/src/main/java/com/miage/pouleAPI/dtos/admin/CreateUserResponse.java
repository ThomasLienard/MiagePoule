package com.miage.pouleAPI.dtos.admin;

public record CreateUserResponse(
    Integer id,
    String name,
    String lastname,
    String email,
    String roleName,
    String temporaryPassword,
    String message
) {}
