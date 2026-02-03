package com.miage.pouleAPI.services;

import com.miage.pouleAPI.adapters.TrialAdapter;
import com.miage.pouleAPI.dtos.trial.AssignedTrialsResponseDTO;
import com.miage.pouleAPI.dtos.trial.TrialDetailDTO;
import com.miage.pouleAPI.dtos.trial.TrialSummaryDTO;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
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

    @Autowired
    public TrialServiceImpl(TrialRepository trialRepository, ApplicationUserRepository userRepository, TrialAdapter trialAdapter) {
        this.trialRepository = trialRepository;
        this.userRepository = userRepository;
        this.trialAdapter = trialAdapter;
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
        List<TrialSummaryDTO> soloTrials = trialAdapter.entityListToSummaryDtoList(
            trialRepository.findSoloTrialsByUserId(userId)
        );

        List<TrialSummaryDTO> teamTrials = trialAdapter.entityListToSummaryDtoList(
            trialRepository.findTeamTrialsByUserId(userId)
        );

        return new AssignedTrialsResponseDTO(soloTrials, teamTrials);
    }
}
