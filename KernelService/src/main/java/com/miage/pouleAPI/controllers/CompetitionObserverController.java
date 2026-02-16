package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.competition.CompetitionObserverDTO;
import com.miage.pouleAPI.entity.CompetitionObserver;
import com.miage.pouleAPI.services.CompetitionObserverService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/notifications/stream/")
public class CompetitionObserverController {

    private final CompetitionObserverService competitionObserverService;

    public CompetitionObserverController(CompetitionObserverService competitionObserverService) {
        this.competitionObserverService = competitionObserverService;
    }

    @GetMapping("/observers")
    public ResponseEntity<List<CompetitionObserverDTO>> getCompetitionObserversByUserId(@RequestParam Integer userId) {
        List<CompetitionObserver> observers = this.competitionObserverService.getCompetitionObserversByUserId(userId);
        if (observers == null) {
            return ResponseEntity.notFound().build();
        }

        List<CompetitionObserverDTO> competitionObserverDTOs = new ArrayList<>();
        // map to DTO
        observers.forEach(o -> {
            competitionObserverDTOs.add(new CompetitionObserverDTO(o.getId().getCompetitionId(), o.getId().getUserId()));
        });

        return ResponseEntity.ok(competitionObserverDTOs);
    }
}
