package com.miage.pouleAPI.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.miage.pouleAPI.entity.Trial;

import java.util.List;

@Repository
public interface TrialRepository extends JpaRepository<Trial, Integer> {
    @Query("SELECT t FROM Trial t WHERE t.event.competition.id = :competitionId")
    List<Trial> findByCompetitionId(@Param("competitionId") Integer competitionId);
}

