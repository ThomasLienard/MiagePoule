package com.miage.pouleAPI.controllers;


import com.miage.pouleAPI.dtos.incident.CreateIncidentRequestDTO;
import com.miage.pouleAPI.dtos.incident.IncidentDetailDTO;
import com.miage.pouleAPI.dtos.incident.IncidentSummaryDTO;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.services.interfaces.IncidentService;
import com.miage.pouleAPI.services.interfaces.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/incident")
@RequiredArgsConstructor
public class AdminIncidentController {

    private final NotificationService notificationService;
    private final IncidentService incidentService;
    private final ApplicationUserRepository applicationUserRepository;

    /**
     * Crée un nouvel incident
     * @param requestDTO les données de l'incident
     * @param userDetails les détails de l'utilisateur courant
     * @return l'incident créé
     */
    @PostMapping
    public ResponseEntity<IncidentDetailDTO> createIncident(
            @RequestBody @Valid CreateIncidentRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Integer userId = getUserIdFromUserDetails(userDetails);
        IncidentDetailDTO incident = incidentService.createIncident(requestDTO, userId);
        
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
     * Récupère les incidents par niveau d'alerte
     * @param alertLevel le niveau d'alerte
     * @return la liste des incidents
     */
    @GetMapping("/alert-level/{alertLevel}")
    public ResponseEntity<List<IncidentSummaryDTO>> getIncidentsByAlertLevel(@PathVariable String alertLevel) {
        List<IncidentSummaryDTO> incidents = incidentService.getIncidentsByAlertLevel(alertLevel);
        return ResponseEntity.ok(incidents);
    }

    /**
     * Récupère l'ID de l'utilisateur à partir de ses détails de sécurité
     * @param userDetails les détails de l'utilisateur
     * @return l'ID de l'utilisateur
     */
    private Integer getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("L'utilisateur n'est pas authentifié");
        }

        String email = userDetails.getUsername();
        return applicationUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'email: " + email))
                .getId();
    }

}
