package com.miage.pouleAPI.domains;

import com.miage.pouleAPI.entity.Championship;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CompetitionModel {
    private Integer id;

    private String name;

    private String description;

    private Integer championshipId;

    private LocalDate start;

    private LocalDate end;

    public CompetitionModel(Integer championshipId, String description, LocalDate end, Integer id, String name, LocalDate start) {
        this.championshipId = championshipId;
        this.description = description;
        this.end = end;
        this.id = id;
        this.name = name;
        this.start = start;
    }
}
