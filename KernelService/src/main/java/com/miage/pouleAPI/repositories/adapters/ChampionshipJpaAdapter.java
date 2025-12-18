package com.miage.pouleAPI.repositories.adapters;

import com.miage.pouleAPI.dtos.championship.ChampionshipDTO;
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


    public List<ChampionshipDTO> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    public Optional<ChampionshipDTO> findById(Integer id) {
        return repository.findById(id).map(this::toDomain);
    }

    public ChampionshipDTO save(ChampionshipDTO championshipDto) {
        Championship entity = toEntity(championshipDto);
        Championship saved = repository.save(entity);
        return toDomain(saved);
    }

    private ChampionshipDTO toDomain(Championship championship){
        if (championship == null){
            return  null;
        }
        return new ChampionshipDTO(
                championship.getDescription(),
                championship.getEnd(),
                championship.getId(),
                championship.getName(),
                championship.getStart());
    }

    private Championship toEntity(ChampionshipDTO championship){
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
