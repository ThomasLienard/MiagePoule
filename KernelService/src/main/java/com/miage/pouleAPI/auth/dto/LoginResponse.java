package com.miage.pouleAPI.auth.dto;

public record LoginResponse(String token, boolean mustChangePassword, boolean isAccountActivated) {}
