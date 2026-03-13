package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import com.miage.pouleAPI.dtos.competition.CreateCompetitionRequestDTO;
import com.miage.pouleAPI.services.interfaces.CompetitionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCompetitionControllerTest {

    @Mock
    private CompetitionService competitionService;

    @InjectMocks
    private AdminCompetitionController adminCompetitionController;

    @Test
    void create_shouldReturn201() {
        CreateCompetitionRequestDTO request = new CreateCompetitionRequestDTO("Comp", "Desc", 1, LocalDate.now(), LocalDate.now().plusDays(1));

        ResponseEntity<Void> response = adminCompetitionController.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(competitionService).save(request);
    }

    @Test
    void update_shouldReturn202_AndFillGaps() {
        Integer id = 1;
        CompetitionDTO existing = new CompetitionDTO(id, "Ancienne Desc", LocalDate.now(), 1, "Ancien Nom", LocalDate.now());

        CompetitionDTO updateReq = new CompetitionDTO(id, "", null, 1, "", null);

        when(competitionService.findById(id)).thenReturn(Optional.of(existing));

        ResponseEntity<Void> response = adminCompetitionController.update(updateReq);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Ancien Nom", updateReq.getName());
        verify(competitionService).update(updateReq);
    }
}