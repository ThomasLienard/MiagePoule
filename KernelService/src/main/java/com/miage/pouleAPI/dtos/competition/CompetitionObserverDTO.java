package com.miage.pouleAPI.dtos.competition;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompetitionObserverDTO {
    private Integer userId;
    private Integer competitionId;

    public CompetitionObserverDTO(Integer competitionId, Integer userId) {
        this.competitionId = competitionId;
        this.userId = userId;
    }
}
