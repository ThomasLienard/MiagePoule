package com.miage.pouleAPI.dtos.admin;

public record UpdateUserRequest(
    String name,
    String lastname,
    String email,
    String roleName,
    String countryCode
) {}
