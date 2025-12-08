package com.miage.pouleAPI.repositories.adapters;

import com.miage.pouleAPI.domains.ChampionshipModel;
import com.miage.pouleAPI.domains.ports.ChampionshipPort;
import com.miage.pouleAPI.entity.Championship;
import com.miage.pouleAPI.repositories.ChampionshipRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChampionshipJpaAdapter implements ChampionshipPort {

    private final ChampionshipRepository repository;

    public ChampionshipJpaAdapter(ChampionshipRepository repository){
        this.repository=repository;
    }


    @Override
    public List<ChampionshipModel> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<ChampionshipModel> findById(Integer id) {
        return Optional.empty();
    }

    @Override
    public ChampionshipModel save(ChampionshipModel championshipModel) {
        return null;
    }

    private ChampionshipModel toDomain(Championship championship){
        if (championship == null){
            return  null;
        }
        return new ChampionshipModel(
                championship.getDescription(),
                championship.getEnd(),
                championship.getId(),
                championship.getName(),
                championship.getStart());
    }

    private Championship toEntity(ChampionshipModel championship){
        if (championship == null){
            return  null;
        }
        return new Championship(
                championship.getId(),
                championship.getDescription(),
                championship.getName(),
                championship.getStart(),
                championship.getEnd());
    }
}
