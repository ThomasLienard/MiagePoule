package com.miage.pouleAPI.services;

import com.miage.pouleAPI.domains.CompetitionModel;
import com.miage.pouleAPI.entity.Championship;
import com.miage.pouleAPI.repositories.adapters.CompetitionJpaAdapter;
import com.miage.pouleAPI.services.impl.CompetitionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompetitionServiceImplTest {

    @Mock
    private CompetitionJpaAdapter competitionJpaAdapter;

    @InjectMocks
    private CompetitionServiceImpl competitionService;

    private Championship championship;
    private CompetitionModel competitionModel1;
    private CompetitionModel competitionModel2;

    @BeforeEach
    void setUp() {
        championship = new Championship(
                1,
                "Championship Description",
                "Championship 1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        competitionModel1 = new CompetitionModel(
                championship.getId(),
                "Competition 1 Description",
                LocalDate.of(2024, 6, 30),
                1,
                "Competition 1",
                LocalDate.of(2024, 1, 1)
        );

        competitionModel2 = new CompetitionModel(
                championship.getId(),
                "Competition 2 Description",
                LocalDate.of(2024, 9, 30),
                2,
                "Competition 2",
                LocalDate.of(2024, 7, 1)
        );
    }

    @Test
    void findAll_ShouldReturnAllCompetitions() {
        List<CompetitionModel> expectedCompetitions = Arrays.asList(competitionModel1, competitionModel2);
        when(competitionJpaAdapter.findAll()).thenReturn(expectedCompetitions);

        List<CompetitionModel> actualCompetitions = competitionService.findAll();

        assertThat(actualCompetitions).isNotNull();
        assertThat(actualCompetitions).hasSize(2);
        assertThat(actualCompetitions).containsExactlyInAnyOrder(competitionModel1, competitionModel2);
        verify(competitionJpaAdapter, times(1)).findAll();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoCompetitions() {
        when(competitionJpaAdapter.findAll()).thenReturn(List.of());

        List<CompetitionModel> actualCompetitions = competitionService.findAll();

        assertThat(actualCompetitions).isNotNull();
        assertThat(actualCompetitions).isEmpty();
        verify(competitionJpaAdapter, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnCompetition_WhenExists() {
        Integer competitionId = 1;
        when(competitionJpaAdapter.findById(competitionId)).thenReturn(Optional.of(competitionModel1));

        Optional<CompetitionModel> actualCompetition = competitionService.findById(competitionId);

        assertThat(actualCompetition).isPresent();
        assertThat(actualCompetition.get()).isEqualTo(competitionModel1);
        assertThat(actualCompetition.get().getId()).isEqualTo(competitionId);
        assertThat(actualCompetition.get().getName()).isEqualTo("Competition 1");
        assertThat(actualCompetition.get().getChampionshipId()).isEqualTo(championship.getId());
        verify(competitionJpaAdapter, times(1)).findById(competitionId);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Integer competitionId = 999;
        when(competitionJpaAdapter.findById(competitionId)).thenReturn(Optional.empty());

        Optional<CompetitionModel> actualCompetition = competitionService.findById(competitionId);

        assertThat(actualCompetition).isEmpty();
        verify(competitionJpaAdapter, times(1)).findById(competitionId);
    }

    @Test
    void save_ShouldReturnSavedCompetition() {
        CompetitionModel newCompetition = new CompetitionModel(
                championship.getId(),
                "New Competition Description",
                LocalDate.of(2025, 6, 30),
                null,
                "New Competition",
                LocalDate.of(2025, 1, 1)
        );

        CompetitionModel savedCompetition = new CompetitionModel(
                championship.getId(),
                "New Competition Description",
                LocalDate.of(2025, 6, 30),
                3,
                "New Competition",
                LocalDate.of(2025, 1, 1)
        );

        when(competitionJpaAdapter.save(any(CompetitionModel.class))).thenReturn(savedCompetition);

        CompetitionModel result = competitionService.save(newCompetition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("New Competition");
        assertThat(result.getDescription()).isEqualTo("New Competition Description");
        assertThat(result.getStart()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.getEnd()).isEqualTo(LocalDate.of(2025, 6, 30));
        assertThat(result.getChampionshipId()).isEqualTo(championship.getId());
        verify(competitionJpaAdapter, times(1)).save(newCompetition);
    }

    @Test
    void save_ShouldUpdateExistingCompetition() {
        CompetitionModel updatedCompetition = new CompetitionModel(
                championship.getId(),
                "Updated Description",
                LocalDate.of(2024, 6, 30),
                1,
                "Updated Competition",
                LocalDate.of(2024, 1, 1)
        );

        when(competitionJpaAdapter.save(any(CompetitionModel.class))).thenReturn(updatedCompetition);

        CompetitionModel result = competitionService.save(updatedCompetition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Updated Competition");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        verify(competitionJpaAdapter, times(1)).save(updatedCompetition);
    }

    @Test
    void findByChampionship_ShouldReturnCompetitions_WhenChampionshipExists() {
        Integer championshipId = 1;
        List<CompetitionModel> expectedCompetitions = Arrays.asList(competitionModel1, competitionModel2);
        when(competitionJpaAdapter.findByChampionshipId(championshipId)).thenReturn(expectedCompetitions);

        List<CompetitionModel> actualCompetitions = competitionService.findByChampionship(championshipId);

        assertThat(actualCompetitions).isNotNull();
        assertThat(actualCompetitions).hasSize(2);
        assertThat(actualCompetitions).containsExactlyInAnyOrder(competitionModel1, competitionModel2);
        assertThat(actualCompetitions).allMatch(c -> c.getChampionshipId().equals(championshipId));
        verify(competitionJpaAdapter, times(1)).findByChampionshipId(championshipId);
    }

    @Test
    void findByChampionship_ShouldReturnEmptyList_WhenNoCompetitionsForChampionship() {
        Integer championshipId = 999;
        when(competitionJpaAdapter.findByChampionshipId(championshipId)).thenReturn(List.of());

        List<CompetitionModel> actualCompetitions = competitionService.findByChampionship(championshipId);

        assertThat(actualCompetitions).isNotNull();
        assertThat(actualCompetitions).isEmpty();
        verify(competitionJpaAdapter, times(1)).findByChampionshipId(championshipId);
    }

    @Test
    void findByChampionship_ShouldReturnOnlyCompetitionsFromSpecificChampionship() {
        Integer championshipId = 1;
        List<CompetitionModel> expectedCompetitions = List.of(competitionModel1);
        when(competitionJpaAdapter.findByChampionshipId(championshipId)).thenReturn(expectedCompetitions);

        List<CompetitionModel> actualCompetitions = competitionService.findByChampionship(championshipId);

        assertThat(actualCompetitions).hasSize(1);
        assertThat(actualCompetitions.get(0).getChampionshipId()).isEqualTo(championshipId);
        verify(competitionJpaAdapter, times(1)).findByChampionshipId(championshipId);
    }

    @Test
    void constructor_ShouldInitializePort() {
        CompetitionServiceImpl service = new CompetitionServiceImpl(competitionJpaAdapter);

        assertThat(service).isNotNull();
    }
}