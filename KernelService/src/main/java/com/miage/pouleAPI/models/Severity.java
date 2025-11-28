package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Severity")
public class Severity {

    @Id
    @Column(name = "name_severity")
    private String name;

    @Column(name = "desc_severity", length = 250)
    private String description;

}
