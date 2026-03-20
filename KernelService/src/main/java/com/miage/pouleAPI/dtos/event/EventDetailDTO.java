package com.miage.pouleAPI.dtos.event;

import com.miage.pouleAPI.dtos.place.PlaceDTO;
import com.miage.pouleAPI.dtos.ranking.RankingDTO;
import com.miage.pouleAPI.dtos.timeslot.TimeSlotDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<RankingDTO> rankings;
    private String status;
    private String typeEvent;
}
