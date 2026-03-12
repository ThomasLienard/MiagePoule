package com.miage.pouleAPI.dtos.agenda;

import com.miage.pouleAPI.dtos.place.PlaceDTO;
import com.miage.pouleAPI.dtos.timeslot.TimeSlotDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerTaskEventDTO {
    private Integer eventId;
    private String eventName;
    private TimeSlotDTO timeSlot;
    private PlaceDTO place;
}
