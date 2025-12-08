package com.miage.pouleAPI.services.impl;

import com.miage.pouleAPI.domains.CompetitionModel;
import com.miage.pouleAPI.domains.ports.CompetitionPort;
import com.miage.pouleAPI.services.CompetitionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompetitionServiceImpl implements CompetitionService {

    private CompetitionPort competitionPort;

    public CompetitionServiceImpl(CompetitionPort competitionPort) {
        this.competitionPort = competitionPort;
    }


    @Override
    public List<CompetitionModel> findAll() {
        return competitionPort.findAll();
    }

    @Override
    public Optional<CompetitionModel> findById(Integer id) {
        return competitionPort.findById(id);
    }

    @Override
    public CompetitionModel save(CompetitionModel competition) {
        return competitionPort.save(competition);
    }
}
