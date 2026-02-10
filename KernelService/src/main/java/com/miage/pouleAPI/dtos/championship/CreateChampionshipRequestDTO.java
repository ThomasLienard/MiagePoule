package com.miage.pouleAPI.dtos.championship;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class CreateChampionshipRequestDTO {

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate start;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate end;
}
