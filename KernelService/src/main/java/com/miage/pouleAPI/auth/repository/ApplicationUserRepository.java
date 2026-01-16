package com.miage.pouleAPI.auth.repository;

import com.miage.pouleAPI.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Integer> {
    Optional<ApplicationUser> findByEmail(String email);

    @Query("SELECT MAX(u.id) FROM ApplicationUser u")
    Integer findMaxId();

    boolean existsByEmail(String email);
}