package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.auth.AuthService;
import com.miage.pouleAPI.auth.jwt.JwtService;
import com.miage.pouleAPI.auth.repository.ApplicationUserRepository;
import com.miage.pouleAPI.dtos.profile.ChangePasswordRequest;
import com.miage.pouleAPI.dtos.profile.UpdateProfileRequest;
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

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@RequestBody UpdateProfileRequest req, Authentication auth) {
        String email = auth.getName();
        ApplicationUser user = userRepo.findByEmail(email).orElseThrow();
        authService.updateProfile(user.getId(), req.getEmail(), req.getName(), req.getLastname(), countryRepo.getReferenceById(req.getCountryCode()) );
        return ResponseEntity.ok("Updated");
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest req, Authentication auth) {
        String email = auth.getName();
        ApplicationUser user = userRepo.findByEmail(email).orElseThrow();
        authService.changePassword(user.getId(), req.getCurrentPassword(), req.getNewPassword());
        return ResponseEntity.ok("Password changed");
    }
}
