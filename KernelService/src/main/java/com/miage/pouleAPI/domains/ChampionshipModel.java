package com.miage.pouleAPI.domains;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ChampionshipModel {

    private Integer id;

    private String description;

    private String name;

    private LocalDate start;

    private LocalDate end;
}
