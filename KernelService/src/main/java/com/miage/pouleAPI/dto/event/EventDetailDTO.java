package com.miage.pouleAPI.dto.event;

import com.miage.pouleAPI.dto.place.PlaceDTO;
import com.miage.pouleAPI.dto.timeslot.TimeSlotDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailDTO {
    private Integer id;
    private String name;
    private String description;
    private String competitionName;
    private TimeSlotDTO timeSlot;
    private PlaceDTO place;
}
