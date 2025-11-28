package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Team")
public class Team {

    @Id
    @Column(name = "id_team")
    private Integer id;

    @Column(name = "name_team", unique = true, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "Country_code", nullable = false)
    private Country country;

}
