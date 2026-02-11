package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.TypeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeEventRepository extends JpaRepository<TypeEvent, String> {
}