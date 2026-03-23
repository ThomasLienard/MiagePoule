package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.championship.ChampionshipDTO;
import com.miage.pouleAPI.dtos.championship.CreateChampionshipRequestDTO;
import com.miage.pouleAPI.services.interfaces.ChampionshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/champs")
@RequiredArgsConstructor
public class AdminChampionshipController {

    private final ChampionshipService championshipService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateChampionshipRequestDTO request) {
        championshipService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@RequestBody @Valid ChampionshipDTO request) {
        checkUpdateRequest(request);
        championshipService.update(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    private void checkUpdateRequest(ChampionshipDTO request) {
        checkNameInUpdateRequest(request);
        checkDescriptionInUpdateRequest(request);
        checkStartInUpdateRequest(request);
        checkEndInUpdateRequest(request);
    }

    private void checkNameInUpdateRequest(ChampionshipDTO request) {
        if (request.getName().isEmpty()) {
            request.setName(championshipService.findById(request.getId()).get().getName());
        }
    }

    private void checkDescriptionInUpdateRequest(ChampionshipDTO request) {
        if (request.getDescription().isEmpty()) {
            request.setDescription(championshipService.findById(request.getId()).get().getDescription());
        }
    }

    private void checkStartInUpdateRequest(ChampionshipDTO request) {
        if (request.getStart() == null) {
            request.setStart(championshipService.findById(request.getId()).get().getStart());
        }
    }

    private void checkEndInUpdateRequest(ChampionshipDTO request) {
        if (request.getEnd() == null) {
            request.setEnd(championshipService.findById(request.getId()).get().getEnd());
        }
    }
}