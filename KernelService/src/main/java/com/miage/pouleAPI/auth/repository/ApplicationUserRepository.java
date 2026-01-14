package com.miage.pouleAPI.auth.repository;

import com.miage.pouleAPI.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Integer> {
    Optional<ApplicationUser> findByEmail(String email);
}