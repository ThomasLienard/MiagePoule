package com.miage.pouleAPI.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miage.pouleAPI.entity.Trial;
import com.miage.pouleAPI.repositories.interfaces.TrialRepository;
import com.miage.pouleAPI.services.interfaces.TrialService;



@Service
public class TrialServiceImpl implements  TrialService {
    
    @Autowired
    private TrialRepository trialRepository;
    
    public List<Trial> getAllTrials() {
        return trialRepository.findAll();
    }

    @Override
    public Optional<Trial> getTrialById(Integer id) {
        return trialRepository.findById(id);
    }

 

}
