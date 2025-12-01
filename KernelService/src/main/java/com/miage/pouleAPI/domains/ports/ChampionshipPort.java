package com.miage.pouleAPI.domains.ports;

import com.miage.pouleAPI.domains.ChampionshipModel;
import com.miage.pouleAPI.entity.Championship;

import java.util.List;
import java.util.Optional;

public interface ChampionshipPort {
    List<ChampionshipModel> findAll();
    Optional<ChampionshipModel> findById(Integer id);
    ChampionshipModel save (ChampionshipModel championshipEntity);
}
