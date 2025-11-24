package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Competition")
public class Competition {

    @Id
    @Column(name = "id_competition")
    private Integer id;

    @Column(name = "name_competition", nullable = false)
    private String name;

    @Column(name = "description_competition")
    private String description;

    @ManyToOne
    @JoinColumn(name = "id_championship", nullable = false)
    private Championship championship;

}
