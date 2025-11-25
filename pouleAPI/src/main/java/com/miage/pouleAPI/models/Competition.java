package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

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

    @Column(name = "start_date_competition", nullable = false)
    private LocalDate start;

    @Column(name = "end_date_competition", nullable = false)
    private LocalDate end;

}
