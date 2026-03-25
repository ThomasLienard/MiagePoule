package com.miage.pouleAPI.controllers;


import com.miage.pouleAPI.dtos.incident.CreateIncidentRequestDTO;
import com.miage.pouleAPI.dtos.incident.IncidentDetailDTO;
import com.miage.pouleAPI.dtos.incident.IncidentSummaryDTO;
import com.miage.pouleAPI.services.interfaces.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/incident")
@RequiredArgsConstructor
public class AdminIncidentController {

    private final IncidentService incidentService;

    /**
     * Crée un nouvel incident
     * @param requestDTO les données de l'incident
     * @return l'incident créé
     */
    @PostMapping
    public ResponseEntity<IncidentDetailDTO> createIncident(
            @RequestBody @Valid CreateIncidentRequestDTO requestDTO) {

        IncidentDetailDTO incident = incidentService.createIncident(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(incident);
    }

    /**
     * Récupère tous les incidents
     * @return la liste de tous les incidents
     */
    @GetMapping
    public ResponseEntity<List<IncidentSummaryDTO>> getAllIncidents() {
        List<IncidentSummaryDTO> incidents = incidentService.getAllIncidents();
        return ResponseEntity.ok(incidents);
    }

    /**
     * Récupère un incident par son ID
     * @param id l'ID de l'incident
     * @return l'incident
     */
    @GetMapping("/{id}")
    public ResponseEntity<Optional<IncidentDetailDTO>> getIncidentById(@PathVariable Integer id) {
        Optional<IncidentDetailDTO> incident = incidentService.getIncidentById(id);
        if (incident.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(incident);
    }

    /**
     * Récupère les incidents d'une épreuve
     * @param eventId l'ID de l'épreuve
     * @return la liste des incidents
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<IncidentSummaryDTO>> getIncidentsByEvent(@PathVariable Integer eventId) {
        List<IncidentSummaryDTO> incidents = incidentService.getIncidentsByEventId(eventId);
        return ResponseEntity.ok(incidents);
    }

    /**
     * Récupère les incidents d'un lieu
     * @param placeId l'ID du lieu
     * @return la liste des incidents
     */
    @GetMapping("/place/{placeId}")
    public ResponseEntity<List<IncidentSummaryDTO>> getIncidentsByPlace(@PathVariable Integer placeId) {
        List<IncidentSummaryDTO> incidents = incidentService.getIncidentsByPlaceId(placeId);
        return ResponseEntity.ok(incidents);
    }

    /**
     * Récupère les incidents par niveau de sévérité
     * @param severity le niveau de sévérité
     * @return la liste des incidents
     */
    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<IncidentSummaryDTO>> getIncidentsBySeverity(@PathVariable String severity) {
        List<IncidentSummaryDTO> incidents = incidentService.getIncidentsBySeverity(severity);
        return ResponseEntity.ok(incidents);
    }

}
