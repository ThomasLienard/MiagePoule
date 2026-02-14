package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {
    
    @Query("SELECT t FROM Team t WHERE t.id NOT IN " +
           "(SELECT p.team.id FROM ParticipateAt p WHERE p.trial.id = :trialId)")
    List<Team> findTeamsNotInTrial(@Param("trialId") Integer trialId);
    
    @Query("SELECT t FROM Team t JOIN ParticipateAt p ON t.id = p.team.id WHERE p.trial.id = :trialId")
    List<Team> findTeamsByTrialId(@Param("trialId") Integer trialId);
    
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.users u LEFT JOIN FETCH u.country")
    List<Team> findAllWithUsers();
    
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.users u LEFT JOIN FETCH u.country WHERE t.id = :id")
    java.util.Optional<Team> findByIdWithUsers(@Param("id") Integer id);
}
