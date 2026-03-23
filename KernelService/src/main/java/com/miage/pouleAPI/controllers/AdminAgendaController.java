package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.agenda.AgendaUploadItemDTO;
import com.miage.pouleAPI.dtos.agenda.UploadAgendaResponse;
import com.miage.pouleAPI.services.interfaces.VolunteerAgendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/agenda")
@RequiredArgsConstructor
public class AdminAgendaController {

    private final VolunteerAgendaService volunteerAgendaService;

    /**
     * Téléverse les agendas des bénévoles depuis un fichier JSON.
     * Chaque entrée contient l'email d'un bénévole et la liste de ses tâches.
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadAgendaResponse> uploadAgendas(
            @Valid @RequestBody List<AgendaUploadItemDTO> agendas) {

        UploadAgendaResponse response = volunteerAgendaService.uploadAgendas(agendas);

        if (response.failed() == response.totalVolunteers()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
