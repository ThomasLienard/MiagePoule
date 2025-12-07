package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.domains.ChampionshipModel;
import com.miage.pouleAPI.domains.CompetitionModel;
import com.miage.pouleAPI.entity.Championship;
import com.miage.pouleAPI.services.ChampionshipService;
import com.miage.pouleAPI.services.CompetitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChampionshipController.class)
class ChampionshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChampionshipService championshipService;

    @MockitoBean
    private CompetitionService competitionService;

    private ChampionshipModel championship1;
    private ChampionshipModel championship2;
    private Championship championshipEntity;
    private CompetitionModel competition1;
    private CompetitionModel competition2;

    @BeforeEach
    void setUp() {
        championship1 = new ChampionshipModel(
                "Championship 1 Description",
                LocalDate.of(2024, 12, 31),
                1,
                "Championship 1",
                LocalDate.of(2024, 1, 1)
        );

        championship2 = new ChampionshipModel(
                "Championship 2 Description",
                LocalDate.of(2024, 6, 30),
                2,
                "Championship 2",
                LocalDate.of(2024, 1, 1)
        );

        championshipEntity = new Championship(
                1,
                "Championship 1 Description",
                "Championship 1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        competition1 = new CompetitionModel(
                championshipEntity,
                "Competition 1 Description",
                LocalDate.of(2024, 6, 30),
                1,
                "Competition 1",
                LocalDate.of(2024, 1, 1)
        );

        competition2 = new CompetitionModel(
                championshipEntity,
                "Competition 2 Description",
                LocalDate.of(2024, 12, 31),
                2,
                "Competition 2",
                LocalDate.of(2024, 7, 1)
        );
    }

    @Test
    void getAll_ShouldReturnListOfChampionships() throws Exception {
        List<ChampionshipModel> championships = Arrays.asList(championship1, championship2);
        when(championshipService.findAll()).thenReturn(championships);

        mockMvc.perform(get("/championships")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Championship 1")))
                .andExpect(jsonPath("$[0].description", is("Championship 1 Description")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Championship 2")));

        verify(championshipService, times(1)).findAll();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoChampionships() throws Exception {
        when(championshipService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/championships")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(championshipService, times(1)).findAll();
    }

    @Test
    void getById_ShouldReturnChampionship_WhenExists() throws Exception {
        Integer championshipId = 1;
        when(championshipService.findById(championshipId)).thenReturn(Optional.of(championship1));

        mockMvc.perform(get("/championships/{id}", championshipId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Championship 1")))
                .andExpect(jsonPath("$.description", is("Championship 1 Description")))
                .andExpect(jsonPath("$.start", is("2024-01-01")))
                .andExpect(jsonPath("$.end", is("2024-12-31")));

        verify(championshipService, times(1)).findById(championshipId);
    }

    @Test
    void getById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        Integer championshipId = 999;
        when(championshipService.findById(championshipId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/championships/{id}", championshipId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(championshipService, times(1)).findById(championshipId);
    }

    @Test
    void getCompetitions_ShouldReturnListOfCompetitions() throws Exception {
        Integer championshipId = 1;
        List<CompetitionModel> competitions = Arrays.asList(competition1, competition2);
        when(competitionService.findByChampionship(championshipId)).thenReturn(competitions);

        mockMvc.perform(get("/championships/{id}/competitions", championshipId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Competition 1")))
                .andExpect(jsonPath("$[0].description", is("Competition 1 Description")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Competition 2")));

        verify(competitionService, times(1)).findByChampionship(championshipId);
    }

    @Test
    void getCompetitions_ShouldReturnEmptyList_WhenNoCompetitions() throws Exception {
        Integer championshipId = 1;
        when(competitionService.findByChampionship(championshipId)).thenReturn(List.of());

        mockMvc.perform(get("/championships/{id}/competitions", championshipId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(competitionService, times(1)).findByChampionship(championshipId);
    }

    @Test
    void getById_ShouldHandleInvalidId() throws Exception {
        when(championshipService.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/championships/{id}", 0)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCompetitions_ShouldReturnCompetitionsForValidChampionship() throws Exception {
        Integer championshipId = 1;
        List<CompetitionModel> competitions = List.of(competition1);
        when(competitionService.findByChampionship(championshipId)).thenReturn(competitions);

        mockMvc.perform(get("/championships/{id}/competitions", championshipId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].championship.id", is(1)));

        verify(competitionService, times(1)).findByChampionship(championshipId);
    }

    @Test
    void getAllEndpoint_ShouldBeAccessible() throws Exception {
        when(championshipService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/championships"))
                .andExpect(status().isOk());
    }

    @Test
    void getByIdEndpoint_ShouldBeAccessible() throws Exception {
        when(championshipService.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/championships/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCompetitionsEndpoint_ShouldBeAccessible() throws Exception {
        when(competitionService.findByChampionship(1)).thenReturn(List.of());

        mockMvc.perform(get("/championships/1/competitions"))
                .andExpect(status().isOk());
    }
}