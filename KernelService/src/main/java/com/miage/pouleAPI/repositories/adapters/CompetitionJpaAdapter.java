package com.miage.pouleAPI.repositories.adapters;

import com.miage.pouleAPI.domains.CompetitionModel;
import com.miage.pouleAPI.entity.Championship;
import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.repositories.ChampionshipRepository;
import com.miage.pouleAPI.repositories.CompetitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompetitionJpaAdapter {

    private final CompetitionRepository repository;
    private final ChampionshipRepository championshipRepository;

    public CompetitionJpaAdapter(CompetitionRepository repository, ChampionshipRepository championshipRepository){
        this.repository=repository;
        this.championshipRepository = championshipRepository;
    }


    public List<CompetitionModel> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    public Optional<CompetitionModel> findById(Integer id) {
        return repository.findById(id).map(this::toDomain);
    }

    public CompetitionModel save(CompetitionModel model) {
        Competition entity = toEntity(model);
        Competition saved = repository.save(entity);
        return toDomain(saved);
    }

    public List<CompetitionModel> findByChampionshipId(Integer championshipId) {
        return repository.findByChampionship_Id(championshipId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private CompetitionModel toDomain(Competition competition){
        if (competition == null){
            return  null;
        }
        return new CompetitionModel(
                competition.getChampionship().getId(),
                competition.getDescription(),
                competition.getEnd(),
                competition.getId(),
                competition.getName(),
                competition.getStart());
    }

    private Competition toEntity(CompetitionModel competition) {
        if (competition == null) {
            return null;
        }
        Championship championship = championshipRepository.findById(competition.getChampionshipId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Championship not found with id " + competition.getChampionshipId()
                ));

        return new Competition(
                competition.getId(),
                competition.getName(),
                competition.getDescription(),
                championship,
                competition.getStart(),
                competition.getEnd()
        );
    }
}
