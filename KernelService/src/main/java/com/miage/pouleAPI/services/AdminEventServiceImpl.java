package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.event.CreateEventRequestDTO;
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

    private final EventRepository eventRepo;
    private final TrialRepository trialRepo;
    private final PlaceRepository placeRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final CompetitionRepository competitionRepo;
    private final TypeEventRepository typeRepo;
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

        if ("TRIAL".equalsIgnoreCase(req.typeEventName())) {
            Trial trial = new Trial();
            fillEventData(trial, req, type, place, slot, comp);
            trialRepo.save(trial);
        } else {
            Event event = new Event();
            fillEventData(event, req, type, place, slot, comp);
            eventRepo.save(event);
        }
    }

    private void fillEventData(Event e, CreateEventRequestDTO req, TypeEvent t, Place p, TimeSlot s, Competition c) {
        e.setName(req.name());
        e.setDescription(req.description());
        e.setTypeEvent(t);
        e.setPlace(p);
        e.setTimeSlot(s);
        e.setCompetition(c);
    }
}