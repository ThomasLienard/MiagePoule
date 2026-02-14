package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.team.CreateTeamRequestDTO;
import com.miage.pouleAPI.dtos.team.TeamDTO;
import com.miage.pouleAPI.dtos.team.UpdateTeamRequestDTO;
import com.miage.pouleAPI.services.interfaces.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commissaire/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    public record ApiResponse(String message) {}

    /**
     * Récupère toutes les équipes
     */
    @GetMapping
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        List<TeamDTO> teams = teamService.findAll();
        return ResponseEntity.ok(teams);
    }

    /**
     * Récupère une équipe par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeamDTO> getTeamById(@PathVariable Integer id) {
        return teamService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée une nouvelle équipe
     */
    @PostMapping
    public ResponseEntity<Object> createTeam(@Valid @RequestBody CreateTeamRequestDTO request) {
        try {
            TeamDTO createdTeam = teamService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(e.getMessage()));
        }
    }

    /**
     * Met à jour une équipe existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateTeam(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateTeamRequestDTO request) {
        try {
            TeamDTO updatedTeam = teamService.update(id, request);
            return ResponseEntity.ok(updatedTeam);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(e.getMessage()));
        }
    }

    /**
     * Supprime une équipe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteTeam(@PathVariable Integer id) {
        try {
            teamService.delete(id);
            return ResponseEntity.ok(new ApiResponse("Équipe supprimée avec succès"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(e.getMessage()));
        }
    }
}
