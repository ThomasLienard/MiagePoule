package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.adapters.UserAdapter;
import com.miage.pouleAPI.auth.AuthService;
import com.miage.pouleAPI.auth.repository.ApplicationUserRepository;
import com.miage.pouleAPI.dtos.profile.ChangePasswordRequestDTO;
import com.miage.pouleAPI.dtos.profile.UpdateProfileRequestDTO;
import com.miage.pouleAPI.dtos.profile.UserProfileResponseDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class ProfileController {
    private final AuthService authService;
    private final ApplicationUserRepository userRepo;
    private final UserAdapter userAdapter;

    @GetMapping
    public ResponseEntity<UserProfileResponseDTO> getProfile(Authentication auth) {
        return userRepo.findByEmail(auth.getName())
                .map(userAdapter::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/settings")
    public ResponseEntity<UserProfileResponseDTO> updateProfile(
            @RequestBody UpdateProfileRequestDTO dto,
            Authentication auth) {

        ApplicationUser user = userRepo.findByEmail(auth.getName()).orElseThrow();

        ApplicationUser updatedUser = authService.updateProfile(user.getId(), dto);

        return ResponseEntity.ok(userAdapter.toResponseDTO(updatedUser));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody ChangePasswordRequestDTO dto,
            Authentication auth) {

        ApplicationUser user = userRepo.findByEmail(auth.getName()).orElseThrow();
        authService.changePassword(user.getId(), dto.getCurrentPassword(), dto.getNewPassword());

        return ResponseEntity.noContent().build(); // 204 pour un succès sans corps
    }
}