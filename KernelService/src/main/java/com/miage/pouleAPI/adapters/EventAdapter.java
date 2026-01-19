package com.miage.pouleAPI.adapters;

import com.miage.pouleAPI.dtos.event.EventDetailDTO;
import com.miage.pouleAPI.dtos.event.EventSummaryDTO;
import com.miage.pouleAPI.dtos.place.PlaceDTO;
import com.miage.pouleAPI.dtos.timeslot.TimeSlotDTO;
import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.Place;
import com.miage.pouleAPI.entity.TimeSlot;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class EventAdapter {

    // ===== Conversions Entity -> DTO =====
    
    public EventSummaryDTO entityToSummaryDto(Event event) {
        if (event == null) return null;
        
        return new EventSummaryDTO(
            event.getId(),
            event.getName(),
            event.getDescription()
        );
    }
    
    public List<EventSummaryDTO> entityListToSummaryDtoList(List<Event> events) {
        if (events == null) return new ArrayList<>();
        return events.stream()
            .map(this::entityToSummaryDto)
            .toList();
    }
    
    public EventDetailDTO entityToDetailDto(Event event) {
        if (event == null) return null;
        
        EventDetailDTO dto = new EventDetailDTO();
        dto.setId(event.getId());
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
        
        // Rankings will be empty for regular events (only trials have rankings)
        dto.setRankings(new ArrayList<>());
        
        return dto;
    }
    
    // ===== Conversions DTO -> Entity =====
    
    public Event summaryDtoToEntity(EventSummaryDTO dto) {
        if (dto == null) return null;
        
        Event event = new Event();
        event.setId(dto.getId());
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        
        return event;
    }
    
    public Event detailDtoToEntity(EventDetailDTO dto) {
        if (dto == null) return null;
        
        Event event = new Event();
        event.setId(dto.getId());
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        
        // Note: Pour une conversion complète, il faudrait récupérer
        // les entités liées (Competition, TimeSlot, Place) depuis la BDD
        // Cette méthode est plutôt utilisée pour les créations/updates
        
        if (dto.getTimeSlot() != null) {
            event.setTimeSlot(dtoToTimeSlot(dto.getTimeSlot()));
        }
        
        if (dto.getPlace() != null) {
            event.setPlace(dtoToPlace(dto.getPlace()));
        }
        
        return event;
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
