package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.championship.CreateChampionshipRequestDTO;
import com.miage.pouleAPI.services.interfaces.ChampionshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}