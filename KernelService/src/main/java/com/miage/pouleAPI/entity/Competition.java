package com.miage.pouleAPI.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Competition")
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
