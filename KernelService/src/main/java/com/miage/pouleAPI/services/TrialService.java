package main.java.com.miage.pouleAPI.services;

import java.util.List;

import com.miage.pouleAPI.entity.Trial;

import main.java.com.miage.pouleAPI.repositories.TrialRepository;

public interface TrialService {

    public List<Trial> getAllTrials() ;
    

}