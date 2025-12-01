package com.miage.pouleAPI.domains;

import com.miage.pouleAPI.models.Championship;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CompetitionModel {
    private Integer id;

    private String name;

    private String description;

    private Championship championship;

    private LocalDate start;

    private LocalDate end;

    public CompetitionModel(Championship championship, String description, LocalDate end, Integer id, String name, LocalDate start) {
        this.championship = championship;
        this.description = description;
        this.end = end;
        this.id = id;
        this.name = name;
        this.start = start;
    }
}
