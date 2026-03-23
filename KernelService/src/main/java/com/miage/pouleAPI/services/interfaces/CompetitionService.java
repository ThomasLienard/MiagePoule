package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import com.miage.pouleAPI.dtos.competition.CreateCompetitionRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CompetitionService {
    List<CompetitionDTO> findAll();
    Optional<CompetitionDTO> findById(Integer id);
    CompetitionDTO save (CreateCompetitionRequestDTO competition);
    List<CompetitionDTO> findByChampionship(Integer championshipId);
    CompetitionDTO update (CompetitionDTO competitionDTO);
    Optional<CompetitionDTO> findByName(String name);
    void addObserverToCompetition(Integer competitionId, Integer userId);
    void removeObserverFromCompetition(Integer competitionId, Integer userId);
}
