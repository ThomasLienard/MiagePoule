package com.miage.pouleAPI.services;

import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.services.interfaces.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ApplicationUserRepository userRepository;

    @Transactional
    public void signCharter() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        ApplicationUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!"ATHLETE".equals(user.getRole().getRoleName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seuls les athlètes peuvent signer la charte européenne du sport.");
        }

        if (Boolean.TRUE.equals(user.getHasSignedCharter())) {
            return;
        }

        user.setHasSignedCharter(true);

        userRepository.save(user);
    }
}