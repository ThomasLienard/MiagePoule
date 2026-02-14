package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.ParticipateAt;
import com.miage.pouleAPI.entity.ParticipateAtId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipateAtRepository extends JpaRepository<ParticipateAt, ParticipateAtId> {
    @Query("SELECT p FROM ParticipateAt p WHERE p.trial.id = :trialId ORDER BY p.result ASC")
    List<ParticipateAt> findByTrialIdOrderedByResult(@Param("trialId") Integer trialId);

    @Query("SELECT p FROM ParticipateAt p WHERE p.trial.id = :trialId AND p.team.id = :teamId")
    Optional<ParticipateAt> findByTrialIdAndTeamId(@Param("trialId") Integer trialId, @Param("teamId") Integer teamId);

    @Query("SELECT COUNT(p) > 0 FROM ParticipateAt p WHERE p.trial.id = :trialId")
    boolean hasTeamParticipation(@Param("trialId") Integer trialId);
    
    @Modifying
    @Query("DELETE FROM ParticipateAt p WHERE p.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Integer teamId);

    @Query("SELECT p FROM ParticipateAt p WHERE p.trial.id = :trialId")
    List<ParticipateAt> findByTrialId(@Param("trialId") Integer trialId);
}
