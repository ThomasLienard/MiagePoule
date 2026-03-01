package com.miage.pouleAPI.services;

import com.miage.pouleAPI.adapters.CompetitionJpaAdapter;
import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import com.miage.pouleAPI.dtos.competition.CreateCompetitionRequestDTO;
import com.miage.pouleAPI.services.interfaces.CompetitionService;

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
    public List<CompetitionDTO> findAll() {
        return competitionJpaAdapter.findAll();
    }

    @Override
    public Optional<CompetitionDTO> findById(Integer id) {
        return competitionJpaAdapter.findById(id);
    }

    @Override
    public CompetitionDTO save(CreateCompetitionRequestDTO competition) {
        return competitionJpaAdapter.save(competition);
    }

    @Override
    public CompetitionDTO update(CompetitionDTO competitionDTO) {
        return competitionJpaAdapter.update(competitionDTO);
    }

    @Override
    public List<CompetitionDTO> findByChampionship(Integer championshipId) {
        return competitionJpaAdapter.findByChampionshipId(championshipId);


    public void addObserverToCompetition(Integer competitionId, Integer userId) {
        competitionJpaAdapter.addObserver(competitionId, userId);
    }

    public void removeObserverFromCompetition(Integer competitionId, Integer userId) {
        competitionJpaAdapter.removeObserver(competitionId, userId);
    }
    @Override
    public Optional<CompetitionDTO> findByName(String name) {
        return competitionJpaAdapter.findByName(name);
    }


    public List<Integer> getCompetitionObservers(Integer competitionId) {
        return competitionJpaAdapter.getObserverIds(competitionId);
    }
}
