package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.participant.*;
import com.miage.pouleAPI.entity.*;
import com.miage.pouleAPI.repositories.*;
import com.miage.pouleAPI.services.interfaces.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private static final String ATHLETE_TYPE = "ATHLETE";
    private static final String TEAM_TYPE = "TEAM";
    private static final String TRIAL_NOT_FOUND = "Épreuve non trouvée";
    private static final String ATHLETE_NOT_REGISTERED = "L'athlète n'est pas inscrit à cette épreuve";
    private static final String TEAM_NOT_REGISTERED = "L'équipe n'est pas inscrite à cette épreuve";

    private final TrialRepository trialRepository;
    private final ApplicationUserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ParticipateAtRepository participateAtRepository;
    private final IsConvenedToRepository isConvenedToRepository;

    @Override
    public Optional<AthleteDTO> getAthleteById(Integer athleteId) {
        Optional<ApplicationUser> optionalUser = userRepository.findById(athleteId);
        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }
        ApplicationUser user = optionalUser.get();

        AthleteDTO athleteDTO = new AthleteDTO(
                user.getId(),
                (user.getName() + " " + user.getLastname()),
                user.getCountry().getCode()
        );
        return Optional.of(athleteDTO);
    }

    @Override
    public Optional<TrialParticipantsDTO> getTrialParticipants(Integer trialId) {
        return trialRepository.findById(trialId).map(trial -> {
            TrialParticipantsDTO dto = new TrialParticipantsDTO();
            dto.setTrialId(trial.getId());
            dto.setTrialName(trial.getName());
            
            // Déterminer si c'est une épreuve en équipe ou solo
            boolean hasTeamParticipation = participateAtRepository.hasTeamParticipation(trialId);
            
            // L'épreuve est en équipe si elle a déjà des participations d'équipe
            // Sinon elle est considérée comme solo (ou indéterminée si vide)
            boolean isTeamTrial = hasTeamParticipation;
            dto.setTeamTrial(isTeamTrial);
            
            if (isTeamTrial) {
                // Mode équipe - montrer les équipes
                dto.setParticipants(getTeamParticipants(trialId));
                dto.setPotentialParticipants(getPotentialTeams(trialId));
            } else {
                // Mode solo - montrer les athlètes
                dto.setParticipants(getAthleteParticipants(trialId));
                dto.setPotentialParticipants(getPotentialAthletes(trialId));
            }
            
            return dto;
        });
    }
    
    @Override
    public Optional<TrialParticipantsFullDTO> getTrialParticipantsFull(Integer trialId) {
        return trialRepository.findById(trialId).map(trial -> {
            TrialParticipantsFullDTO dto = new TrialParticipantsFullDTO();
            dto.setTrialId(trial.getId());
            dto.setTrialName(trial.getName());
            
            boolean hasTeamParticipation = participateAtRepository.hasTeamParticipation(trialId);
            boolean hasAthleteParticipation = isConvenedToRepository.hasAthleteParticipation(trialId);
            
            dto.setTeamTrial(hasTeamParticipation);
            dto.setCanChangeType(!hasTeamParticipation && !hasAthleteParticipation);
            
            // Récupérer les participants actuels
            if (hasTeamParticipation) {
                dto.setParticipants(getTeamParticipants(trialId));
            } else {
                dto.setParticipants(getAthleteParticipants(trialId));
            }
            
            // Toujours fournir les deux listes de potentiels
            dto.setPotentialAthletes(getPotentialAthletes(trialId));
            dto.setPotentialTeams(getPotentialTeams(trialId));
            
            return dto;
        });
    }

    @Override
    @Transactional
    public ParticipantDTO addAthleteToTrial(Integer trialId, Integer athleteId) {
        Trial trial = trialRepository.findById(trialId)
                .orElseThrow(() -> new IllegalArgumentException(TRIAL_NOT_FOUND));
        
        ApplicationUser athlete = userRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Athlète non trouvé"));
        
        // Vérifier que c'est bien un athlète
        if (!ATHLETE_TYPE.equals(athlete.getRole().getRoleName())) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un athlète");
        }
        
        // Vérifier qu'il n'y a pas déjà des équipes inscrites
        if (participateAtRepository.hasTeamParticipation(trialId)) {
            throw new IllegalArgumentException("Cette épreuve est réservée aux équipes");
        }
        
        // Vérifier que l'athlète n'est pas déjà inscrit
        if (isConvenedToRepository.findByTrialIdAndUserId(trialId, athleteId).isPresent()) {
            throw new IllegalArgumentException("L'athlète est déjà inscrit à cette épreuve");
        }
        
        // Créer l'inscription
        IsConvenedTo inscription = new IsConvenedTo();
        IsConvenedToId id = new IsConvenedToId(athleteId, trialId);
        inscription.setId(id);
        inscription.setUser(athlete);
        inscription.setTrial(trial);
        inscription.setIsForfeit(false);
        
        isConvenedToRepository.save(inscription);
        
        return createAthleteParticipantDTO(athlete, false);
    }

    @Override
    @Transactional
    public ParticipantDTO addTeamToTrial(Integer trialId, Integer teamId) {
        Trial trial = trialRepository.findById(trialId)
                .orElseThrow(() -> new IllegalArgumentException(TRIAL_NOT_FOUND));
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Équipe non trouvée"));
        
        // Vérifier qu'il n'y a pas déjà des athlètes inscrits individuellement
        if (isConvenedToRepository.hasAthleteParticipation(trialId)) {
            throw new IllegalArgumentException("Cette épreuve est réservée aux athlètes individuels");
        }
        
        // Vérifier que l'équipe n'est pas déjà inscrite
        if (participateAtRepository.findByTrialIdAndTeamId(trialId, teamId).isPresent()) {
            throw new IllegalArgumentException("L'équipe est déjà inscrite à cette épreuve");
        }
        
        // Créer l'inscription
        ParticipateAt inscription = new ParticipateAt();
        ParticipateAtId id = new ParticipateAtId(teamId, trialId);
        inscription.setId(id);
        inscription.setTeam(team);
        inscription.setTrial(trial);
        inscription.setIsForfeit(false);
        
        participateAtRepository.save(inscription);
        
        return createTeamParticipantDTO(team, false);
    }

    @Override
    @Transactional
    public ParticipantDTO forfeitAthlete(Integer trialId, Integer athleteId) {
        IsConvenedTo inscription = isConvenedToRepository.findByTrialIdAndUserId(trialId, athleteId)
                .orElseThrow(() -> new IllegalArgumentException(ATHLETE_NOT_REGISTERED));
        
        inscription.setIsForfeit(true);
        isConvenedToRepository.save(inscription);
        
        return createAthleteParticipantDTO(inscription.getUser(), true);
    }

    @Override
    @Transactional
    public ParticipantDTO forfeitTeam(Integer trialId, Integer teamId) {
        ParticipateAt inscription = participateAtRepository.findByTrialIdAndTeamId(trialId, teamId)
                .orElseThrow(() -> new IllegalArgumentException(TEAM_NOT_REGISTERED));
        
        inscription.setIsForfeit(true);
        participateAtRepository.save(inscription);
        
        return createTeamParticipantDTO(inscription.getTeam(), true);
    }

    @Override
    @Transactional
    public ParticipantDTO unforfeitAthlete(Integer trialId, Integer athleteId) {
        IsConvenedTo inscription = isConvenedToRepository.findByTrialIdAndUserId(trialId, athleteId)
                .orElseThrow(() -> new IllegalArgumentException(ATHLETE_NOT_REGISTERED));
        
        inscription.setIsForfeit(false);
        isConvenedToRepository.save(inscription);
        
        return createAthleteParticipantDTO(inscription.getUser(), false);
    }

    @Override
    @Transactional
    public ParticipantDTO unforfeitTeam(Integer trialId, Integer teamId) {
        ParticipateAt inscription = participateAtRepository.findByTrialIdAndTeamId(trialId, teamId)
                .orElseThrow(() -> new IllegalArgumentException(TEAM_NOT_REGISTERED));
        
        inscription.setIsForfeit(false);
        participateAtRepository.save(inscription);
        
        return createTeamParticipantDTO(inscription.getTeam(), false);
    }

    @Override
    @Transactional
    public void removeAthleteFromTrial(Integer trialId, Integer athleteId) {
        IsConvenedTo inscription = isConvenedToRepository.findByTrialIdAndUserId(trialId, athleteId)
                .orElseThrow(() -> new IllegalArgumentException(ATHLETE_NOT_REGISTERED));
        
        isConvenedToRepository.delete(inscription);
    }

    @Override
    @Transactional
    public void removeTeamFromTrial(Integer trialId, Integer teamId) {
        ParticipateAt inscription = participateAtRepository.findByTrialIdAndTeamId(trialId, teamId)
                .orElseThrow(() -> new IllegalArgumentException(TEAM_NOT_REGISTERED));
        
        participateAtRepository.delete(inscription);
    }

    @Override
    public List<TrialParticipantsDTO> getTrialsForCommissaire() {
        // Récupérer l'email du commissaire connecté
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Récupérer l'utilisateur commissaire
        ApplicationUser commissaire = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Commissaire non trouvé"));
        
        // Ne retourner que les épreuves assignées à ce commissaire et non terminées
        return trialRepository.findActiveTrialsAssignedToUser(commissaire.getId(), LocalDateTime.now()).stream()
                .map(trial -> getTrialParticipants(trial.getId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional
    public ParticipantDTO athleteDeclareWithdrawal(Integer trialId, String athleteEmail) {
        // Récupérer l'athlète authentifié
        ApplicationUser athlete = userRepository.findByEmail(athleteEmail)
                .orElseThrow(() -> new IllegalArgumentException("Athlète non trouvé"));
        
        // Récupérer l'épreuve
        Trial trial = trialRepository.findById(trialId)
                .orElseThrow(() -> new IllegalArgumentException(TRIAL_NOT_FOUND));
        
        // Vérifier que l'épreuve n'est pas déjà terminée
        if (trial.getTimeSlot().getEnd().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Impossible de déclarer forfait : l'épreuve est déjà terminée");
        }
        
        // Vérifier que l'athlète est inscrit à cette épreuve
        IsConvenedTo inscription = isConvenedToRepository.findByTrialIdAndUserId(trialId, athlete.getId())
                .orElseThrow(() -> new IllegalArgumentException("Vous n'êtes pas inscrit à cette épreuve"));
        
        // Vérifier qu'il n'est pas déjà forfait
        if (inscription.getIsForfeit() != null && inscription.getIsForfeit()) {
            throw new IllegalStateException("Vous êtes déjà déclaré forfait pour cette épreuve");
        }
        
        // Déclarer forfait
        inscription.setIsForfeit(true);
        isConvenedToRepository.save(inscription);
        
        return createAthleteParticipantDTO(athlete, true);
    }

    // ===== Méthodes privées =====
    
    private List<ParticipantDTO> getTeamParticipants(Integer trialId) {
        return participateAtRepository.findByTrialIdOrderedByResult(trialId).stream()
                .map(p -> createTeamParticipantDTO(p.getTeam(), 
                        p.getIsForfeit() != null && p.getIsForfeit()))
                .toList();
    }
    
    private List<ParticipantDTO> getAthleteParticipants(Integer trialId) {
        return isConvenedToRepository.findByTrialIdOrderedByResult(trialId).stream()
                .map(i -> createAthleteParticipantDTO(i.getUser(), 
                        i.getIsForfeit() != null && i.getIsForfeit()))
                .toList();
    }
    
    private List<PotentialParticipantDTO> getPotentialTeams(Integer trialId) {
        return teamRepository.findTeamsNotInTrial(trialId).stream()
                .map(this::createPotentialTeamDTO)
                .toList();
    }
    
    private List<PotentialParticipantDTO> getPotentialAthletes(Integer trialId) {
        return userRepository.findAthletesNotInTrial(trialId).stream()
                .map(this::createPotentialAthleteDTO)
                .toList();
    }
    
    private ParticipantDTO createTeamParticipantDTO(Team team, boolean isForfeit) {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setType(TEAM_TYPE);
        dto.setCountry(team.getCountry() != null ? team.getCountry().getCode() : null);
        dto.setForfeit(isForfeit);
        return dto;
    }
    
    private ParticipantDTO createAthleteParticipantDTO(ApplicationUser user, boolean isForfeit) {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setId(user.getId());
        dto.setName(user.getName() + " " + user.getLastname());
        dto.setType(ATHLETE_TYPE);
        dto.setCountry(user.getCountry() != null ? user.getCountry().getCode() : null);
        dto.setForfeit(isForfeit);
        return dto;
    }
    
    private PotentialParticipantDTO createPotentialTeamDTO(Team team) {
        PotentialParticipantDTO dto = new PotentialParticipantDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setType(TEAM_TYPE);
        dto.setCountry(team.getCountry() != null ? team.getCountry().getCode() : null);
        return dto;
    }
    
    private PotentialParticipantDTO createPotentialAthleteDTO(ApplicationUser user) {
        PotentialParticipantDTO dto = new PotentialParticipantDTO();
        dto.setId(user.getId());
        dto.setName(user.getName() + " " + user.getLastname());
        dto.setType(ATHLETE_TYPE);
        dto.setCountry(user.getCountry() != null ? user.getCountry().getCode() : null);
        return dto;
    }
}
