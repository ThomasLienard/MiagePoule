package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.incident.CreateIncidentRequestDTO;
import com.miage.pouleAPI.dtos.incident.IncidentDetailDTO;
import com.miage.pouleAPI.dtos.incident.IncidentSummaryDTO;
import com.miage.pouleAPI.entity.*;
import com.miage.pouleAPI.repositories.*;
import com.miage.pouleAPI.services.interfaces.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final EventRepository eventRepository;
    private final PlaceRepository placeRepository;
    private final AlertLevelRepository alertLevelRepository;

    @Override
    public IncidentDetailDTO createIncident(CreateIncidentRequestDTO requestDTO, Integer userId) {
        // Récupérer l'utilisateur qui crée l'incident
        ApplicationUser user = applicationUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Récupérer le niveau d'alerte
        AlertLevel alertLevel = alertLevelRepository.findById(requestDTO.alertLevel())
                .orElseThrow(() -> new RuntimeException("Niveau d'alerte non trouvé: " + requestDTO.alertLevel()));

        // Récupérer l'événement (optionnel)
        Event event = null;
        if (requestDTO.eventId() != null) {
            event = eventRepository.findById(requestDTO.eventId())
                    .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
        }

        // Récupérer le lieu (optionnel)
        Place place = null;
        if (requestDTO.placeId() != null) {
            place = placeRepository.findById(requestDTO.placeId())
                    .orElseThrow(() -> new RuntimeException("Lieu non trouvé"));
        }

        // Créer l'incident
        Incident incident = new Incident();
        incident.setTitle(requestDTO.title());
        incident.setDescription(requestDTO.description());
        incident.setAlertLevel(alertLevel);
        incident.setEvent(event);
        incident.setPlace(place);
        incident.setCreatedBy(user);
        incident.setCreatedAt(LocalDateTime.now());

        // Sauvegarder l'incident
        Incident savedIncident = incidentRepository.save(incident);

        // Retourner le DTO
        return convertToDetailDTO(savedIncident);
    }

    @Override
    public Optional<IncidentDetailDTO> getIncidentById(Integer id) {
        return incidentRepository.findById(id)
                .map(this::convertToDetailDTO);
    }

    @Override
    public List<IncidentSummaryDTO> getAllIncidents() {
        return incidentRepository.findAll().stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidentSummaryDTO> getIncidentsByEventId(Integer eventId) {
        return incidentRepository.findByEventId(eventId).stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidentSummaryDTO> getIncidentsByPlaceId(Integer placeId) {
        return incidentRepository.findByPlaceId(placeId).stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidentSummaryDTO> getIncidentsByAlertLevel(String alertLevel) {
        return incidentRepository.findByAlertLevel(alertLevel).stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    private IncidentDetailDTO convertToDetailDTO(Incident incident) {
        return new IncidentDetailDTO(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getAlertLevel().getName(),
                incident.getEvent() != null ? incident.getEvent().getId() : null,
                incident.getEvent() != null ? incident.getEvent().getName() : null,
                incident.getPlace() != null ? incident.getPlace().getId() : null,
                incident.getPlace() != null ? incident.getPlace().getName() : null,
                incident.getCreatedBy().getEmail(),
                incident.getCreatedAt()
        );
    }

    private IncidentSummaryDTO convertToSummaryDTO(Incident incident) {
        return new IncidentSummaryDTO(
                incident.getId(),
                incident.getTitle(),
                incident.getAlertLevel().getName(),
                incident.getCreatedAt()
        );
    }
}
