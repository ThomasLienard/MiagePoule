package com.miage.pouleAPI.dtos.admin;

import java.time.LocalDateTime;

public record UserDto(
    Integer id,
    String name,
    String lastname,
    String email,
    String roleName,
    String countryCode,
    Boolean isActive,
    Boolean isAccountActivated,
    Boolean isAccountValidated,
    Boolean mustChangePassword,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime deactivatedAt,
    String deactivationReason,
    Boolean hasSignedCharter
) {}
