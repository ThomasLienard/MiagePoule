package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.competition.CreateCompetitionRequestDTO;
import com.miage.pouleAPI.services.interfaces.CompetitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/comps")
@RequiredArgsConstructor
public class AdminCompetitionController {

    private final CompetitionService competitionService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateCompetitionRequestDTO request) {
        competitionService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}