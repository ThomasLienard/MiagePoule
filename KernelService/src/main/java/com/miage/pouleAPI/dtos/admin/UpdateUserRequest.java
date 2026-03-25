package com.miage.pouleAPI.dtos.admin;

import java.util.Optional;

public record UpdateUserRequest(
    Optional<String> name,
    Optional<String> lastname,
    Optional<String> email,
    Optional<String> roleName,
    Optional<String> countryCode
) {}
