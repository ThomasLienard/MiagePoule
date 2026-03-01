package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.dtos.championship.ChampionshipDTO;
import com.miage.pouleAPI.dtos.championship.CreateChampionshipRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ChampionshipService {
    List<ChampionshipDTO> findAll();
    Optional<ChampionshipDTO> findById(Integer id);
    ChampionshipDTO save (CreateChampionshipRequestDTO championship);
    ChampionshipDTO update (ChampionshipDTO championship);
}
