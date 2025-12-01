package com.miage.pouleAPI.services;

import com.miage.pouleAPI.domains.ChampionshipModel;

import java.util.List;
import java.util.Optional;

public interface ChampionshipService {
    List<ChampionshipModel> findAll();
    Optional<ChampionshipModel> findById(Integer id);
    ChampionshipModel save (ChampionshipModel competition);
}
