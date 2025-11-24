package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Championship")
public class Championship {

    @Id
    @Column(name = "id_championship")
    private Integer id;

    @Column(name = "description_championship", length = 1500)
    private String description;

    @Column(name = "name_championship", nullable = false)
    private String name;

}
