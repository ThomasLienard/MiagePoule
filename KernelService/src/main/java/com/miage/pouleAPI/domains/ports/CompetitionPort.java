package com.miage.pouleAPI.domains.ports;

import com.miage.pouleAPI.domains.CompetitionModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface CompetitionPort {
    List<CompetitionModel> findAll();
    Optional<CompetitionModel> findById(Integer id);
    CompetitionModel save (CompetitionModel competitionEntity);
    List<CompetitionModel> findByChampionshipId(Integer idChampionship);

}
