package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import com.miage.pouleAPI.dtos.event.CreateEventRequestDTO;
import com.miage.pouleAPI.dtos.event.EventDetailDTO;
import com.miage.pouleAPI.dtos.event.UpdateEventRequestDTO;
import com.miage.pouleAPI.services.interfaces.AdminEventService;
import com.miage.pouleAPI.services.interfaces.CompetitionService;
import com.miage.pouleAPI.services.interfaces.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEventControllerTest {

    @Mock private AdminEventService adminEventService;
    @Mock private EventService eventService;
    @Mock private CompetitionService competitionService;

    @InjectMocks
    private AdminEventController adminEventController;

    @Test
    void create_shouldReturn201() {
        CreateEventRequestDTO request = new CreateEventRequestDTO("Event", "Desc", "TRIAL", 1, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Lieu", "Ville", "Rue", "1", "75000", "Infos", 0.0, 0.0, true,null);

        ResponseEntity<Void> response = adminEventController.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(adminEventService).createEvent(request);
    }

    @Test
    void update_shouldReturn202_AndMergeWithExisting() {
        Integer id = 5;
        Integer compIdFound = 10;
        String eventNameInDb = "Nom Existant";
        String competitionNameInDb = "Marathon";

        UpdateEventRequestDTO updateReq = new UpdateEventRequestDTO();
        updateReq.setName("    ");
        updateReq.setCompetitionId(null);

        EventDetailDTO existing = new EventDetailDTO();
        existing.setName(eventNameInDb);
        existing.setCompetitionName(competitionNameInDb);

        when(eventService.getEventById(id)).thenReturn(Optional.of(existing));

        when(competitionService.findByName(competitionNameInDb))
                .thenReturn(Optional.of(new CompetitionDTO(1, "desc", null, compIdFound, competitionNameInDb, null)));

        ResponseEntity<Void> response = adminEventController.update(id, updateReq);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(eventNameInDb, updateReq.getName());
        assertEquals(compIdFound, updateReq.getCompetitionId());
        verify(adminEventService).updateEvent(updateReq);
    }
}