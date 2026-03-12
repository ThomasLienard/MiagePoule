package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.TypeScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeScoreRepository extends JpaRepository<TypeScore, String> {
}
