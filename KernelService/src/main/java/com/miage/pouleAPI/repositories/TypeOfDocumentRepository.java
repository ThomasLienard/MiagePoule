package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.TypeOfDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeOfDocumentRepository extends JpaRepository<TypeOfDocument, Integer> {
    Optional<TypeOfDocument> findByName(String name);
}