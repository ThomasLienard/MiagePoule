package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Integer> {
    interface RoleCountProjection {
        String getRoleName();
        Long getCount();
    }

    Optional<ApplicationUser> findByEmail(String email);

    boolean existsByEmail(String email);
    Optional<ApplicationUser> findByIdAndIsActiveTrue(Integer id);
    
    @Query("SELECT u FROM ApplicationUser u WHERE u.role.roleName = 'ATHLETE' AND u.isActive = true " +
           "AND u.id NOT IN (SELECT i.user.id FROM IsConvenedTo i WHERE i.trial.id = :trialId)")
    List<ApplicationUser> findAthletesNotInTrial(@Param("trialId") Integer trialId);
    
    @Query("SELECT u FROM ApplicationUser u JOIN IsConvenedTo i ON u.id = i.user.id WHERE i.trial.id = :trialId")
    List<ApplicationUser> findAthletesByTrialId(@Param("trialId") Integer trialId);

    @Query("SELECT COUNT(u) FROM ApplicationUser u WHERE u.createdAt >= :start AND u.createdAt < :end")
    long countCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(u) FROM ApplicationUser u WHERE u.lastLoginAt >= :start AND u.lastLoginAt < :end")
    long countConnectedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT u.role.roleName AS roleName, COUNT(u) AS count FROM ApplicationUser u GROUP BY u.role.roleName")
    List<RoleCountProjection> countUsersByRole();
}
