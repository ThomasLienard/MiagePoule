package com.miage.pouleAPI.dtos.agenda;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerTaskDTO {
    private Integer id;
    private String name;
    private String description;
    private List<VolunteerTaskEventDTO> events;
}
