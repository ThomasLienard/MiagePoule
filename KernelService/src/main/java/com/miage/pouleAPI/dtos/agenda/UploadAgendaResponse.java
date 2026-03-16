package com.miage.pouleAPI.dtos.agenda;

import java.util.List;

public record UploadAgendaResponse(
        int totalVolunteers,
        int successfullyProcessed,
        int failed,
        List<VolunteerProcessResult> results
) {}
