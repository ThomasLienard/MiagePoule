package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Integer> {
    Optional<ApplicationUser> findByEmail(String email);

    boolean existsByEmail(String email);
    Optional<ApplicationUser> findByIdAndIsActiveTrue(Integer id);
}
