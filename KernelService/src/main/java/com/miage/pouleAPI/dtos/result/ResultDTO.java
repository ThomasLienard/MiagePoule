package com.miage.pouleAPI.dtos.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultDTO {
    private Integer participantId;
    private String participantName;
    private String participantType; // "ATHLETE" or "TEAM"
    private String country;
    private Double result;
    private Boolean isValidated;
    private Boolean isForfeit;
}
