package com.miage.pouleAPI.services;

import com.miage.pouleAPI.adapters.ChampionshipJpaAdapter;
import com.miage.pouleAPI.dtos.championship.ChampionshipDTO;
import com.miage.pouleAPI.dtos.championship.CreateChampionshipRequestDTO;
import com.miage.pouleAPI.services.interfaces.ChampionshipService;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChampionshipServiceImpl implements ChampionshipService {

    private final ChampionshipJpaAdapter championshipJpaAdapter;

    public ChampionshipServiceImpl(ChampionshipJpaAdapter championshipJpaAdapter) {
        this.championshipJpaAdapter = championshipJpaAdapter;
    }

    @Override
    public List<ChampionshipDTO> findAll() {
        return championshipJpaAdapter.findAll();
    }

    @Override
    public Optional<ChampionshipDTO> findById(Integer id) {
        return championshipJpaAdapter.findById(id);
    }

    @Override
    public ChampionshipDTO save(CreateChampionshipRequestDTO championshipDto) {
        return championshipJpaAdapter.save(championshipDto);
    }
}
