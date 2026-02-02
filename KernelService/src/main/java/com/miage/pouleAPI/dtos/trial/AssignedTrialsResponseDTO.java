package com.miage.pouleAPI.dtos.trial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignedTrialsResponseDTO {
    private List<TrialSummaryDTO> soloTrials;
    private List<TrialSummaryDTO> teamTrials;
}
