package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.IsConvenedTo;
import com.miage.pouleAPI.entity.IsConvenedToId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IsConvenedToRepository extends JpaRepository<IsConvenedTo, IsConvenedToId> {
    @Query("SELECT i FROM IsConvenedTo i WHERE i.trial.id = :trialId ORDER BY i.result ASC")
    List<IsConvenedTo> findByTrialIdOrderedByResult(@Param("trialId") Integer trialId);

    @Query("SELECT i FROM IsConvenedTo i WHERE i.trial.id = :trialId AND i.user.id = :userId")
    Optional<IsConvenedTo> findByTrialIdAndUserId(@Param("trialId") Integer trialId, @Param("userId") Integer userId);

    @Query("SELECT COUNT(i) > 0 FROM IsConvenedTo i WHERE i.trial.id = :trialId")
    boolean hasAthleteParticipation(@Param("trialId") Integer trialId);
}
