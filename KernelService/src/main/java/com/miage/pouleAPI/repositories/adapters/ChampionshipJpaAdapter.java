package com.miage.pouleAPI.repositories.adapters;

import com.miage.pouleAPI.domains.ChampionshipModel;
import com.miage.pouleAPI.entity.Championship;
import com.miage.pouleAPI.repositories.ChampionshipRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChampionshipJpaAdapter{

    private final ChampionshipRepository repository;

    public ChampionshipJpaAdapter(ChampionshipRepository repository){
        this.repository=repository;
    }


    public List<ChampionshipModel> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    public Optional<ChampionshipModel> findById(Integer id) {
        return repository.findById(id).map(this::toDomain);
    }

    public ChampionshipModel save(ChampionshipModel championshipModel) {
        Championship entity = toEntity(championshipModel);
        Championship saved = repository.save(entity);
        return toDomain(saved);
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
