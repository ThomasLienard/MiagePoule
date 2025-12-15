package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.dto.trial.TrialDetailDTO;
import com.miage.pouleAPI.dto.trial.TrialSummaryDTO;

import java.util.List;
import java.util.Optional;

public interface TrialService {
    List<TrialSummaryDTO> getAllTrials();
    Optional<TrialDetailDTO> getTrialById(Integer id);
}
