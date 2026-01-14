package com.miage.pouleAPI.auth;

import com.miage.pouleAPI.auth.dto.LoginRequest;
import com.miage.pouleAPI.auth.jwt.JwtService;
import com.miage.pouleAPI.auth.repository.ApplicationUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final ApplicationUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(LoginRequest request) {
        log.info("Tentative de login pour: {}", request.email());

        var user = userRepo.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé: {}", request.email());
                    return new BadCredentialsException("User not found");
                });

        log.info("Utilisateur trouvé: {}", user.getEmail());
        log.info("Password stocké: {}", user.getPassword());
        log.info("Password fourni: {}", request.password());

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPassword());
        log.info("Password match: {}", passwordMatches);

        if (!passwordMatches) {
            log.error("Mot de passe incorrect pour: {}", request.email());
            throw new BadCredentialsException("Bad credentials");
        }

        String role = user.getRole().getRoleName();
        log.info("Rôle de l'utilisateur: {}", role);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), role);
        log.info("Token généré pour: {}", request.email());

        return token;
    }
}