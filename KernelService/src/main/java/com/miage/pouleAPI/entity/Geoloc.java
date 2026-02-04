package com.miage.pouleAPI.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "geoloc")
public class Geoloc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_geoloc")
    private Integer id;

    @Column(name = "latitude_geoloc")
    private BigDecimal latitude;

    @Column(name = "longitude_geoloc")
    private BigDecimal longitude;

    @ManyToMany(mappedBy = "geolocs")
    private Set<ApplicationUser> users = new HashSet<>();


}
