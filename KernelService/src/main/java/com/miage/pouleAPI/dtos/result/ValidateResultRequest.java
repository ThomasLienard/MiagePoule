package com.miage.pouleAPI.dtos.result;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateResultRequest {
    @NotNull
    private Integer participantId;
    @NotNull
    private String participantType; // "ATHLETE" or "TEAM"
}
