package com.miage.pouleAPI.dtos.participant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrialParticipantsDTO {
    private Integer trialId;
    private String trialName;
    private boolean isTeamTrial; // true si épreuve en équipe, false si solo
    private List<ParticipantDTO> participants;
    private List<PotentialParticipantDTO> potentialParticipants;
}
