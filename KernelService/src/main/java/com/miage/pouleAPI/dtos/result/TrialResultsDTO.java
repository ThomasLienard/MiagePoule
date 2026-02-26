package com.miage.pouleAPI.dtos.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrialResultsDTO {
    private Integer trialId;
    private String trialName;
    private boolean isTeamTrial;
    private List<ResultDTO> results;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
