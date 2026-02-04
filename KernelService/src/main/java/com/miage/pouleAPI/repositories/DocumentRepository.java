package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {

    List<Document> findByUserId(Integer userId);

    @Query("SELECT d FROM Document d WHERE d.user.id = :userId AND d.type.name = :typeName ORDER BY d.uploadedAt DESC")
    List<Document> findByUserIdAndTypeName(@Param("userId") Integer userId, @Param("typeName") String typeName);

    @Query("SELECT d FROM Document d WHERE d.user.id = :userId AND d.id = :documentId")
    Optional<Document> findByUserIdAndId(@Param("userId") Integer userId, @Param("documentId") Integer documentId);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.user.id = :userId")
    long countByUserId(@Param("userId") Integer userId);
}