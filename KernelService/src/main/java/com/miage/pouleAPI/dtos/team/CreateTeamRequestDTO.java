package com.miage.pouleAPI.dtos.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequestDTO {

    @NotBlank(message = "Le nom de l'équipe est obligatoire")
    private String name;
    
    @NotNull(message = "Le code pays est obligatoire")
    private String countryCode;
    
    private Set<Integer> memberIds;
}
