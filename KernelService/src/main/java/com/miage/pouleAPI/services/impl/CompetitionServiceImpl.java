package com.miage.pouleAPI.services.impl;

import com.miage.pouleAPI.domains.CompetitionModel;
import com.miage.pouleAPI.repositories.adapters.CompetitionJpaAdapter;
import com.miage.pouleAPI.services.CompetitionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompetitionServiceImpl implements CompetitionService {

    private CompetitionJpaAdapter competitionJpaAdapter;

    public CompetitionServiceImpl(CompetitionJpaAdapter competitionJpaAdapter) {
        this.competitionJpaAdapter = competitionJpaAdapter;
    }


    @Override
    public List<CompetitionModel> findAll() {
        return competitionJpaAdapter.findAll();
    }

    @Override
    public Optional<CompetitionModel> findById(Integer id) {
        return competitionJpaAdapter.findById(id);
    }

    @Override
    public CompetitionModel save(CompetitionModel competition) {
        return competitionJpaAdapter.save(competition);
    }

    @Override
    public List<CompetitionModel> findByChampionship(Integer championshipId) {
        return competitionJpaAdapter.findByChampionshipId(championshipId);


    }


}
