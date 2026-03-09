package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.event.CreateEventRequestDTO;
import com.miage.pouleAPI.entity.*;
import com.miage.pouleAPI.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEventServiceImplTest {

    @Mock private EventRepository eventRepo;
    @Mock private PlaceRepository placeRepo;
    @Mock private GeocodingService geocodingService;
    @Mock private TimeSlotRepository timeSlotRepo;
    @Mock private TypeEventRepository typeRepo;
    @Mock private CompetitionRepository competitionRepo;
    @Mock private TrialRepository trialRepo;

    @InjectMocks
    private AdminEventServiceImpl adminEventService;

    @Test
    void createEvent_ShouldNotSavePlace_WhenPlaceExists() {
        CreateEventRequestDTO req = createSampleDTO("MEETING");
        Place existingPlace = new Place();

        when(placeRepo.findByNameAndStreetAndCity(any(), any(), any()))
                .thenReturn(Optional.of(existingPlace));
        setupCommonMocks();

        adminEventService.createEvent(req);

        verify(geocodingService, never()).getCoordinates(anyString());
        verify(placeRepo, never()).save(any(Place.class));
        verify(eventRepo).save(any(Event.class));
    }

    @Test
    void createEvent_ShouldSaveNewPlace_WhenPlaceDoesNotExist() {
        CreateEventRequestDTO req = createSampleDTO("TRIAL");
        when(placeRepo.findByNameAndStreetAndCity(any(), any(), any())).thenReturn(Optional.empty());
        when(geocodingService.getCoordinates(anyString())).thenReturn(new Double[]{48.8, 2.3});
        setupCommonMocks();


        adminEventService.createEvent(req);

        verify(geocodingService).getCoordinates(anyString());
        verify(placeRepo).save(any(Place.class));
        verify(trialRepo).save(any(Trial.class));
    }

    @Test
    void createEvent_ShouldPersistEventWithAllRelations() {
        CreateEventRequestDTO req = createSampleDTO("MEETING");

        when(placeRepo.findByNameAndStreetAndCity(any(), any(), any())).thenReturn(Optional.empty());
        when(geocodingService.getCoordinates(anyString())).thenReturn(new Double[]{48.0, 2.0});

        TypeEvent mockType = new TypeEvent();
        mockType.setName("MEETING");
        when(typeRepo.findById("MEETING")).thenReturn(Optional.of(mockType));

        Competition mockComp = new Competition();
        when(competitionRepo.findById(1)).thenReturn(Optional.of(mockComp));

        when(timeSlotRepo.save(any(TimeSlot.class))).thenAnswer(i -> i.getArguments()[0]);
        when(placeRepo.save(any(Place.class))).thenAnswer(i -> i.getArguments()[0]);

        adminEventService.createEvent(req);

        verify(eventRepo).save(argThat(event -> {
            boolean nameOk = event.getName().equals(req.name());
            boolean descOk = event.getDescription().equals(req.description());
            boolean placeOk = event.getPlace().getName().equals(req.placeName());
            boolean compOk = event.getCompetition() != null;
            boolean slotOk = event.getTimeSlot().getStart().equals(req.startTime());

            return nameOk && descOk && placeOk && compOk && slotOk;
        }));

        verify(trialRepo, never()).save(any());
    }

    @Test
    void createEvent_ShouldPersistTrial_WhenTypeIsTrial() {
        CreateEventRequestDTO req = createSampleDTO("TRIAL");

        Place existingPlace = new Place();
        when(placeRepo.findByNameAndStreetAndCity(any(), any(), any())).thenReturn(Optional.of(existingPlace));

        when(typeRepo.findById("TRIAL")).thenReturn(Optional.of(new TypeEvent()));
        when(competitionRepo.findById(any())).thenReturn(Optional.of(new Competition()));
        when(timeSlotRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        adminEventService.createEvent(req);

        verify(trialRepo).save(any(Trial.class));
    }

    @Test
    void cancelEvent_ShouldUpdateStatusAndAppendReasonToDescription() {
        // GIVEN
        Integer eventId = 1;
        String initialDesc = "Compétition de natation";
        String cancelReason = "Panne de filtrage";

        Event event = new Event();
        event.setId(eventId);
        event.setStatus("SCHEDULED");
        event.setDescription(initialDesc);

        when(eventRepo.findById(eventId)).thenReturn(Optional.of(event));

        adminEventService.cancelEvent(eventId, cancelReason);

        assertEquals("CANCELLED", event.getStatus());

        org.junit.jupiter.api.Assertions.assertTrue(
                event.getDescription().contains(cancelReason),
                "La description devrait contenir la raison de l'annulation"
        );

        org.junit.jupiter.api.Assertions.assertTrue(event.getDescription().startsWith(initialDesc));

        verify(eventRepo).save(event);
    }

    @Test
    void cancelEvent_ShouldThrowException_WhenEventNotFound() {
        Integer eventId = 99;
        when(eventRepo.findById(eventId)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            adminEventService.cancelEvent(eventId, "raison");
        });

        verify(eventRepo, never()).save(any());
    }

    private void setupCommonMocks() {
        when(timeSlotRepo.save(any())).thenReturn(new TimeSlot());
        when(typeRepo.findById(anyString())).thenReturn(Optional.of(new TypeEvent()));
        when(competitionRepo.findById(anyInt())).thenReturn(Optional.of(new Competition()));
    }

    private CreateEventRequestDTO createSampleDTO(String type) {
        return new CreateEventRequestDTO("Test", "Desc", type, 1,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1),2,
                "Stade", "Paris", "Rue", "1", "75000", "PMR", 0.0, 0.0, true);
    }
}