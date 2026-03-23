package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.result.BulkSetResultRequest;
import com.miage.pouleAPI.dtos.result.ResultDTO;
import com.miage.pouleAPI.dtos.result.SetResultRequest;
import com.miage.pouleAPI.dtos.result.TrialResultsDTO;
import com.miage.pouleAPI.dtos.result.ValidateResultRequest;
import com.miage.pouleAPI.services.interfaces.ResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commissaire/trials")
@RequiredArgsConstructor
public class ResultController {

    private static final String ATHLETE_TYPE = "ATHLETE";
    private static final String TEAM_TYPE = "TEAM";
    private static final String INVALID_TYPE_MSG = "Type de participant invalide. Utilisez 'ATHLETE' ou 'TEAM'.";

    private final ResultService resultService;

    public record ApiResponse(String message) {}

    /**
     * US1 + US2 - Récupère tous les résultats d'une épreuve (base pour saisie/modification)
     * GET /commissaire/trials/{trialId}/results
     */
    @GetMapping("/{trialId}/results")
    public ResponseEntity<TrialResultsDTO> getTrialResults(@PathVariable Integer trialId) {
        return resultService.getTrialResults(trialId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * US1 - Saisit ou modifie le résultat d'un participant (athlète ou équipe)
     * PUT /commissaire/trials/{trialId}/results
     */
    @PutMapping("/{trialId}/results")
    public ResponseEntity<Object> setResult(
            @PathVariable Integer trialId,
            @Valid @RequestBody SetResultRequest request) {
        try {
            ResultDTO result;
            if (ATHLETE_TYPE.equalsIgnoreCase(request.getParticipantType())) {
                result = resultService.setAthleteResult(trialId, request.getParticipantId(), request.getResult());
            } else if (TEAM_TYPE.equalsIgnoreCase(request.getParticipantType())) {
                result = resultService.setTeamResult(trialId, request.getParticipantId(), request.getResult());
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse(INVALID_TYPE_MSG));
            }
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(new ApiResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
        }
    }

    /**
     * US2 - Modifie plusieurs résultats en une seule opération
     * PUT /commissaire/trials/{trialId}/results/bulk
     */
    @PutMapping("/{trialId}/results/bulk")
    public ResponseEntity<Object> setBulkResults(
            @PathVariable Integer trialId,
            @Valid @RequestBody BulkSetResultRequest request) {
        try {
            List<ResultDTO> results = resultService.setBulkResults(trialId, request);
            return ResponseEntity.ok(results);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(new ApiResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
        }
    }

    /**
     * US3 - Valide le résultat d'un participant individuellement
     * POST /commissaire/trials/{trialId}/results/validate
     */
    @PostMapping("/{trialId}/results/validate")
    public ResponseEntity<Object> validateResult(
            @PathVariable Integer trialId,
            @Valid @RequestBody ValidateResultRequest request) {
        try {
            ResultDTO result;
            if (ATHLETE_TYPE.equalsIgnoreCase(request.getParticipantType())) {
                result = resultService.validateAthleteResult(trialId, request.getParticipantId());
            } else if (TEAM_TYPE.equalsIgnoreCase(request.getParticipantType())) {
                result = resultService.validateTeamResult(trialId, request.getParticipantId());
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse(INVALID_TYPE_MSG));
            }
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(new ApiResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
        }
    }

    /**
     * US3 - Valide tous les résultats d'une épreuve d'un seul coup
     * POST /commissaire/trials/{trialId}/results/validate-all
     */
    @PostMapping("/{trialId}/results/validate-all")
    public ResponseEntity<Object> validateAllResults(@PathVariable Integer trialId) {
        try {
            TrialResultsDTO results = resultService.validateAllResults(trialId);
            return ResponseEntity.ok(results);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(new ApiResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
        }
    }

    /**
     * US3 - Invalide (dévalide) le résultat d'un participant
     * POST /commissaire/trials/{trialId}/results/invalidate
     */
    @PostMapping("/{trialId}/results/invalidate")
    public ResponseEntity<Object> invalidateResult(
            @PathVariable Integer trialId,
            @Valid @RequestBody ValidateResultRequest request) {
        try {
            ResultDTO result;
            if (ATHLETE_TYPE.equalsIgnoreCase(request.getParticipantType())) {
                result = resultService.invalidateAthleteResult(trialId, request.getParticipantId());
            } else if (TEAM_TYPE.equalsIgnoreCase(request.getParticipantType())) {
                result = resultService.invalidateTeamResult(trialId, request.getParticipantId());
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse(INVALID_TYPE_MSG));
            }
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(new ApiResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
        }
    }
}
