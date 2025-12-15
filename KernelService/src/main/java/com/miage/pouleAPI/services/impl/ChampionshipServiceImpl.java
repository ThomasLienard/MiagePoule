package com.miage.pouleAPI.services.impl;

import com.miage.pouleAPI.domains.ChampionshipModel;
import com.miage.pouleAPI.repositories.adapters.ChampionshipJpaAdapter;
import com.miage.pouleAPI.services.ChampionshipService;
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
    public List<ChampionshipModel> findAll() {
        return championshipJpaAdapter.findAll();
    }

    @Override
    public Optional<ChampionshipModel> findById(Integer id) {
        return championshipJpaAdapter.findById(id);
    }

    @Override
    public ChampionshipModel save(ChampionshipModel championshipModel) {
        return championshipJpaAdapter.save(championshipModel);
    }
}
