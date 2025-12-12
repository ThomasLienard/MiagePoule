package com.miage.pouleAPI.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Place")
public class Place {

    @Id
    @Column(name = "id_place")
    private Integer id;

    @Column(name = "name_place")
    private String name;

    @Column(name = "city_place")
    private String city;

    @Column(name = "zip_code_place")
    private String zip;

    @Column(name = "street_place")
    private String street;

    @Column(name = "parking_place", nullable = false)
    private Boolean parking;

    @Column(name = "number_place")
    private String number;

    @Column(name = "description_place", length = 1500)
    private String description;

    @Column(name = "latitude_place")
    private BigDecimal latitude;

    @Column(name = "longitude_place")
    private BigDecimal longitude;
}
