package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.competition.CompetitionObserverDTO;
import com.miage.pouleAPI.entity.CompetitionObserver;
import com.miage.pouleAPI.services.CompetitionObserverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/notifications/stream/")
public class CompetitionObserverController {

    private final CompetitionObserverService competitionObserverService;

    public CompetitionObserverController(CompetitionObserverService competitionObserverService) {
        this.competitionObserverService = competitionObserverService;
    }

    @GetMapping("/observers")
    public ResponseEntity<CompetitionObserverDTO> getCompetitionObserversByUserId(@RequestParam Integer userId) {
        CompetitionObserver observer = this.competitionObserverService.getCompetitionObserversByUserId(userId);
        if (observer == null) {
            return ResponseEntity.notFound().build();
        }
        // map to DTO
        CompetitionObserverDTO competitionObserverDTO = new CompetitionObserverDTO(observer.getId().getCompetitionId(), observer.getId().getUserId());

        return ResponseEntity.ok(competitionObserverDTO);
    }
}
