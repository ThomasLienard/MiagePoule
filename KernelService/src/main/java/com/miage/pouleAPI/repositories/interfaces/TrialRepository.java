package com.miage.pouleAPI.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.miage.pouleAPI.entity.Trial;

@Repository
public interface TrialRepository extends JpaRepository<Trial, Integer> {
}

