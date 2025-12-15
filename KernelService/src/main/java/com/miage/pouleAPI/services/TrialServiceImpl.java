package com.miage.pouleAPI.services;

import com.miage.pouleAPI.adapter.TrialAdapter;
import com.miage.pouleAPI.dto.trial.TrialDetailDTO;
import com.miage.pouleAPI.dto.trial.TrialSummaryDTO;
import com.miage.pouleAPI.repositories.interfaces.TrialRepository;
import com.miage.pouleAPI.services.interfaces.TrialService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrialServiceImpl implements TrialService {
    
    @Autowired
    private TrialRepository trialRepository;
    
    @Autowired
    private TrialAdapter trialAdapter;
    
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
}
