package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.participant.AddParticipantRequest;
import com.miage.pouleAPI.dtos.participant.ForfeitRequest;
import com.miage.pouleAPI.dtos.participant.ParticipantDTO;
import com.miage.pouleAPI.dtos.participant.TrialParticipantsDTO;
import com.miage.pouleAPI.dtos.participant.TrialParticipantsFullDTO;
import com.miage.pouleAPI.services.interfaces.ParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commissaire/trials")
@RequiredArgsConstructor
@Slf4j
public class ParticipantController {

    private static final String ATHLETE_TYPE = "ATHLETE";
    private static final String TEAM_TYPE = "TEAM";
    private static final String INVALID_PARTICIPANT_TYPE = "Type de participant invalide. Utilisez 'ATHLETE' ou 'TEAM'.";

    private final ParticipantService participantService;

    public record ApiResponse(String message) {}

    /**
     * Récupère toutes les épreuves avec leurs participants
     */
    @GetMapping
    public ResponseEntity<List<TrialParticipantsDTO>> getAllTrials() {
        List<TrialParticipantsDTO> trials = participantService.getTrialsForCommissaire();
        return ResponseEntity.ok(trials);
    }

    /**
     * Récupère les participants d'une épreuve spécifique
     */
    @GetMapping("/{trialId}/participants")
    public ResponseEntity<TrialParticipantsDTO> getTrialParticipants(@PathVariable Integer trialId) {
        return participantService.getTrialParticipants(trialId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Récupère les participants d'une épreuve avec tous les potentiels (athlètes ET équipes)
     */
    @GetMapping("/{trialId}/participants/full")
    public ResponseEntity<TrialParticipantsFullDTO> getTrialParticipantsFull(@PathVariable Integer trialId) {
        return participantService.getTrialParticipantsFull(trialId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Inscrit un participant à une épreuve
     */
    @PostMapping("/{trialId}/participants")
    public ResponseEntity<Object> addParticipant(
            @PathVariable Integer trialId,
            @Valid @RequestBody AddParticipantRequest request) {
        try {
            ParticipantDTO participant;
            
            if (ATHLETE_TYPE.equalsIgnoreCase(request.getParticipantType())) {
                participant = participantService.addAthleteToTrial(trialId, request.getParticipantId());
            } else if (TEAM_TYPE.equalsIgnoreCase(request.getParticipantType())) {
                participant = participantService.addTeamToTrial(trialId, request.getParticipantId());
            } else {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(INVALID_PARTICIPANT_TYPE));
            }
            
            return ResponseEntity.ok(participant);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(e.getMessage()));
        }
    }

    /**
     * Déclare un participant forfait
     */
    @PostMapping("/{trialId}/forfeit")
    public ResponseEntity<Object> forfeitParticipant(
            @PathVariable Integer trialId,
            @Valid @RequestBody ForfeitRequest request) {
        try {
            ParticipantDTO participant;
            
            if (ATHLETE_TYPE.equalsIgnoreCase(request.getParticipantType())) {
                participant = participantService.forfeitAthlete(trialId, request.getParticipantId());
            } else if (TEAM_TYPE.equalsIgnoreCase(request.getParticipantType())) {
                participant = participantService.forfeitTeam(trialId, request.getParticipantId());
            } else {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(INVALID_PARTICIPANT_TYPE));
            }
            
            return ResponseEntity.ok(participant);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(e.getMessage()));
        }
    }

    /**
     * Annule le forfait d'un participant
     */
    @PostMapping("/{trialId}/unforfeit")
    public ResponseEntity<Object> unforfeitParticipant(
            @PathVariable Integer trialId,
            @Valid @RequestBody ForfeitRequest request) {
        try {
            ParticipantDTO participant;
            
            if (ATHLETE_TYPE.equalsIgnoreCase(request.getParticipantType())) {
                participant = participantService.unforfeitAthlete(trialId, request.getParticipantId());
            } else if (TEAM_TYPE.equalsIgnoreCase(request.getParticipantType())) {
                participant = participantService.unforfeitTeam(trialId, request.getParticipantId());
            } else {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(INVALID_PARTICIPANT_TYPE));
            }
            
            return ResponseEntity.ok(participant);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(e.getMessage()));
        }
    }

    /**
     * Retire un participant d'une épreuve
     */
    @DeleteMapping("/{trialId}/participants")
    public ResponseEntity<Object> removeParticipant(
            @PathVariable Integer trialId,
            @RequestParam Integer participantId,
            @RequestParam String participantType) {
        try {
            if (ATHLETE_TYPE.equalsIgnoreCase(participantType)) {
                participantService.removeAthleteFromTrial(trialId, participantId);
            } else if (TEAM_TYPE.equalsIgnoreCase(participantType)) {
                participantService.removeTeamFromTrial(trialId, participantId);
            } else {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(INVALID_PARTICIPANT_TYPE));
            }
            
            return ResponseEntity.ok(new ApiResponse("Participant retiré avec succès"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(e.getMessage()));
        }
    }
}
