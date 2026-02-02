package com.miage.pouleAPI.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.miage.pouleAPI.entity.Trial;

import java.util.List;

@Repository
public interface TrialRepository extends JpaRepository<Trial, Integer> {
    @Query("SELECT t FROM Trial t WHERE t.competition.id = :competitionId")
    List<Trial> findByCompetitionId(@Param("competitionId") Integer competitionId);
    
    @Query("SELECT t FROM Trial t " +
            "JOIN IsConvenedTo i ON t.id = i.trial.id " +
            "WHERE i.user.id = :userId " +
            "ORDER BY t.timeSlot.start")
    List<Trial> findSoloTrialsByUserId(@Param("userId") Integer userId);

    @Query("SELECT t FROM Trial t " +
            "JOIN ParticipateAt p ON t.id = p.trial.id " +
            "JOIN Team team ON p.team.id = team.id " +
            "JOIN ApplicationUser u ON u MEMBER OF team.users " +
            "WHERE u.id = :userId " +
            "ORDER BY t.timeSlot.start")
    List<Trial> findTeamTrialsByUserId(@Param("userId") Integer userId);
}

