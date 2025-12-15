package com.miage.pouleAPI.adapter;


import com.miage.pouleAPI.dto.place.PlaceDTO;
import com.miage.pouleAPI.dto.timeslot.TimeSlotDTO;
import com.miage.pouleAPI.dto.trial.TrialDetailDTO;
import com.miage.pouleAPI.dto.trial.TrialSummaryDTO;
import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.Place;
import com.miage.pouleAPI.entity.TimeSlot;
import com.miage.pouleAPI.entity.Trial;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrialAdapter {

    // ===== Conversions Entity -> DTO =====
    
    public TrialSummaryDTO entityToSummaryDto(Trial trial) {
        if (trial == null || trial.getEvent() == null) return null;
        
        Event event = trial.getEvent();
        
        return new TrialSummaryDTO(
            trial.getId(),
            event.getName(),
            event.getDescription()
        );
    }
    
    public List<TrialSummaryDTO> entityListToSummaryDtoList(List<Trial> trials) {
    if (trials == null) return null;
    return trials.stream()
        .map(this::entityToSummaryDto)
        .filter(dto -> dto != null)  // ← Ajouter ce filtre
        .collect(Collectors.toList());
}

    
    public TrialDetailDTO entityToDetailDto(Trial trial) {
        if (trial == null || trial.getEvent() == null) return null;
        
        Event event = trial.getEvent();
        
        TrialDetailDTO dto = new TrialDetailDTO();
        dto.setId(trial.getId());
        dto.setName(event.getName());
        dto.setDescription(event.getDescription());
        
        // Competition name
        if (event.getCompetition() != null) {
            dto.setCompetitionName(event.getCompetition().getName());
        }
        
        // TimeSlot
        if (event.getTimeSlot() != null) {
            dto.setTimeSlot(timeSlotToDto(event.getTimeSlot()));
        }
        
        // Place
        if (event.getPlace() != null) {
            dto.setPlace(placeToDto(event.getPlace()));
        }
        
        return dto;
    }
    
    // ===== Conversions DTO -> Entity =====
    
    public Trial summaryDtoToEntity(TrialSummaryDTO dto) {
        if (dto == null) return null;
        
        Trial trial = new Trial();
        trial.setId(dto.getId());
        
        Event event = new Event();
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        trial.setEvent(event);
        
        return trial;
    }
    
    public Trial detailDtoToEntity(TrialDetailDTO dto) {
        if (dto == null) return null;
        
        Trial trial = new Trial();
        trial.setId(dto.getId());
        
        Event event = new Event();
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        
        if (dto.getTimeSlot() != null) {
            event.setTimeSlot(dtoToTimeSlot(dto.getTimeSlot()));
        }
        
        if (dto.getPlace() != null) {
            event.setPlace(dtoToPlace(dto.getPlace()));
        }
        
        trial.setEvent(event);
        
        return trial;
    }
    
    // ===== Méthodes privées pour les sous-objets =====
    
    private TimeSlotDTO timeSlotToDto(TimeSlot timeSlot) {
        if (timeSlot == null) return null;
        
        return new TimeSlotDTO(
            timeSlot.getStart(),
            timeSlot.getEnd()
        );
    }
    
    private TimeSlot dtoToTimeSlot(TimeSlotDTO dto) {
        if (dto == null) return null;
        
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStart(dto.getStart());
        timeSlot.setEnd(dto.getEnd());
        
        return timeSlot;
    }
    
    private PlaceDTO placeToDto(Place place) {
        if (place == null) return null;
        
        PlaceDTO dto = new PlaceDTO();
        dto.setId(place.getId());
        dto.setName(place.getName());
        dto.setDescription(place.getDescription());
        dto.setStreet(place.getStreet());
        dto.setNumber(place.getNumber());
        dto.setCity(place.getCity());
        dto.setZip(place.getZip());
        dto.setParking(place.getParking());
        dto.setLatitude(place.getLatitude());
        dto.setLongitude(place.getLongitude());
        
        return dto;
    }
    
    private Place dtoToPlace(PlaceDTO dto) {
        if (dto == null) return null;
        
        Place place = new Place();
        place.setId(dto.getId());
        place.setName(dto.getName());
        place.setDescription(dto.getDescription());
        place.setStreet(dto.getStreet());
        place.setNumber(dto.getNumber());
        place.setCity(dto.getCity());
        place.setZip(dto.getZip());
        place.setParking(dto.getParking());
        place.setLatitude(dto.getLatitude());
        place.setLongitude(dto.getLongitude());
        
        return place;
    }
}
