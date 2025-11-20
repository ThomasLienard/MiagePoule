package com.miage.pouleAPI.model;

import jakarta.persistence.*;

@Entity
public class Severity {
    @Id
    private String nameSeverity;
    private String descSeverity;
    // getters and setters
}