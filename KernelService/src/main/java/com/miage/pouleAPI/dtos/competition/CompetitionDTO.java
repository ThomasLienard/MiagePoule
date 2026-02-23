package com.miage.pouleAPI.dtos.competition;

import com.miage.pouleAPI.entity.ApplicationUser;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class CompetitionDTO {
    private Integer id;

    private String name;

    private String description;

    private Integer championshipId;

    private LocalDate start;

    private LocalDate end;

    private Set<ApplicationUser> observers;

    public CompetitionDTO(Integer championshipId, String description, LocalDate end, Integer id, String name, LocalDate start) {
        this.championshipId = championshipId;
        this.description = description;
        this.end = end;
        this.id = id;
        this.name = name;
        this.start = start;
    }
}
