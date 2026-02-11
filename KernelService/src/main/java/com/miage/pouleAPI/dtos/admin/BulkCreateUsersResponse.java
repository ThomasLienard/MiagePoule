package com.miage.pouleAPI.dtos.admin;

import java.util.List;

public record BulkCreateUsersResponse(
    int totalRequested,
    int successfullyCreated,
    int failed,
    List<UserCreationResult> results
) {
    public record UserCreationResult(
        String email,
        boolean success,
        String message,
        String temporaryPassword
    ) {}
}
