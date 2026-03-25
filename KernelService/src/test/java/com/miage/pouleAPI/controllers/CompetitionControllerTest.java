package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import com.miage.pouleAPI.entity.Championship;
import com.miage.pouleAPI.services.interfaces.CompetitionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompetitionControllerTest {

    @Mock
    private CompetitionService competitionService;

    @InjectMocks
    private CompetitionController controller;

    private Championship championship;
    private CompetitionDTO competition1;

    @BeforeEach
    void setUp() {
        championship = new Championship(1,
                "Championship 1 desc",
                "Championship 1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));
        competition1 = new CompetitionDTO(
                championship.getId(),
                "Competition 1 Description",
                LocalDate.of(2024, 10, 20),
                1,
                "Competition 1",
                LocalDate.of(2024, 1, 7)
        );
    }

    @Test
    void getById_ShouldReturnCompetition_WhenExists() {
        Integer id = 1;
        Integer championshipId = 1;
        when(competitionService.findById(id)).thenReturn(Optional.of(competition1));

        ResponseEntity<CompetitionDTO> response = controller.getById(championshipId,id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1);
        assertThat(response.getBody().getChampionshipId()).isEqualTo(championshipId);
        assertThat(response.getBody().getName()).isEqualTo("Competition 1");
        assertThat(response.getBody().getDescription()).isEqualTo("Competition 1 Description");
        assertThat(response.getBody().getStart()).isEqualTo(LocalDate.of(2024, 1, 7));
        assertThat(response.getBody().getEnd()).isEqualTo(LocalDate.of(2024, 10, 20));
        verify(competitionService, times(1)).findById(id);
    }

    @Test
    void getById_ShouldReturnNotFound_WhenNotExists() {
        Integer id = 999;
        Integer championshipId = 1;
        when(competitionService.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<CompetitionDTO> response = controller.getById(championshipId,id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(competitionService, times(1)).findById(id);
    }

    @Test
    void constructor_ShouldInitializeServices() {
        CompetitionController newController = new CompetitionController(
                competitionService
        );

        assertThat(newController).isNotNull();
    }

    @Test
    void getById_ShouldNotCallServiceMultipleTimes() {
        Integer id = 1;
        Integer championshipId = 1;
        when(competitionService.findById(id)).thenReturn(Optional.of(competition1));

        controller.getById(championshipId, id);

        verify(competitionService, times(1)).findById(id);
        verifyNoMoreInteractions(competitionService);
    }
}