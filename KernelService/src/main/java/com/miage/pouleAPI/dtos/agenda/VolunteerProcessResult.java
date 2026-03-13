package com.miage.pouleAPI.dtos.agenda;

public record VolunteerProcessResult(
            String volunteerEmail,
            boolean success,
            int tasksCreated,
            String message
    ) {}