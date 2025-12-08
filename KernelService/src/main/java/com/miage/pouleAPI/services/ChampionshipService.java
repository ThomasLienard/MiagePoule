package com.miage.pouleAPI.services;

import com.miage.pouleAPI.domains.ChampionshipModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ChampionshipService {
    List<ChampionshipModel> findAll();
    Optional<ChampionshipModel> findById(Integer id);
    ChampionshipModel save (ChampionshipModel competition);
}
