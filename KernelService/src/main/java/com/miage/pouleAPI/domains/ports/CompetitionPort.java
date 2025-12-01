package com.miage.pouleAPI.domains.ports;

import com.miage.pouleAPI.domains.CompetitionModel;

import java.util.List;
import java.util.Optional;

public interface CompetitionPort {
    List<CompetitionModel> findAll();
    Optional<CompetitionModel> findById(Integer id);
    CompetitionModel save (CompetitionModel competitionEntity);
}
