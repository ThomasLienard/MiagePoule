package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.agenda.VolunteerTaskDTO;
import com.miage.pouleAPI.services.interfaces.VolunteerAgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/volunteer/agenda")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VOLONTAIRE')")
public class VolunteerAgendaController {

    private final VolunteerAgendaService volunteerAgendaService;

    @GetMapping
    public List<VolunteerTaskDTO> getAgenda() {
        return volunteerAgendaService.getCurrentVolunteerAgenda();
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<VolunteerTaskDTO> getTaskDetails(@PathVariable Integer taskId) {
        return volunteerAgendaService.getCurrentVolunteerTask(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
