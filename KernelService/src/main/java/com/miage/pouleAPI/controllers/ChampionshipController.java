package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.championship.ChampionshipDTO;
import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import com.miage.pouleAPI.services.interfaces.ChampionshipService;
import com.miage.pouleAPI.services.interfaces.CompetitionService;

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
    public List<ChampionshipDTO> getAll() {
        return championshipService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChampionshipDTO> getById(@PathVariable Integer id) {
        return championshipService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/comp")
    public List<CompetitionDTO> getCompetitions(@PathVariable Integer id) {
        return competitionService.findByChampionship(id);
    }
}
