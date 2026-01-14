package com.miage.pouleAPI.auth;

import com.miage.pouleAPI.auth.repository.ApplicationUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordCheckRunner implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final ApplicationUserRepository userRepository;

    @Override
    public void run(String... args) {
        // Vérifiez le hash du mot de passe
        String rawPassword = "test123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Mot de passe hashé pour 'test123': " + encodedPassword);

        // Afficher tous les utilisateurs
        userRepository.findAll().forEach(user -> {
            System.out.println("Utilisateur: " + user.getEmail() + ", Password: " + user.getPassword());
            System.out.println("Match avec 'test123': " + passwordEncoder.matches("test123", user.getPassword()));
        });
    }
}