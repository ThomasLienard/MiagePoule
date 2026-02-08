package com.miage.pouleAPI.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Team")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_team")
    private Integer id;

    @Column(name = "name_team", unique = true, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "Country_code", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Country country;

    @ManyToMany(mappedBy = "teams")
    @JsonIgnoreProperties({"teams", "events", "geolocs", "dailyTasks", "notifications", "metrics", "password"})
    private Set<ApplicationUser> users = new HashSet<>();
}
