package com.miage.pouleAPI.domains.ports;

import com.miage.pouleAPI.models.Championship;

import java.util.List;
import java.util.Optional;

public interface ChampionshipPort {
    List<Championship> findAll();
    Optional<Championship> findById(Integer id);
    Championship save (Championship competitionEntity);
}
