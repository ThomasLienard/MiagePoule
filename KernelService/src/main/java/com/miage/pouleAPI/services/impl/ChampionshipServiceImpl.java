package com.miage.pouleAPI.services.impl;

import com.miage.pouleAPI.domains.ChampionshipModel;
import com.miage.pouleAPI.domains.ports.ChampionshipPort;
import com.miage.pouleAPI.services.ChampionshipService;

import java.util.List;
import java.util.Optional;

public class ChampionshipServiceImpl implements ChampionshipService {

    private ChampionshipPort championshipPort;

    public ChampionshipServiceImpl(ChampionshipPort championshipPort) {
        this.championshipPort = championshipPort;
    }

    @Override
    public List<ChampionshipModel> findAll() {
        return championshipPort.findAll();
    }

    @Override
    public Optional<ChampionshipModel> findById(Integer id) {
        return championshipPort.findById(id);
    }

    @Override
    public ChampionshipModel save(ChampionshipModel championshipModel) {
        return championshipPort.save(championshipModel);
    }
}
