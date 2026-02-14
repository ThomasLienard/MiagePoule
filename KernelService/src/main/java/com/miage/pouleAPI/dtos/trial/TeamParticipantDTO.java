package com.miage.pouleAPI.dtos.trial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamParticipantDTO {
    private Integer id;
    private String name;
    private String country;
    private List<SoloParticipantDTO> members;
}
