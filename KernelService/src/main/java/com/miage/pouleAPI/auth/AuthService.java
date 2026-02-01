package com.miage.pouleAPI.auth;

import com.miage.pouleAPI.adapters.UserAdapter;
import com.miage.pouleAPI.auth.dto.LoginRequest;
import com.miage.pouleAPI.auth.dto.SignUpRequest;
import com.miage.pouleAPI.auth.dto.SignUpResponse;
import com.miage.pouleAPI.auth.jwt.JwtService;
import com.miage.pouleAPI.auth.repository.ApplicationUserRepository;
import com.miage.pouleAPI.dtos.profile.UpdateProfileRequestDTO;
import com.miage.pouleAPI.dtos.profile.UpdateProfileResponse;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Country;
import com.miage.pouleAPI.entity.Role;
import com.miage.pouleAPI.repositories.CountryRepository;
import com.miage.pouleAPI.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final ApplicationUserRepository userRepo;
    private final RoleRepository roleRepository;
    private final CountryRepository countryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserAdapter userAdapter;

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

    public SignUpResponse signUp(SignUpRequest request) {
        log.info("Tentative d'inscription pour: {}", request.email());

        // Vérifier si l'email existe déjà
        if (userRepo.findByEmail(request.email()).isPresent()) {
            log.error("Email déjà utilisé: {}", request.email());
            return new SignUpResponse(
                    null,
                    request.email(),
                    null,
                    null,
                    null,
                    "Email already exists"
            );
        }

        // Vérifier si le rôle existe
        Role role = roleRepository.findById(request.roleName())
                .orElseThrow(() -> {
                    log.error("Rôle non trouvé: {}", request.roleName());
                    return new IllegalArgumentException("Role not found: " + request.roleName());
                });

        // Vérifier si le pays existe
        Country country = countryRepository.findById(request.countryCode())
                .orElseThrow(() -> {
                    log.error("Pays non trouvé: {}", request.countryCode());
                    return new IllegalArgumentException("Country not found: " + request.countryCode());
                });

        // Générer un nouvel ID (utiliser la séquence max + 1)
        Integer maxId = userRepo.findMaxId();
        Integer newId = (maxId != null ? maxId : 0) + 1;

        log.info("Création d'un nouvel utilisateur avec ID: {}", newId);

        // Créer l'utilisateur - ajuster selon votre constructeur ApplicationUser
        ApplicationUser user = new ApplicationUser();
        user.setId(newId);
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setLastname(request.lastname());
        user.setRole(role);
        user.setCountry(country);

        // Sauvegarder l'utilisateur
        userRepo.save(user);
        log.info("Utilisateur créé avec succès: {}", user.getEmail());

        // Générer le token JWT
        String token = jwtService.generateToken(user.getId(), user.getEmail(), role.getRoleName());

        return new SignUpResponse(
                token,
                user.getEmail(),
                user.getName(),
                user.getLastname(),
                role.getRoleName(),
                "User registered successfully"
        );
    }

    @Transactional
    public UpdateProfileResponse updateProfile(Integer userId, UpdateProfileRequestDTO dto) {
        ApplicationUser user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        boolean emailChanged = false;
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email incorrect");
            }
            user.setEmail(dto.getEmail());
            emailChanged = true;
        }

        // MapStruct vérifie chaque champ du DTO.
        // S'il est null, il ne touche pas au champ correspondant dans 'user'.
        userAdapter.updateEntityFromDto(dto, user);
        if (dto.getCountryCode() != null) {
            Country country = countryRepository.findById(dto.getCountryCode())
                    .orElseThrow(() -> new IllegalArgumentException("Code pays invalide"));
            user.setCountry(country);
        }

        String newToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().getRoleName());
        userRepo.save(user);

        return new UpdateProfileResponse(userAdapter.toResponseDTO(user), newToken);
    }

    //Si il y a une erreur rien n'est écrit en BDD
    @Transactional
    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        ApplicationUser user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("L'ancien mot de passe est incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }
}