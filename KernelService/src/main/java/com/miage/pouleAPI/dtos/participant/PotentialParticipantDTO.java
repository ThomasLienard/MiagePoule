package com.miage.pouleAPI.dtos.participant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PotentialParticipantDTO {
    private Integer id;
    private String name;
    private String type; // "ATHLETE" or "TEAM"
    private String country;
}
