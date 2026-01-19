package com.miage.pouleAPI.dtos.ranking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingDTO {
    private Integer rank;
    private String result;
    private String participantName;  // For athletes: "firstname lastname", for teams: "team name"
    private String participantType;   // "ATHLETE" or "TEAM"
    private Integer participantId;
}
