package com.miage.pouleAPI.services;

import com.miage.pouleAPI.adapters.TrialAdapter;
import com.miage.pouleAPI.dtos.trial.AssignedTrialsResponseDTO;
import com.miage.pouleAPI.dtos.trial.TrialDetailDTO;
import com.miage.pouleAPI.dtos.trial.TrialSummaryDTO;
import com.miage.pouleAPI.entity.IsConvenedTo;
import com.miage.pouleAPI.entity.Trial;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.IsConvenedToRepository;
import com.miage.pouleAPI.repositories.TrialRepository;
import com.miage.pouleAPI.services.interfaces.TrialService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrialServiceImpl implements TrialService {
    
    private TrialRepository trialRepository;
    private ApplicationUserRepository userRepository;
    private TrialAdapter trialAdapter;
    private IsConvenedToRepository isConvenedToRepository;

    @Autowired
    public TrialServiceImpl(TrialRepository trialRepository, ApplicationUserRepository userRepository, 
                           TrialAdapter trialAdapter, IsConvenedToRepository isConvenedToRepository) {
        this.trialRepository = trialRepository;
        this.userRepository = userRepository;
        this.trialAdapter = trialAdapter;
        this.isConvenedToRepository = isConvenedToRepository;
    }
    
    @Override
    public List<TrialSummaryDTO> getAllTrials() {
        return trialAdapter.entityListToSummaryDtoList(
            trialRepository.findAll()
        );
    }
    
    @Override
    public Optional<TrialDetailDTO> getTrialById(Integer id) {
        return trialRepository.findById(id)
            .map(trialAdapter::entityToDetailDto);
    }
    
    @Override
    public List<TrialSummaryDTO> getTrialsByChampionshipAndCompetition(Integer championshipId, Integer competitionId) {
        return trialAdapter.entityListToSummaryDtoList(
            trialRepository.findByCompetitionId(competitionId)
        );
    }

    @Override
    public Optional<AssignedTrialsResponseDTO> getAssignedTrialsForUserEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> buildAssignedTrialsResponse(user.getId()));
    }


    private AssignedTrialsResponseDTO buildAssignedTrialsResponse(Integer userId) {
        // Récupérer les trials solo avec les informations de forfait
        List<Trial> soloTrialEntities = trialRepository.findSoloTrialsByUserId(userId);
        List<TrialSummaryDTO> soloTrials = soloTrialEntities.stream()
            .map(trial -> {
                Boolean isForfeit = isConvenedToRepository
                    .findByTrialIdAndUserId(trial.getId(), userId)
                    .map(IsConvenedTo::getIsForfeit)
                    .orElse(false);
                return new TrialSummaryDTO(
                    trial.getId(),
                    trial.getId(),
                    trial.getName(),
                    trial.getDescription(),
                    isForfeit
                );
            })
            .toList();

        // Pour les trials d'équipe, on garde l'ancienne logique pour l'instant
        List<TrialSummaryDTO> teamTrials = trialRepository.findTeamTrialsByUserId(userId).stream()
            .map(trial -> new TrialSummaryDTO(
                trial.getId(),
                trial.getId(),
                trial.getName(),
                trial.getDescription(),
                false  // Pas de gestion de forfait pour les équipes pour l'instant
            ))
            .toList();

        return new AssignedTrialsResponseDTO(soloTrials, teamTrials);
    }
}
