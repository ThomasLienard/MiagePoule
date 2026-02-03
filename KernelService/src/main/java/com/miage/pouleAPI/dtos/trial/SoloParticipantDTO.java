package com.miage.pouleAPI.dtos.trial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoloParticipantDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String fullName;
}
