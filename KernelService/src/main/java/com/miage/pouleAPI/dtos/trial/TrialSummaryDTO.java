package com.miage.pouleAPI.dtos.trial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrialSummaryDTO {
    private Integer id;
    private Integer idEvent;
    private String name;
    private String description;
}
