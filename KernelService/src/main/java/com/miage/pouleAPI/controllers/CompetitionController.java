package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import com.miage.pouleAPI.services.interfaces.CompetitionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/public/championship")
public class CompetitionController {

    private final CompetitionService competitionService;

    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }


    @GetMapping("/{id}/comp/{idComp}")
    public ResponseEntity<CompetitionDTO> getById(@PathVariable Integer id, @PathVariable Integer idComp) {
        return competitionService.findById(idComp)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
