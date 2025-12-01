package com.miage.pouleAPI.repositories.adapters;

import com.miage.pouleAPI.domains.ChampionshipModel;
import com.miage.pouleAPI.domains.CompetitionModel;
import com.miage.pouleAPI.domains.ports.ChampionshipPort;
import com.miage.pouleAPI.domains.ports.CompetitionPort;
import com.miage.pouleAPI.models.Competition;
import com.miage.pouleAPI.repositories.ChampionshipRepository;
import com.miage.pouleAPI.repositories.CompetitionRepository;

import java.util.List;
import java.util.Optional;

public class ChampionshipJpaAdapter implements ChampionshipPort {

    private final ChampionshipRepository repository;

    public ChampionshipJpaAdapter(ChampionshipRepository repository){
        this.repository=repository;
    }


    @Override
    public List<ChampionshipModel> findAll() {
        return repository.findAll().stream().map(this::toDomain);
    }

    @Override
    public Optional<ChampionshipModel> findById(Integer id) {
        return Optional.empty();
    }

    @Override
    public ChampionshipModel save(ChampionshipModel championshipModel) {
        return null;
    }

    private CompetitionModel toDomain(Competition competition){
        if (competition == null){
            return  null;
        }
        return new CompetitionModel(
                competition.getChampionship(),
                competition.getDescription(),
                competition.getEnd(),
                competition.getId(),
                competition.getName(),
                competition.getStart());
    }

    private Competition toEntity(CompetitionModel competition){
        if (competition == null){
            return  null;
        }
        return new Competition(
                competition.getChampionship(),
                competition.getDescription(),
                competition.getEnd(),
                competition.getId(),
                competition.getName(),
                competition.getStart());
    }
}
