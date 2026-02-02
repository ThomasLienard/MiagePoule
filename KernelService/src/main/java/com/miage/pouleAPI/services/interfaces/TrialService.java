package com.miage.pouleAPI.services.interfaces;

import java.util.List;
import java.util.Optional;

import com.miage.pouleAPI.dtos.trial.AssignedTrialsResponseDTO;
import com.miage.pouleAPI.dtos.trial.TrialDetailDTO;
import com.miage.pouleAPI.dtos.trial.TrialSummaryDTO;

public interface TrialService {
    List<TrialSummaryDTO> getAllTrials();
    Optional<TrialDetailDTO> getTrialById(Integer id);
    List<TrialSummaryDTO> getTrialsByChampionshipAndCompetition(Integer championshipId, Integer competitionId);
    AssignedTrialsResponseDTO getAssignedTrialsForUser(Integer userId);
}
