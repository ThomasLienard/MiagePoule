package com.miage.pouleAPI.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @ManyToMany(mappedBy = "teams")
    private Set<ApplicationUser> users = new HashSet<>();

}
