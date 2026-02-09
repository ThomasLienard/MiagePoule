package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.admin.*;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Country;
import com.miage.pouleAPI.entity.Role;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.CountryRepository;
import com.miage.pouleAPI.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private static final String USER_NOT_FOUND = "Utilisateur non trouvé: ";
    private static final String ROLE_NOT_FOUND = "Rôle non trouvé: ";

    private final ApplicationUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CountryRepository countryRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crée un nouveau compte utilisateur avec mot de passe temporaire
     */
    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request, String createdBy) {
        log.info("Création d'un compte {} par {}", request.roleName(), createdBy);

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Email déjà utilisé: {}", request.email());
            return new CreateUserResponse(null, null, null, null, null, null, 
                "Un compte avec cet email existe déjà");
        }

        // Vérifier le rôle
        Role role = roleRepository.findById(request.roleName())
            .orElseThrow(() -> new IllegalArgumentException(ROLE_NOT_FOUND + request.roleName()));

        // Vérifier le pays si fourni
        Country country = null;
        if (request.countryCode() != null && !request.countryCode().isBlank()) {
            country = countryRepository.findById(request.countryCode())
                .orElse(null);
        }

        // Générer le mot de passe temporaire: nom.prenom
        String tempPassword = (request.lastname() + "." + request.name()).toLowerCase()
            .replaceAll("\\s+", "");

        // Créer l'utilisateur - l'ID sera auto-généré
        ApplicationUser user = new ApplicationUser();
        user.setName(request.name());
        user.setLastname(request.lastname());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(role);
        user.setCountry(country);
        user.setIsActive(true);
        
        // Les spectateurs sont automatiquement validés
        boolean isSpectateur = "SPECTATEUR".equals(request.roleName());
        user.setIsAccountActivated(isSpectateur);
        user.setMustChangePassword(!isSpectateur);
        
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy(createdBy);

        userRepository.save(user);
        log.info("Utilisateur créé avec succès: {} (ID: {})", user.getEmail(), user.getId());

        return new CreateUserResponse(
            user.getId(),
            user.getName(),
            user.getLastname(),
            user.getEmail(),
            role.getRoleName(),
            tempPassword,
            "Compte créé avec succès"
        );
    }

    /**
     * Crée plusieurs comptes utilisateurs à partir d'une liste
     */
    @Transactional
    public BulkCreateUsersResponse bulkCreateUsers(BulkCreateUsersRequest request, String createdBy) {
        log.info("Création en masse de {} utilisateurs par {}", request.users().size(), createdBy);
        
        List<BulkCreateUsersResponse.UserCreationResult> results = request.users().stream()
            .map(userRequest -> {
                try {
                    // Vérifier si l'email existe déjà
                    if (userRepository.existsByEmail(userRequest.email())) {
                        return new BulkCreateUsersResponse.UserCreationResult(
                            userRequest.email(),
                            false,
                            "Un compte avec cet email existe déjà",
                            null
                        );
                    }

                    // Vérifier le rôle
                    Role role = roleRepository.findById(userRequest.roleName())
                        .orElse(null);
                    if (role == null) {
                        return new BulkCreateUsersResponse.UserCreationResult(
                            userRequest.email(),
                            false,
                            ROLE_NOT_FOUND + userRequest.roleName(),
                            null
                        );
                    }

                    // Vérifier le pays si fourni
                    Country country = null;
                    if (userRequest.countryCode() != null && !userRequest.countryCode().isBlank()) {
                        country = countryRepository.findById(userRequest.countryCode())
                            .orElse(null);
                    }

                    // Générer le mot de passe temporaire
                    String tempPassword = (userRequest.lastname() + "." + userRequest.name()).toLowerCase()
                        .replaceAll("\\s+", "");

                    // Créer l'utilisateur
                    ApplicationUser user = new ApplicationUser();
                    user.setName(userRequest.name());
                    user.setLastname(userRequest.lastname());
                    user.setEmail(userRequest.email());
                    user.setPassword(passwordEncoder.encode(tempPassword));
                    user.setRole(role);
                    user.setCountry(country);
                    user.setIsActive(true);
                    
                    // Les spectateurs sont automatiquement validés
                    boolean isSpectateur = "SPECTATEUR".equals(userRequest.roleName());
                    user.setIsAccountActivated(isSpectateur);
                    user.setMustChangePassword(!isSpectateur);
                    
                    user.setCreatedAt(LocalDateTime.now());
                    user.setCreatedBy(createdBy);

                    userRepository.save(user);
                    log.info("Utilisateur créé avec succès: {} (ID: {})", user.getEmail(), user.getId());

                    return new BulkCreateUsersResponse.UserCreationResult(
                        userRequest.email(),
                        true,
                        "Compte créé avec succès",
                        tempPassword
                    );
                } catch (Exception e) {
                    log.error("Erreur lors de la création de l'utilisateur {}: {}", 
                        userRequest.email(), e.getMessage(), e);
                    return new BulkCreateUsersResponse.UserCreationResult(
                        userRequest.email(),
                        false,
                        "Erreur: " + e.getMessage(),
                        null
                    );
                }
            })
            .toList();

        long successCount = results.stream().filter(BulkCreateUsersResponse.UserCreationResult::success).count();
        long failedCount = results.stream().filter(r -> !r.success()).count();

        log.info("Création en masse terminée: {} succès, {} échecs", successCount, failedCount);

        return new BulkCreateUsersResponse(
            request.users().size(),
            (int) successCount,
            (int) failedCount,
            results
        );
    }

    /**
     * Récupère tous les utilisateurs
     */
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::toUserDto)
            .toList();
    }

    /**
     * Récupère les utilisateurs par rôle
     */
    public List<UserDto> getUsersByRole(String roleName) {
        return userRepository.findAll().stream()
            .filter(u -> u.getRole() != null && u.getRole().getRoleName().equals(roleName))
            .map(this::toUserDto)
            .toList();
    }

    /**
     * Récupère un utilisateur par ID
     */
    public UserDto getUserById(Integer id) {
        return userRepository.findById(id)
            .map(this::toUserDto)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND + id));
    }

    /**
     * Met à jour un utilisateur
     */
    @Transactional
    public UserDto updateUser(Integer id, UpdateUserRequest request) {
        ApplicationUser user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND + id));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.lastname() != null && !request.lastname().isBlank()) {
            user.setLastname(request.lastname());
        }
        if (request.email() != null && !request.email().isBlank()) {
            // Vérifier que le nouvel email n'est pas déjà utilisé par un autre
            if (!request.email().equals(user.getEmail()) && userRepository.existsByEmail(request.email())) {
                throw new IllegalArgumentException("Cet email est déjà utilisé");
            }
            user.setEmail(request.email());
        }
        if (request.roleName() != null && !request.roleName().isBlank()) {
            Role role = roleRepository.findById(request.roleName())
                .orElseThrow(() -> new IllegalArgumentException(ROLE_NOT_FOUND + request.roleName()));
            user.setRole(role);
        }
        if (request.countryCode() != null) {
            Country country = countryRepository.findById(request.countryCode()).orElse(null);
            user.setCountry(country);
        }

        userRepository.save(user);
        log.info("Utilisateur mis à jour: {}", user.getEmail());
        return toUserDto(user);
    }

    /**
     * Désactive un compte utilisateur
     */
    @Transactional
    public UserDto deactivateUser(Integer id, String reason) {
        ApplicationUser user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND + id));

        // Empêcher la désactivation des comptes ADMIN
        if (user.getRole() != null && "ADMIN".equals(user.getRole().getRoleName())) {
            throw new IllegalArgumentException("Impossible de désactiver un compte administrateur");
        }

        user.setIsActive(false);
        user.setDeactivatedAt(LocalDateTime.now());
        user.setDeactivationReason(reason);

        userRepository.save(user);
        log.info("Utilisateur désactivé: {} - Raison: {}", user.getEmail(), reason);
        return toUserDto(user);
    }

    /**
     * Réactive un compte utilisateur
     */
    @Transactional
    public UserDto reactivateUser(Integer id) {
        ApplicationUser user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND + id));

        user.setIsActive(true);
        user.setDeactivatedAt(null);
        user.setDeactivationReason(null);

        userRepository.save(user);
        log.info("Utilisateur réactivé: {}", user.getEmail());
        return toUserDto(user);
    }

    /**
     * Active le compte et change le mot de passe (première connexion)
     */
    @Transactional
    public void activateAccount(String email, String newPassword) {
        ApplicationUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        if (Boolean.TRUE.equals(user.getIsAccountActivated())) {
            throw new IllegalStateException("Ce compte est déjà activé");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setIsAccountActivated(true);
        user.setMustChangePassword(false);

        userRepository.save(user);
        log.info("Compte activé avec succès: {}", email);
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur
     */
    @Transactional
    public String resetPassword(Integer id) {
        ApplicationUser user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND + id));

        // Générer un nouveau mot de passe temporaire
        String tempPassword = (user.getLastname() + "." + user.getName()).toLowerCase()
            .replaceAll("\\s+", "");

        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        user.setIsAccountActivated(false);

        userRepository.save(user);
        log.info("Mot de passe réinitialisé pour: {}", user.getEmail());
        return tempPassword;
    }

    private UserDto toUserDto(ApplicationUser user) {
        return new UserDto(
            user.getId(),
            user.getName(),
            user.getLastname(),
            user.getEmail(),
            user.getRole() != null ? user.getRole().getRoleName() : null,
            user.getCountry() != null ? user.getCountry().getCode() : null,
            Boolean.TRUE.equals(user.getIsActive()),
            Boolean.TRUE.equals(user.getIsAccountActivated()),
            Boolean.TRUE.equals(user.getMustChangePassword()),
            user.getCreatedAt(),
            user.getCreatedBy(),
            user.getDeactivatedAt(),
            user.getDeactivationReason()
        );
    }
}
