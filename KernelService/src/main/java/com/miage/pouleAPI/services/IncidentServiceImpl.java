package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.incident.CreateIncidentRequestDTO;
import com.miage.pouleAPI.dtos.incident.IncidentDetailDTO;
import com.miage.pouleAPI.dtos.incident.IncidentSummaryDTO;
import com.miage.pouleAPI.entity.*;
import com.miage.pouleAPI.repositories.*;
import com.miage.pouleAPI.services.interfaces.IncidentService;
import com.miage.pouleAPI.services.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final NotificationRepository notificationRepository;
    private final EventRepository eventRepository;
    private final PlaceRepository placeRepository;
    private final SeverityRepository severityRepository;
    private final NotificationService notificationService;
    private final CompetitionRepository competitionRepository;

    @Override
    public IncidentDetailDTO createIncident(CreateIncidentRequestDTO requestDTO) {
        // Récupérer la sévérité
        Severity severity = severityRepository.findById(requestDTO.severity())
                .orElseThrow(() -> new RuntimeException("Sévérité non trouvée: " + requestDTO.severity()));

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

        // Créer la notification d'incident
        Notification notification = new Notification();
        notification.setTitle(requestDTO.title());
        notification.setDescription(requestDTO.description());
        notification.setSeverity(severity);
        notification.setEvent(event);
        notification.setPlace(place);
        notification.setType(TypeNotification.INCIDENT);
        notification.setEmissionDate(LocalDateTime.now());

        // Déterminer les destinataires et notifier
        String scope = requestDTO.audienceScope() == null ? "TOUS" : requestDTO.audienceScope();

        if (requestDTO.competitionId() != null) {
            Competition competition = competitionRepository.findById(requestDTO.competitionId())
                    .orElseThrow(() -> new RuntimeException("Compétition non trouvée"));
            Notification saved = notificationService.notifyIncident(notification, competition, scope);
            return convertToDetailDTO(saved);
        }

        if (event != null && event.getCompetition() != null) {
            Notification saved = notificationService.notifyIncident(notification, event.getCompetition(), scope);
            return convertToDetailDTO(saved);
        }

        if (place != null) {
            // Notifier les observateurs des compétitions des événements qui se déroulent à ce lieu
            List<Event> eventsAtPlace = eventRepository.findByPlaceId(place.getId());
            Notification saved = notificationRepository.save(notification);

            eventsAtPlace.stream()
                    .map(Event::getCompetition)
                    .distinct()
                    .forEach(comp -> notificationService.notifyIncident(saved, comp, scope));

            return convertToDetailDTO(saved);
        }

        // Si aucun contexte trouvée (ni compétition, ni épreuve, ni lieu), on sauvegarde quand même.
        Notification saved = notificationRepository.save(notification);
        return convertToDetailDTO(saved);
    }

    @Override
    public Optional<IncidentDetailDTO> getIncidentById(Integer id) {
        return notificationRepository.findById(id)
                .filter(notification -> notification.getType() == TypeNotification.INCIDENT)
                .map(this::convertToDetailDTO);
    }

    @Override
    public List<IncidentSummaryDTO> getAllIncidents() {
        return notificationRepository.findAll().stream()
                .filter(notification -> notification.getType() == TypeNotification.INCIDENT)
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidentSummaryDTO> getIncidentsByEventId(Integer eventId) {
        return notificationRepository.findByEventId(eventId).stream()
                .filter(notification -> notification.getType() == TypeNotification.INCIDENT)
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidentSummaryDTO> getIncidentsByPlaceId(Integer placeId) {
        return notificationRepository.findByPlaceId(placeId).stream()
                .filter(notification -> notification.getType() == TypeNotification.INCIDENT)
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IncidentSummaryDTO> getIncidentsBySeverity(String severity) {
        return notificationRepository.findBySeverity(severity).stream()
                .filter(notification -> notification.getType() == TypeNotification.INCIDENT)
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    private IncidentDetailDTO convertToDetailDTO(Notification notification) {
        return new IncidentDetailDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getDescription(),
                notification.getSeverity().getName(),
                notification.getEvent() != null ? notification.getEvent().getId() : null,
                notification.getEvent() != null ? notification.getEvent().getName() : null,
                notification.getPlace() != null ? notification.getPlace().getId() : null,
                notification.getPlace() != null ? notification.getPlace().getName() : null,
                notification.getEmissionDate()
        );
    }

    private IncidentSummaryDTO convertToSummaryDTO(Notification notification) {
        return new IncidentSummaryDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getSeverity().getName(),
                notification.getEmissionDate()
        );
    }
}
