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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String USER_REGISTERED_SUCCESS = "User registered successfully";

    private final AuthService authService;
    private final AdminUserService adminUserService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        AuthService.LoginResponseWithStatus response = authService.loginWithStatus(request);
        return ResponseEntity.ok(new LoginResponse(
            response.token(),
            response.mustChangePassword(),
            response.isAccountActivated()
        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);
        HttpStatus status = USER_REGISTERED_SUCCESS.equals(response.message()) 
            ? HttpStatus.CREATED 
            : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/activate")
    public ResponseEntity<ApiResponse> activateAccount(
            @RequestParam String email,
            @Valid @RequestBody ActivateAccountRequest request) {
        try {
            adminUserService.activateAccount(email, request.newPassword());
            return ResponseEntity.ok(new ApiResponse("Compte activé avec succès"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
        }
    }

    public record ApiResponse(String message) {}
}