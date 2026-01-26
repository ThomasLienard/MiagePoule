package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.auth.AuthService;
import com.miage.pouleAPI.auth.jwt.JwtService;
import com.miage.pouleAPI.auth.repository.ApplicationUserRepository;
import com.miage.pouleAPI.dtos.profile.ApplicationUserProfileDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.repositories.CountryRepository;
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
    private final JwtService jwtService;
    private final CountryRepository countryRepo;


    @GetMapping
    public ResponseEntity<ApplicationUser> getProfile(Authentication auth) {
        String email = auth.getName();
        ApplicationUser user = userRepo.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/settings")
    public ResponseEntity<String> updateProfile(@RequestBody ApplicationUserProfileDTO dto, Authentication authentication) {
        ApplicationUser user = userRepo.findByEmail(authentication.getName()).orElseThrow();
        authService.updateProfile(
                user.getId(),
                dto.getEmail(),
                dto.getName(),
                dto.getLastname(),
                dto.getCountry()
        );

        return ResponseEntity.ok("Updated");
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@RequestBody ApplicationUserProfileDTO req, Authentication auth) {
        String email = auth.getName();
        ApplicationUser user = userRepo.findByEmail(email).orElseThrow();
        authService.changePassword(user.getId(), req.getCurrentPassword(), req.getNewPassword());
        return ResponseEntity.ok("Password changed");
    }
}
