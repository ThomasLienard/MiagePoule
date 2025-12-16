package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.domains.ChampionshipModel;
import com.miage.pouleAPI.domains.CompetitionModel;
import com.miage.pouleAPI.entity.Championship;
import com.miage.pouleAPI.services.ChampionshipService;
import com.miage.pouleAPI.services.CompetitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class ChampionshipControllerTest {

    @Mock
    private ChampionshipService championshipService;

    @Mock
    private CompetitionService competitionService;

    @InjectMocks
    private ChampionshipController controller;

    private ChampionshipModel championship1;
    private ChampionshipModel championship2;
    private Championship championshipEntity;
    private CompetitionModel competition1;
    private CompetitionModel competition2;

    @BeforeEach
    void setUp() {
        championshipEntity = new Championship(
                1,
                "Championship 1 Description",
                "Championship 1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        championship1 = new ChampionshipModel(
                "Championship 1 Description",
                LocalDate.of(2024, 12, 31),
                1,
                "Championship 1",
                LocalDate.of(2024, 1, 1)
        );

        championship2 = new ChampionshipModel(
                "Championship 2 Description",
                LocalDate.of(2025, 12, 31),
                2,
                "Championship 2",
                LocalDate.of(2025, 1, 1)
        );

        competition1 = new CompetitionModel(
                championshipEntity.getId(),
                "Competition 1 Description",
                LocalDate.of(2024, 6, 30),
                1,
                "Competition 1",
                LocalDate.of(2024, 1, 1)
        );

        competition2 = new CompetitionModel(
                championshipEntity.getId(),
                "Competition 2 Description",
                LocalDate.of(2024, 12, 31),
                2,
                "Competition 2",
                LocalDate.of(2024, 7, 1)
        );
    }

    @Test
    void getAll_ShouldReturnListOfChampionships() {
        List<ChampionshipModel> championships = Arrays.asList(championship1, championship2);
        when(championshipService.findAll()).thenReturn(championships);

        List<ChampionshipModel> result = controller.getAll();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Championship 1");
        assertThat(result.get(0).getDescription()).isEqualTo("Championship 1 Description");
        assertThat(result.get(1).getName()).isEqualTo("Championship 2");
        verify(championshipService, times(1)).findAll();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoChampionships() {
        when(championshipService.findAll()).thenReturn(List.of());

        List<ChampionshipModel> result = controller.getAll();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(championshipService, times(1)).findAll();
    }

    @Test
    void getAll_ShouldReturnChampionshipsWithCorrectDates() {
        when(championshipService.findAll()).thenReturn(Arrays.asList(championship1));

        List<ChampionshipModel> result = controller.getAll();

        assertThat(result).isNotNull();
        assertThat(result.get(0).getStart()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(result.get(0).getEnd()).isEqualTo(LocalDate.of(2024, 12, 31));
        verify(championshipService, times(1)).findAll();
    }

    @Test
    void getById_ShouldReturnChampionship_WhenExists() {
        Integer id = 1;
        when(championshipService.findById(id)).thenReturn(Optional.of(championship1));

        ResponseEntity<ChampionshipModel> response = controller.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1);
        assertThat(response.getBody().getName()).isEqualTo("Championship 1");
        assertThat(response.getBody().getDescription()).isEqualTo("Championship 1 Description");
        verify(championshipService, times(1)).findById(id);
    }

    @Test
    void getById_ShouldReturnNotFound_WhenNotExists() {
        Integer id = 999;
        when(championshipService.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<ChampionshipModel> response = controller.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(championshipService, times(1)).findById(id);
    }

    @Test
    void getById_ShouldReturnChampionshipWithAllFields() {
        Integer id = 1;
        when(championshipService.findById(id)).thenReturn(Optional.of(championship1));

        ResponseEntity<ChampionshipModel> response = controller.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1);
        assertThat(response.getBody().getName()).isEqualTo("Championship 1");
        assertThat(response.getBody().getDescription()).isEqualTo("Championship 1 Description");
        assertThat(response.getBody().getStart()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(response.getBody().getEnd()).isEqualTo(LocalDate.of(2024, 12, 31));
        verify(championshipService, times(1)).findById(id);
    }

    @Test
    void getCompetitions_ShouldReturnListOfCompetitions_WhenChampionshipHasCompetitions() {
        Integer championshipId = 1;
        List<CompetitionModel> competitions = Arrays.asList(competition1, competition2);
        when(competitionService.findByChampionship(championshipId)).thenReturn(competitions);

        List<CompetitionModel> result = controller.getCompetitions(championshipId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Competition 1");
        assertThat(result.get(0).getDescription()).isEqualTo("Competition 1 Description");
        assertThat(result.get(1).getName()).isEqualTo("Competition 2");
        verify(competitionService, times(1)).findByChampionship(championshipId);
    }

    @Test
    void getCompetitions_ShouldReturnEmptyList_WhenChampionshipHasNoCompetitions() {
        Integer championshipId = 999;
        when(competitionService.findByChampionship(championshipId)).thenReturn(List.of());

        List<CompetitionModel> result = controller.getCompetitions(championshipId);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(competitionService, times(1)).findByChampionship(championshipId);
    }

    @Test
    void getCompetitions_ShouldReturnCompetitionsWithCorrectChampionship() {
        Integer championshipId = 1;
        List<CompetitionModel> competitions = Arrays.asList(competition1, competition2);
        when(competitionService.findByChampionship(championshipId)).thenReturn(competitions);

        List<CompetitionModel> result = controller.getCompetitions(championshipId);

        assertThat(result).isNotNull();
        assertThat(result).allMatch(c -> c.getChampionshipId().equals(championshipId));
        verify(competitionService, times(1)).findByChampionship(championshipId);
    }

    @Test
    void getCompetitions_ShouldReturnCompetitionsWithCorrectDates() {
        Integer championshipId = 1;
        List<CompetitionModel> competitions = Arrays.asList(competition1, competition2);
        when(competitionService.findByChampionship(championshipId)).thenReturn(competitions);

        List<CompetitionModel> result = controller.getCompetitions(championshipId);

        assertThat(result).isNotNull();
        assertThat(result.get(0).getStart()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(result.get(0).getEnd()).isEqualTo(LocalDate.of(2024, 6, 30));
        assertThat(result.get(1).getStart()).isEqualTo(LocalDate.of(2024, 7, 1));
        assertThat(result.get(1).getEnd()).isEqualTo(LocalDate.of(2024, 12, 31));
        verify(competitionService, times(1)).findByChampionship(championshipId);
    }

    @Test
    void constructor_ShouldInitializeServices() {
        ChampionshipController newController = new ChampionshipController(
                championshipService,
                competitionService
        );

        assertThat(newController).isNotNull();
    }

    @Test
    void getAll_ShouldHandleMultipleChampionships() {
        ChampionshipModel championship3 = new ChampionshipModel(
                "Championship 3 Description",
                LocalDate.of(2026, 12, 31),
                3,
                "Championship 3",
                LocalDate.of(2026, 1, 1)
        );

        List<ChampionshipModel> championships = Arrays.asList(championship1, championship2, championship3);
        when(championshipService.findAll()).thenReturn(championships);

        List<ChampionshipModel> result = controller.getAll();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(1).getId()).isEqualTo(2);
        assertThat(result.get(2).getId()).isEqualTo(3);
        verify(championshipService, times(1)).findAll();
    }

    @Test
    void getCompetitions_ShouldHandleMultipleCompetitions() {
        Integer championshipId = 1;
        CompetitionModel competition3 = new CompetitionModel(
                championshipEntity.getId(),
                "Competition 3 Description",
                LocalDate.of(2024, 3, 31),
                3,
                "Competition 3",
                LocalDate.of(2024, 1, 1)
        );

        List<CompetitionModel> competitions = Arrays.asList(competition1, competition2, competition3);
        when(competitionService.findByChampionship(championshipId)).thenReturn(competitions);

        List<CompetitionModel> result = controller.getCompetitions(championshipId);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(1).getId()).isEqualTo(2);
        assertThat(result.get(2).getId()).isEqualTo(3);
        verify(competitionService, times(1)).findByChampionship(championshipId);
    }

    @Test
    void getById_ShouldNotCallServiceMultipleTimes() {
        Integer id = 1;
        when(championshipService.findById(id)).thenReturn(Optional.of(championship1));

        controller.getById(id);

        verify(championshipService, times(1)).findById(id);
        verifyNoMoreInteractions(championshipService);
    }

    @Test
    void getCompetitions_ShouldNotCallServiceMultipleTimes() {
        Integer championshipId = 1;
        when(competitionService.findByChampionship(championshipId)).thenReturn(List.of());

        controller.getCompetitions(championshipId);

        verify(competitionService, times(1)).findByChampionship(championshipId);
        verifyNoMoreInteractions(competitionService);
    }

}