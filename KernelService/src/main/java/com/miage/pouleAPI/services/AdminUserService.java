package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.admin.*;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Country;
import com.miage.pouleAPI.entity.Role;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.CountryRepository;
import com.miage.pouleAPI.repositories.RoleRepository;
import com.miage.pouleAPI.services.interfaces.MaillingService;
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
    private static final String EMAIL_TEMPLATE = """
        Bonjour %s %s,
    
        Votre compte MiagePoule a été créé avec succès par %s.
    
        Voici vos identifiants de connexion :
        Email : %s
        Mot de passe provisoire : %s
    
        Pour des raisons de sécurité, vous devrez changer ce mot de passe \
        lors de votre première connexion.
    
        Cordialement,
        L'équipe MiagePoule
        """;


    private final ApplicationUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CountryRepository countryRepository;
    private final PasswordEncoder passwordEncoder;
    private final MaillingService maillingService;

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

        Role role = findRoleOrThrow(request.roleName());

        ApplicationUser user = createUserEntity(request, role, createdBy);
        userRepository.save(user);

        log.info("Utilisateur créé avec succès: {} (ID: {})", user.getEmail(), user.getId());
        sendActivationEmail(user, createdBy);

        String tempPassword = generateTempPassword(request);

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

        List<UserCreationResult> results = request.users().stream()
                .map(userRequest -> processSingleUserBulk(userRequest, createdBy))
                .toList();

        int successCount = (int) results.stream().filter(UserCreationResult::success).count();
        int failedCount = results.size() - successCount;

        log.info("Création en masse terminée: {} succès, {} échecs", successCount, failedCount);

        return new BulkCreateUsersResponse(request.users().size(), successCount, failedCount, results);
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
        ApplicationUser user = findUserOrThrow(id);

        request.name()
                .filter(s -> !s.isBlank())
                .ifPresent(user::setName);

        request.lastname()
                .filter(s -> !s.isBlank())
                .ifPresent(user::setLastname);

        request.email()
                .filter(s -> !s.isBlank())
                .ifPresent(email -> this.validateEmail(email, user));

        request.roleName()
                .filter(s -> !s.isBlank())
                .ifPresent(roleName -> {
            Role role = findRoleOrThrow(roleName);
            user.setRole(role);
        });

        request.countryCode()
                .filter(s -> !s.isBlank())
                .ifPresent(countryCode -> {
            Country country = findCountryOrNull(countryCode) ;
            user.setCountry(country);
        });

        userRepository.save(user);
        log.info("Utilisateur mis à jour: {}", user.getEmail());
        return toUserDto(user);
    }

    /**
     * Désactive un compte utilisateur
     */
    @Transactional
    public UserDto deactivateUser(Integer id, String reason) {
        ApplicationUser user = findUserOrThrow(id);

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
        ApplicationUser user = findUserOrThrow(id);

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
        ApplicationUser user = findUserOrThrow(id);

        // Générer un nouveau mot de passe temporaire
        String tempPassword = (user.getLastname() + "." + user.getName()).toLowerCase()
            .replaceAll("\\s+", "");

        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        user.setIsAccountActivated(false);

        userRepository.save(user);
        log.info("Mot de passe réinitialisé pour: {}", user.getEmail());

        // Envoi de l'email de notification de réinitialisation
        try {
            String subject = "Réinitialisation de votre mot de passe MiagePoule";
            String body = String.format("""
                Bonjour %s %s,
                Votre mot de passe a été réinitialisé.
                Voici votre nouveau mot de passe temporaire :
                Mot de passe : %s
                Pour des raisons de sécurité, vous devrez changer ce mot de passe lors de votre prochaine connexion.
                Si vous n'êtes pas à l'origine de cette demande, veuillez contacter un administrateur immédiatement.
                Cordialement,
                L'équipe MiagePoule
                """,
                user.getName(),
                user.getLastname(),
                tempPassword
            );
            maillingService.sendEmail(user.getEmail(), subject, body);
            log.info("Email de réinitialisation envoyé à: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation à {}: {}",
                user.getEmail(), e.getMessage());
            // On ne bloque pas la réinitialisation si l'email échoue
        }

        return tempPassword;
    }

    /**
     * Valide un compte utilisateur (change isAccountValidated à true)
     * Utilisé par un admin après validation des documents
     */
    @Transactional
    public UserDto validateUserAccount(Integer userId) {
        ApplicationUser user = findUserOrThrow(userId);

        if (Boolean.TRUE.equals(user.getIsAccountValidated())) {
            throw new IllegalStateException("Ce compte est déjà validé");
        }

        user.setIsAccountValidated(true);
        userRepository.save(user);

        log.info("Compte utilisateur validé: {} (ID: {})", user.getEmail(), userId);
        return toUserDto(user);
    }

    /**
     * Invalide un compte utilisateur (change isAccountValidated à false)
     * Utilisé par un admin pour révoquer la validation d'un compte
     */
    @Transactional
    public UserDto invalidateUserAccount(Integer userId) {
        ApplicationUser user = findUserOrThrow(userId);

        if (Boolean.FALSE.equals(user.getIsAccountValidated())) {
            throw new IllegalStateException("Ce compte est déjà invalidé");
        }

        user.setIsAccountValidated(false);
        userRepository.save(user);

        log.info("Compte utilisateur invalidé: {} (ID: {})", user.getEmail(), userId);
        return toUserDto(user);
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
            Boolean.TRUE.equals(user.getIsAccountValidated()),
            Boolean.TRUE.equals(user.getMustChangePassword()),
            user.getCreatedAt(),
            user.getCreatedBy(),
            user.getDeactivatedAt(),
            user.getDeactivationReason(),
            Boolean.TRUE.equals(user.getHasSignedCharter())
        );
    }

    private ApplicationUser findUserOrThrow(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND + id));
    }

    private Role findRoleOrThrow(String roleName) {
        return roleRepository.findById(roleName)
                .orElseThrow(() -> new IllegalArgumentException(ROLE_NOT_FOUND + roleName));
    }

    private void validateEmail(String email, ApplicationUser user) {
        if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }
        user.setEmail(email);
    }
    private Country findCountryOrNull(String countryCode) {
        return countryCode != null && !countryCode.isBlank()
                ? countryRepository.findById(countryCode).orElse(null)
                : null;
    }

    private UserCreationResult processSingleUserBulk(
            CreateUserRequest userRequest, String createdBy) {
        try {
            if (userRepository.existsByEmail(userRequest.email())) {
                return new UserCreationResult(
                        userRequest.email(), false, "Un compte avec cet email existe déjà", null);
            }

            Role role = roleRepository.findById(userRequest.roleName()).orElse(null);
            if (role == null) {
                return new UserCreationResult(
                        userRequest.email(), false, ROLE_NOT_FOUND + userRequest.roleName(), null);
            }

            ApplicationUser user = createUserEntity(userRequest, role, createdBy);
            userRepository.save(user);

            sendActivationEmail(user, createdBy);

            String tempPassword = generateTempPassword(userRequest);
            return new UserCreationResult(
                    userRequest.email(), true, "Compte créé avec succès", tempPassword);

        } catch (Exception e) {
            log.error("Erreur création utilisateur {}: {}", userRequest.email(), e.getMessage(), e);
            return new UserCreationResult(
                    userRequest.email(), false, "Erreur: " + e.getMessage(), null);
        }
    }

    private ApplicationUser createUserEntity(CreateUserRequest request, Role role, String createdBy) {
        Country country = findCountryOrNull(request.countryCode());

        ApplicationUser user = new ApplicationUser();
        user.setName(request.name());
        user.setLastname(request.lastname());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(generateTempPassword(request)));
        user.setRole(role);
        user.setCountry(country);
        user.setIsActive(true);
        user.setIsAccountActivated("SPECTATEUR".equals(request.roleName()));
        user.setMustChangePassword(!"SPECTATEUR".equals(request.roleName()));
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy(createdBy);

        return user;
    }

    private void sendActivationEmail(ApplicationUser user, String createdBy) {
        try {
            String subject = "Activation de votre compte MiagePoule";
            String body = String.format(EMAIL_TEMPLATE,
                    user.getName(), user.getLastname(), createdBy,
                    user.getEmail(), generateTempPasswordFromUser(user));
            maillingService.sendEmail(user.getEmail(), subject, body);
            log.info("Email d'activation envoyé à: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Erreur email {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private String generateTempPassword(CreateUserRequest request) {
        return (request.lastname() + "." + request.name()).toLowerCase().replaceAll("\\s+", "");
    }

    private String generateTempPasswordFromUser(ApplicationUser user) {
        return (user.getLastname() + "." + user.getName()).toLowerCase().replaceAll("\\s+", "");
    }

}
