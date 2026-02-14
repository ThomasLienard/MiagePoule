package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.admin.UserDto;
import com.miage.pouleAPI.services.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commissaire/users")
@RequiredArgsConstructor
public class CommissaireUserController {

    private final AdminUserService adminUserService;

    /**
     * Récupère tous les utilisateurs ou filtrés par rôle
     * Endpoint accessible aux COMMISSAIRE pour la gestion des équipes
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
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
    public ResponseEntity<Object> getUserById(@PathVariable Integer id) {
        try {
            UserDto user = adminUserService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
