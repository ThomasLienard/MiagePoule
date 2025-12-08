package com.miage.pouleAPI.repositories.adapters;

import com.miage.pouleAPI.domains.CompetitionModel;
import com.miage.pouleAPI.domains.ports.CompetitionPort;
import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.repositories.CompetitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
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
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public CompetitionModel save(CompetitionModel model) {
        Competition entity = toEntity(model);
        Competition saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<CompetitionModel> findByChampionshipId(Integer championshipId) {
        return repository.findByChampionshipId(championshipId)
                .stream()
                .map(this::toDomain)
                .toList();
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
