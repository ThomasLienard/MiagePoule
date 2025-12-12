package com.miage.pouleAPI.services.interfaces;

import java.util.List;
import java.util.Optional;

import com.miage.pouleAPI.entity.Trial;

public interface TrialService {

    public List<Trial> getAllTrials() ;

    Optional<Trial> getTrialById(Integer id);
    
}