package com.miage.pouleAPI.dtos.event;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateEventDatesDTO {

    private LocalDateTime start;
    private LocalDateTime end;
}