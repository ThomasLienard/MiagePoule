package com.miage.pouleAPI.services;

import com.miage.pouleAPI.adapters.CompetitionJpaAdapter;
import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import com.miage.pouleAPI.dtos.competition.CreateCompetitionRequestDTO;
import com.miage.pouleAPI.entity.Championship;

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
    private CompetitionDTO competitionDTO1;
    private CompetitionDTO competitionDTO2;

    @BeforeEach
    void setUp() {
        championship = new Championship(
                1,
                "Championship Description",
                "Championship 1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        competitionDTO1 = new CompetitionDTO(
                championship.getId(),                     // championshipId
                "Competition 1 Description",              // description
                LocalDate.of(2024, 6, 30),                // end
                1,                                        // id
                "Competition 1",                          // name
                LocalDate.of(2024, 1, 1)                  // start
        );

        competitionDTO2 = new CompetitionDTO(
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
        when(competitionJpaAdapter.findAll()).thenReturn(Arrays.asList(competitionDTO1, competitionDTO2));

        List<CompetitionDTO> actual = competitionService.findAll();

        assertThat(actual).hasSize(2);
        assertThat(actual).containsExactlyInAnyOrder(competitionDTO1, competitionDTO2);
        verify(competitionJpaAdapter).findAll();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoCompetitions() {
        when(competitionJpaAdapter.findAll()).thenReturn(List.of());

        List<CompetitionDTO> actual = competitionService.findAll();

        assertThat(actual).isEmpty();
        verify(competitionJpaAdapter).findAll();
    }

    @Test
    void findById_ShouldReturnCompetition_WhenExists() {
        when(competitionJpaAdapter.findById(1)).thenReturn(Optional.of(competitionDTO1));

        Optional<CompetitionDTO> actual = competitionService.findById(1);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(competitionDTO1);
        assertThat(actual.get().getId()).isEqualTo(1);
        assertThat(actual.get().getName()).isEqualTo("Competition 1");
        assertThat(actual.get().getChampionshipId()).isEqualTo(championship.getId());
        verify(competitionJpaAdapter).findById(1);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        when(competitionJpaAdapter.findById(999)).thenReturn(Optional.empty());

        Optional<CompetitionDTO> actual = competitionService.findById(999);

        assertThat(actual).isEmpty();
        verify(competitionJpaAdapter).findById(999);
    }

    @Test
    void save_ShouldReturnSavedCompetition() {
        CreateCompetitionRequestDTO req = new CreateCompetitionRequestDTO(
                "New Competition",                        // name
                "New Competition Description",            // description
                championship.getId(),                     // championshipId
                LocalDate.of(2025, 6, 30),                // end
                LocalDate.of(2025, 1, 1)                  // start
        );

        CompetitionDTO saved = new CompetitionDTO(
                championship.getId(),
                "New Competition Description",
                LocalDate.of(2025, 6, 30),
                3,
                "New Competition",
                LocalDate.of(2025, 1, 1)
        );

        when(competitionJpaAdapter.save(any(CreateCompetitionRequestDTO.class))).thenReturn(saved);

        CompetitionDTO result = competitionService.save(req);

        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("New Competition");
        assertThat(result.getDescription()).isEqualTo("New Competition Description");
        assertThat(result.getStart()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.getEnd()).isEqualTo(LocalDate.of(2025, 6, 30));
        assertThat(result.getChampionshipId()).isEqualTo(championship.getId());
        verify(competitionJpaAdapter).save(req);
    }

    @Test
    void update_ShouldCallAdapterUpdate() {
        CompetitionDTO updateDTO = new CompetitionDTO(
                championship.getId(),
                "Updated Description",
                LocalDate.of(2024, 6, 30),
                1,
                "Updated Competition",
                LocalDate.of(2024, 1, 1)
        );

        when(competitionJpaAdapter.update(any(CompetitionDTO.class))).thenReturn(updateDTO);

        CompetitionDTO result = competitionService.update(updateDTO);

        assertThat(result).isEqualTo(updateDTO);
        verify(competitionJpaAdapter).update(updateDTO);
    }

    @Test
    void findByChampionship_ShouldReturnCompetitions() {
        when(competitionJpaAdapter.findByChampionshipId(1)).thenReturn(List.of(competitionDTO1, competitionDTO2));

        List<CompetitionDTO> actual = competitionService.findByChampionship(1);

        assertThat(actual).hasSize(2);
        assertThat(actual).allMatch(c -> c.getChampionshipId().equals(1));
        verify(competitionJpaAdapter).findByChampionshipId(1);
    }

    @Test
    void findByChampionship_ShouldReturnEmptyList_WhenNone() {
        when(competitionJpaAdapter.findByChampionshipId(999)).thenReturn(List.of());

        List<CompetitionDTO> actual = competitionService.findByChampionship(999);

        assertThat(actual).isEmpty();
        verify(competitionJpaAdapter).findByChampionshipId(999);
    }

    @Test
    void constructor_ShouldInitializeService() {
        CompetitionServiceImpl service = new CompetitionServiceImpl(competitionJpaAdapter);
        assertThat(service).isNotNull();
    }
}