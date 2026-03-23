package com.miage.pouleAPI.auth.dto;

public record LoginResponseWithStatus(String token, Boolean mustChangePassword, Boolean isAccountActivated, Boolean isAccountValidated) {}