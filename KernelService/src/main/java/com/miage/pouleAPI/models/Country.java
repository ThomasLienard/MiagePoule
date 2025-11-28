package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Country")
public class Country {

    @Id
    @Column(name = "Country_code")
    private String code;

}
