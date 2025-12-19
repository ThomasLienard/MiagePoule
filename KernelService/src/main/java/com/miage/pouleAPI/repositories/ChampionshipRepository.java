package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.Championship;
import com.miage.pouleAPI.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChampionshipRepository extends JpaRepository<Championship, Integer> {

    List<Championship> findAll();
    Optional<Championship> findById(Integer id);
    Championship save (Championship competitionEntity);
}
