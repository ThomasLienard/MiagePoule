package com.miage.pouleAPI.adapters;

import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
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


    public List<CompetitionDTO> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    public Optional<CompetitionDTO> findById(Integer id) {
        return repository.findById(id).map(this::toDomain);
    }

    public CompetitionDTO save(CompetitionDTO model) {
        Competition entity = toEntity(model);
        Competition saved = repository.save(entity);
        return toDomain(saved);
    }

    public List<CompetitionDTO> findByChampionshipId(Integer championshipId) {
        return repository.findByChampionship_Id(championshipId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private CompetitionDTO toDomain(Competition competition){
        if (competition == null){
            return  null;
        }
        return new CompetitionDTO(
                competition.getChampionship().getId(),
                competition.getDescription(),
                competition.getEnd(),
                competition.getId(),
                competition.getName(),
                competition.getStart());
    }

    private Competition toEntity(CompetitionDTO competition) {
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
