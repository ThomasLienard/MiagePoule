package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.dtos.incident.CreateIncidentRequestDTO;
import com.miage.pouleAPI.dtos.incident.IncidentDetailDTO;
import com.miage.pouleAPI.dtos.incident.IncidentSummaryDTO;

import java.util.List;
import java.util.Optional;

public interface IncidentService {
    IncidentDetailDTO createIncident(CreateIncidentRequestDTO requestDTO);
    Optional<IncidentDetailDTO> getIncidentById(Integer id);
    List<IncidentSummaryDTO> getAllIncidents();
    List<IncidentSummaryDTO> getIncidentsByEventId(Integer eventId);
    List<IncidentSummaryDTO> getIncidentsByPlaceId(Integer placeId);
    List<IncidentSummaryDTO> getIncidentsBySeverity(String severity);
}
