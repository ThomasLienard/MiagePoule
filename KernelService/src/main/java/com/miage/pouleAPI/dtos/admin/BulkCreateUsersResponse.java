package com.miage.pouleAPI.dtos.admin;

import java.util.List;

public record BulkCreateUsersResponse(
    int totalRequested,
    int successfullyCreated,
    int failed,
    List<UserCreationResult> results
) {}
