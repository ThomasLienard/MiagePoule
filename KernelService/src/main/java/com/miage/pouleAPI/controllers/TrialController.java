package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.trial.AssignedTrialsResponseDTO;
import com.miage.pouleAPI.dtos.trial.TrialDetailDTO;
import com.miage.pouleAPI.dtos.trial.TrialSummaryDTO;
import com.miage.pouleAPI.services.interfaces.TrialService;
import com.miage.pouleAPI.auth.repository.ApplicationUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TrialController {
    
    private TrialService trialService;
    private ApplicationUserRepository userRepository;

    @Autowired
    public TrialController(TrialService trialService, ApplicationUserRepository userRepository) {
        this.trialService = trialService;
        this.userRepository = userRepository;
    }
    
    @GetMapping("/public/trials")
    public List<TrialSummaryDTO> getAllTrials() {
        return trialService.getAllTrials();
    }
    
    @GetMapping("/public/trials/{id}")
    public ResponseEntity<TrialDetailDTO> getTrialById(@PathVariable Integer id) {
        return trialService.getTrialById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/public/championships/{championshipId}/comp/{competitionId}/trials")
    public List<TrialSummaryDTO> getTrialsByChampionshipAndCompetition(
            @PathVariable Integer championshipId,
            @PathVariable Integer competitionId) {
        return trialService.getTrialsByChampionshipAndCompetition(championshipId, competitionId);
    }

    @GetMapping("/trials/assigned")
    public ResponseEntity<AssignedTrialsResponseDTO> getAssignedTrials(Authentication auth) {
        return trialService.getAssignedTrialsForUserEmail(auth.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
    