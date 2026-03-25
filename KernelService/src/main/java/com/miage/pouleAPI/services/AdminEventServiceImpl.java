package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.event.CreateEventRequestDTO;
import com.miage.pouleAPI.dtos.event.UpdateEventRequestDTO;
import com.miage.pouleAPI.dtos.place.PlaceDTO;
import com.miage.pouleAPI.dtos.timeslot.TimeSlotDTO;
import com.miage.pouleAPI.entity.*;
import com.miage.pouleAPI.repositories.*;
import com.miage.pouleAPI.services.interfaces.AdminEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminEventServiceImpl implements AdminEventService {

    public static final String TYPE_TRIAL = "TRIAL";
    private final EventRepository eventRepo;
    private final TrialRepository trialRepo;
    private final PlaceRepository placeRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final CompetitionRepository competitionRepo;
    private final TypeEventRepository typeRepo;
    private final TypeScoreRepository typeScoreRepo;
    private final GeocodingService geocodingService;

    @Override
    @Transactional
    public void createEvent(CreateEventRequestDTO req) {

        Optional<Place> existingPlace = placeRepo.findByNameAndStreetAndCity(
                req.placeName(), req.street(), req.city()
        );

        Place place;
        if (existingPlace.isPresent()) {
            place = existingPlace.get();
        } else {
            place = new Place();
            place.setName(req.placeName());
            place.setCity(req.city());
            place.setParking(req.hasParking());
            place.setDescription(req.descriptionPlace());
            place.setStreet(req.street());
            place.setNumber(req.number());
            place.setZip(req.zipCode());
            place.setLatitude(req.latitude());
            place.setLongitude(req.longitude());

            String fullAddress =req.number() + " "+ req.street() + ", " + req.zipCode() + " " + req.city();
            Double[] coords = geocodingService.getCoordinates(fullAddress);
            place.setLatitude(coords[0]);
            place.setLongitude(coords[1]);
            place = placeRepo.save(place);
        }



        TimeSlot slot = new TimeSlot();
        slot.setStart(req.startTime());
        slot.setEnd(req.endTime());
        slot = timeSlotRepo.save(slot);
        TypeEvent type = typeRepo.findById(req.typeEventName())
                .orElseThrow(() -> new RuntimeException("Type non trouvé"));
        Competition comp = competitionRepo.findById(req.competitionId())
                .orElseThrow(() -> new RuntimeException("Compétition non trouvée"));


        if (TYPE_TRIAL.equalsIgnoreCase(req.typeEventName())) {
            Trial trial = new Trial();
            fillEventData(trial, req, type, place, slot, comp);
            trialRepo.save(trial);
            if (req.commissaireId() != null) {
                eventRepo.linkCommissaireToEvent(req.commissaireId(), trial.getId());
            }
        } else {
            Event event = new Event();
            fillEventData(event, req, type, place, slot, comp);
            eventRepo.save(event);
            if (req.commissaireId() != null) {
                eventRepo.linkCommissaireToEvent(req.commissaireId(), event.getId());
            }
        }
    }

    @Override
    @Transactional
    public void cancelEvent(Integer id, String reason) {
        Event event = eventRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Épreuve introuvable"));

        event.setStatus("CANCELLED");
        event.setDescription(event.getDescription() + '\n' +reason);
        eventRepo.save(event);
    }

    private void fillEventData(Event e, CreateEventRequestDTO req, TypeEvent t, Place p, TimeSlot s, Competition c) {
        e.setName(req.name());
        e.setDescription(req.description());
        e.setTypeEvent(t);
        e.setPlace(p);
        e.setTimeSlot(s);
        e.setCompetition(c);

        // Détermine le type de score
        String scoreTypeName;
        if (req.typeScoreName() != null && !req.typeScoreName().isBlank()) {
            // Utilise le type de score spécifié dans la requête
            scoreTypeName = req.typeScoreName();
        } else {
            // Valeur par défaut selon le type d'événement
            scoreTypeName = TYPE_TRIAL.equalsIgnoreCase(req.typeEventName()) ? "TIME" : "NA";
        }

        e.setTypeScore(typeScoreRepo.findById(scoreTypeName)
                .orElseThrow(() -> new RuntimeException("Type de score non trouvé: " + scoreTypeName)));
    }

    @Override
    @Transactional
    public void updateEvent(UpdateEventRequestDTO dto) {
        Event existing = eventRepo.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Event introuvable"));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setCompetition(competitionRepo.findById(dto.getCompetitionId())
                .orElseThrow(() -> new RuntimeException("Compétition introuvable")));

        this.updateTimeSlot(existing.getTimeSlot(), dto.getTimeSlot());

        if (dto.getPlace() != null) {
            existing.setPlace(updateOrCreatePlace(dto.getPlace()));
        }

        String scoreTypeName = this.resolveScoreTypeName(dto.getTypeEventName(), dto.getScoreType());
        existing.setTypeScore(typeScoreRepo.findById(scoreTypeName)
                .orElseThrow(() -> new RuntimeException("Type de score non trouvé: " + scoreTypeName)));

        this.handleCommissaireChanges(existing.getId(), dto.getTypeEventName(), dto.getCommissaireId());

        eventRepo.save(existing);
    }

    private void updateTimeSlot(TimeSlot slot, TimeSlotDTO newSlot) {
        if (newSlot != null) {
            if (newSlot.getStart() != null) slot.setStart(newSlot.getStart());
            if (newSlot.getEnd() != null) slot.setEnd(newSlot.getEnd());
            timeSlotRepo.save(slot);
        }
    }

    private void handleCommissaireChanges(Integer eventId, String newTypeEventName, Integer commissaireId) {
        boolean isTrial = TYPE_TRIAL.equalsIgnoreCase(newTypeEventName);

        if (isTrial) {
            eventRepo.unlinkAllCommissairesFromEvent(eventId);
            if (commissaireId != null) {
                eventRepo.linkCommissaireToEvent(commissaireId, eventId);
            }
        }
    }

    private Place updateOrCreatePlace(PlaceDTO p) {
        Place place = placeRepo.findByNameAndStreetAndCity(p.getName(), p.getStreet(), p.getCity())
                .orElse(new Place());

        updatePlaceBasicFields(place, p);

        if (needsGeocoding(p)) {
            try {
                geocodePlace(place, p);
            } catch (Exception e) {
                // Garde les coords existantes ou null
            }
        } else {
            place.setLatitude(p.getLatitude());
            place.setLongitude(p.getLongitude());
        }

        return placeRepo.save(place);
    }

    private void updatePlaceBasicFields(Place place, PlaceDTO p) {
        place.setName(p.getName());
        place.setStreet(p.getStreet());
        place.setCity(p.getCity());
        place.setZip(p.getZip());
        place.setNumber(p.getNumber());
        place.setParking(p.getParking());
        place.setDescription(p.getDescription());
    }

    private boolean needsGeocoding(PlaceDTO p) {
        return p.getLatitude() == null || p.getLongitude() == null;
    }

    private void geocodePlace(Place place, PlaceDTO p) {
        String fullAddress = p.getNumber() + " " + p.getStreet() + ", " + p.getZip() + " " + p.getCity();
        Double[] coords = geocodingService.getCoordinates(fullAddress);
        place.setLatitude(coords[0]);
        place.setLongitude(coords[1]);
    }

    private String resolveScoreTypeName(String typeEventName, String explicitScoreType) {
        if (explicitScoreType != null && !explicitScoreType.isBlank()) {
            return explicitScoreType;
        }
        return TYPE_TRIAL.equalsIgnoreCase(typeEventName) ? "TIME" : "NA";
    }
}