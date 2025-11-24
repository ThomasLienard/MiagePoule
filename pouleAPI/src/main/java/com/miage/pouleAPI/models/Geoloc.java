package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "geoloc")
public class Geoloc {

    @Id
    @Column(name = "id_geoloc")
    private Integer id;

    @Column(name = "latitude_geoloc")
    private BigDecimal latitude;

    @Column(name = "longitude_geoloc")
    private BigDecimal longitude;

}
