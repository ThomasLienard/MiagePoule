package com.miage.pouleAPI.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "First name is required")
        String name,

        @NotBlank(message = "Last name is required")
        String lastname,

        @NotBlank(message = "Country code is required")
        @Size(min = 2, max = 2, message = "Country code must be 2 characters")
        String countryCode,

        @NotBlank(message = "Role is required")
        String roleName
) {}
