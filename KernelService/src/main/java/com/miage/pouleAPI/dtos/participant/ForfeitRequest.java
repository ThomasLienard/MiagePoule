package com.miage.pouleAPI.dtos.participant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForfeitRequest {
    @NotNull(message = "L'ID du participant est requis")
    private Integer participantId;
    
    @NotNull(message = "Le type de participant est requis")
    private String participantType; // "ATHLETE" or "TEAM"
}
