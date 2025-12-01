package com.miage.pouleAPI.repositories.adapters;

import com.miage.pouleAPI.domains.CompetitionModel;
import com.miage.pouleAPI.domains.ports.CompetitionPort;
import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.repositories.CompetitionRepository;

import java.util.List;
import java.util.Optional;

public class CompetitionJpaAdapter implements CompetitionPort {

    private final CompetitionRepository repository;

    public CompetitionJpaAdapter(CompetitionRepository repository){
        this.repository=repository;
    }


    @Override
    public List<CompetitionModel> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<CompetitionModel> findById(Integer id) {
        return Optional.empty();
    }

    @Override
    public CompetitionModel save(CompetitionModel competitionEntity) {
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
                competition.getId(),
                competition.getName(),
                competition.getDescription(),
                competition.getChampionship(),
                competition.getEnd(),
                competition.getStart());
    }
}
