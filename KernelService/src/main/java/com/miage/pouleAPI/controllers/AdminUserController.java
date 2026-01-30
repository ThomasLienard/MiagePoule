package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.admin.*;
import com.miage.pouleAPI.services.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * Crée un nouveau compte utilisateur
     */
    @PostMapping
    public ResponseEntity<?> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        try {
            String createdBy = authentication.getName();
            CreateUserResponse response = adminUserService.createUser(request, createdBy);
            
            if (response.id() == null) {
                return ResponseEntity.badRequest().body(response);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Récupère tous les utilisateurs
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(required = false) String role) {
        List<UserDto> users;
        if (role != null && !role.isBlank()) {
            users = adminUserService.getUsersByRole(role);
        } else {
            users = adminUserService.getAllUsers();
        }
        return ResponseEntity.ok(users);
    }

    /**
     * Récupère un utilisateur par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        try {
            UserDto user = adminUserService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Met à jour un utilisateur
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody UpdateUserRequest request) {
        try {
            UserDto user = adminUserService.updateUser(id, request);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Désactive un compte utilisateur
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(
            @PathVariable Integer id,
            @Valid @RequestBody DeactivateUserRequest request) {
        try {
            UserDto user = adminUserService.deactivateUser(id, request.reason());
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Réactive un compte utilisateur
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivateUser(@PathVariable Integer id) {
        try {
            UserDto user = adminUserService.reactivateUser(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Integer id) {
        try {
            String tempPassword = adminUserService.resetPassword(id);
            return ResponseEntity.ok(Map.of(
                "message", "Mot de passe réinitialisé",
                "temporaryPassword", tempPassword
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }
}
