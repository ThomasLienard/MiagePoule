package com.miage.pouleAPI.dtos.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    @NotBlank(message = "Le prénom est requis")
    String name,
    
    @NotBlank(message = "Le nom est requis")
    String lastname,
    
    @NotBlank(message = "L'email est requis")
    @Email(message = "Format d'email invalide")
    String email,
    
    @NotBlank(message = "Le rôle est requis")
    String roleName,
    
    String countryCode
) {}
