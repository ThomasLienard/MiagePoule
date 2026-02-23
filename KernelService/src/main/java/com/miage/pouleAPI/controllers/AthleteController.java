package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.participant.ParticipantDTO;
import com.miage.pouleAPI.services.interfaces.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/athlete/trials")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ATHLETE')")
public class AthleteController {

    private final ParticipantService participantService;

    public record ApiResponse(String message) {}

    /**
     * Permet à un sportif de déclarer forfait pour une épreuve
     */
    @PostMapping("/{trialId}/forfeit")
    public ResponseEntity<Object> declareWithdrawal(
            @PathVariable Integer trialId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            ParticipantDTO result = participantService.athleteDeclareWithdrawal(trialId, email);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(e.getMessage()));
        }
    }
}
