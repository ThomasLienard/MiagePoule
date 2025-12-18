package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CompetitionService {
    List<CompetitionDTO> findAll();
    Optional<CompetitionDTO> findById(Integer id);
    CompetitionDTO save (CompetitionDTO competition);
    List<CompetitionDTO> findByChampionship(Integer championshipId);
}
