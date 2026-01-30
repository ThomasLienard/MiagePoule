package com.miage.pouleAPI.auth;

import com.miage.pouleAPI.auth.dto.LoginRequest;
import com.miage.pouleAPI.auth.dto.LoginResponse;
import com.miage.pouleAPI.auth.dto.SignUpRequest;
import com.miage.pouleAPI.auth.dto.SignUpResponse;
import com.miage.pouleAPI.dtos.admin.ActivateAccountRequest;
import com.miage.pouleAPI.services.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AdminUserService adminUserService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        AuthService.LoginResponseWithStatus response = authService.loginWithStatus(request);
        return ResponseEntity.ok(Map.of(
            "token", response.token(),
            "mustChangePassword", response.mustChangePassword(),
            "isAccountActivated", response.isAccountActivated()
        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);

        if (response.message().equals("User registered successfully")) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (response.message().equals("Email already exists")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activateAccount(
            @RequestParam String email,
            @Valid @RequestBody ActivateAccountRequest request) {
        try {
            adminUserService.activateAccount(email, request.newPassword());
            return ResponseEntity.ok(Map.of("message", "Compte activé avec succès"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }
}