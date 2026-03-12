package com.miage.pouleAPI.dtos.participant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AthleteDTO {
    private Integer id;
    private String fullName;
    private String country;
}
