package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.auth.repository.ApplicationUserRepository;
import com.miage.pouleAPI.dtos.profile.PrivacyDTO;
import com.miage.pouleAPI.dtos.profile.PrivacyUpdateRequestDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.services.interfaces.PrivacyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account/privacy")
@RequiredArgsConstructor
public class PrivacyController {

    private final PrivacyService privacyService;
    private final ApplicationUserRepository userRepo;

    @GetMapping
    public ResponseEntity<List<PrivacyDTO>> getSettings(Authentication auth) {
        ApplicationUser user = userRepo.findByEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok(privacyService.getUserPrivacySettings(user.getId()));
    }

    @PutMapping("/{categoryName}")
    public ResponseEntity<Void> updateSetting(
            @PathVariable String categoryName,
            @RequestBody PrivacyUpdateRequestDTO update,
            Authentication auth) {
        
        ApplicationUser user = userRepo.findByEmail(auth.getName()).orElseThrow();
        privacyService.updateSetting(user.getId(), categoryName, update.isEnabled());
        return ResponseEntity.noContent().build();
    }
}