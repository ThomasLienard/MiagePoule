package com.miage.pouleAPI.services;

import com.miage.pouleAPI.entity.Severity;
import org.springframework.data.jpa.repository.JpaRepository;

interface SeverityRepository extends JpaRepository<Severity, String> {
}
