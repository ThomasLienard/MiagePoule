package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import com.miage.pouleAPI.dtos.competition.CreateCompetitionRequestDTO;
import com.miage.pouleAPI.services.interfaces.CompetitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@RequestBody @Valid CompetitionDTO request) {
        checkUpdateRequest(request);
        competitionService.update(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    private void checkUpdateRequest(CompetitionDTO request) {
        checkNameInUpdateRequest(request);
        checkDescriptionInUpdateRequest(request);
        checkStartInUpdateRequest(request);
        checkEndInUpdateRequest(request);
    }

    private void checkNameInUpdateRequest(CompetitionDTO request) {
        if (request.getName().isEmpty()) {
            request.setName(competitionService.findById(request.getId()).get().getName());
        }
    }

    private void checkDescriptionInUpdateRequest(CompetitionDTO request) {
        if (request.getDescription().isEmpty()) {
            request.setDescription(competitionService.findById(request.getId()).get().getDescription());
        }
    }

    private void checkStartInUpdateRequest(CompetitionDTO request) {
        if (request.getStart() == null) {
            request.setStart(competitionService.findById(request.getId()).get().getStart());
        }
    }

    private void checkEndInUpdateRequest(CompetitionDTO request) {
        if (request.getEnd() == null) {
            request.setEnd(competitionService.findById(request.getId()).get().getEnd());
        }
    }
}