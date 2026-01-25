package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.trial.TrialDetailDTO;
import com.miage.pouleAPI.dtos.trial.TrialSummaryDTO;
import com.miage.pouleAPI.services.interfaces.TrialService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public")
public class TrialController {
    
    
    private TrialService trialService;

    @Autowired
    public TrialController(TrialService trialService) {
        this.trialService = trialService;
    }
    
    @GetMapping("/trials")
    public List<TrialSummaryDTO> getAllTrials() {
        return trialService.getAllTrials();
    }
    
    @GetMapping("/trials/{id}")
    public ResponseEntity<TrialDetailDTO> getTrialById(@PathVariable Integer id) {
        return trialService.getTrialById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/championships/{championshipId}/comp/{competitionId}/trials")
    public List<TrialSummaryDTO> getTrialsByChampionshipAndCompetition(
            @PathVariable Integer championshipId,
            @PathVariable Integer competitionId) {
        return trialService.getTrialsByChampionshipAndCompetition(championshipId, competitionId);
    }
}
