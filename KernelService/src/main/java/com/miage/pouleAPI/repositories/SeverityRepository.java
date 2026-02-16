package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.Severity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeverityRepository extends JpaRepository<Severity, String> {
}
