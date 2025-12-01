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

    public ChampionshipModel(String description, LocalDate end, Integer id, String name, LocalDate start) {
        this.description = description;
        this.end = end;
        this.id = id;
        this.name = name;
        this.start = start;
    }
}
