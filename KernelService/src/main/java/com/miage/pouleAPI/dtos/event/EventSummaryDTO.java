package com.miage.pouleAPI.dtos.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventSummaryDTO {
    private Integer id;
    private String name;
    private String description;
}
