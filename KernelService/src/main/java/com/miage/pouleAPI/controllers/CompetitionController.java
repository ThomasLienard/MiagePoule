package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.domains.CompetitionModel;
import com.miage.pouleAPI.services.CompetitionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/public/championship")
@CrossOrigin(origins = "http://localhost:3000")
public class CompetitionController {

    private final CompetitionService competitionService;

    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }


    @GetMapping("/{id}/comp/{idComp}")
    public ResponseEntity<CompetitionModel> getById(@PathVariable Integer id,@PathVariable Integer idComp) {
        return competitionService.findById(idComp)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
