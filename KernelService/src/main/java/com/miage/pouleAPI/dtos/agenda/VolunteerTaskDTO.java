package com.miage.pouleAPI.dtos.agenda;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerTaskDTO {
    private Integer id;
    private String name;
    private String description;
    private VolunteerTaskEventDTO event;
}
