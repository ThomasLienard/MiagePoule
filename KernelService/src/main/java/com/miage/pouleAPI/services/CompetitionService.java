package com.miage.pouleAPI.services;

import com.miage.pouleAPI.domains.CompetitionModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CompetitionService {
    List<CompetitionModel> findAll();
    Optional<CompetitionModel> findById(Integer id);
    CompetitionModel save (CompetitionModel competition);
    List<CompetitionModel> findByChampionship(Integer championshipId);
}
