package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.domains.ChampionshipModel;
import com.miage.pouleAPI.domains.CompetitionModel;
import com.miage.pouleAPI.services.ChampionshipService;
import com.miage.pouleAPI.services.CompetitionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/championship")
@CrossOrigin(origins = "http://localhost:3000")
public class ChampionshipController {

    private final ChampionshipService championshipService;
    private final CompetitionService competitionService;

    public ChampionshipController(ChampionshipService championshipService, CompetitionService competitionService) {
        this.championshipService = championshipService;
        this.competitionService = competitionService;
    }

    @GetMapping
    public List<ChampionshipModel> getAll() {
        return championshipService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChampionshipModel> getById(@PathVariable Integer id) {
        return championshipService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/comp")
    public List<CompetitionModel> getCompetitions(@PathVariable Integer id) {
        return competitionService.findByChampionship(id);
    }
}
