package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.championship.ChampionshipDTO;
import com.miage.pouleAPI.repositories.adapters.ChampionshipJpaAdapter;
import com.miage.pouleAPI.services.impl.ChampionshipServiceImpl;
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
class ChampionshipServiceImplTest {

    @Mock
    private ChampionshipJpaAdapter championshipJpaAdapter;

    @InjectMocks
    private ChampionshipServiceImpl championshipService;

    private ChampionshipDTO championshipDTO1;
    private ChampionshipDTO championshipDTO2;

    @BeforeEach
    void setUp() {
        championshipDTO1 = new ChampionshipDTO(
                "Description Championship 1",
                LocalDate.of(2024, 12, 31),
                1,
                "Championship 1",
                LocalDate.of(2024, 1, 1)
        );

        championshipDTO2 = new ChampionshipDTO(
                "Description Championship 2",
                LocalDate.of(2024, 6, 30),
                2,
                "Championship 2",
                LocalDate.of(2024, 1, 1)
        );
    }

    @Test
    void findAll_ShouldReturnAllChampionships() {
        List<ChampionshipDTO> expectedChampionships = Arrays.asList(championshipDTO1, championshipDTO2);
        when(championshipJpaAdapter.findAll()).thenReturn(expectedChampionships);

        List<ChampionshipDTO> actualChampionships = championshipService.findAll();

        assertThat(actualChampionships).isNotNull();
        assertThat(actualChampionships).hasSize(2);
        assertThat(actualChampionships).containsExactlyInAnyOrder(championshipDTO1, championshipDTO2);
        verify(championshipJpaAdapter, times(1)).findAll();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoChampionships() {
        when(championshipJpaAdapter.findAll()).thenReturn(List.of());

        List<ChampionshipDTO> actualChampionships = championshipService.findAll();

        assertThat(actualChampionships).isNotNull();
        assertThat(actualChampionships).isEmpty();
        verify(championshipJpaAdapter, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnChampionship_WhenExists() {
        Integer championshipId = 1;
        when(championshipJpaAdapter.findById(championshipId)).thenReturn(Optional.of(championshipDTO1));

        Optional<ChampionshipDTO> actualChampionship = championshipService.findById(championshipId);

        assertThat(actualChampionship).isPresent();
        assertThat(actualChampionship.get()).isEqualTo(championshipDTO1);
        assertThat(actualChampionship.get().getId()).isEqualTo(championshipId);
        assertThat(actualChampionship.get().getName()).isEqualTo("Championship 1");
        verify(championshipJpaAdapter, times(1)).findById(championshipId);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Integer championshipId = 999;
        when(championshipJpaAdapter.findById(championshipId)).thenReturn(Optional.empty());

        Optional<ChampionshipDTO> actualChampionship = championshipService.findById(championshipId);

        assertThat(actualChampionship).isEmpty();
        verify(championshipJpaAdapter, times(1)).findById(championshipId);
    }

    @Test
    void save_ShouldReturnSavedChampionship() {
        ChampionshipDTO newChampionship = new ChampionshipDTO(
                "New Championship Description",
                LocalDate.of(2025, 12, 31),
                null,
                "New Championship",
                LocalDate.of(2025, 1, 1)
        );

        ChampionshipDTO savedChampionship = new ChampionshipDTO(
                "New Championship Description",
                LocalDate.of(2025, 12, 31),
                3,
                "New Championship",
                LocalDate.of(2025, 1, 1)
        );

        when(championshipJpaAdapter.save(any(ChampionshipDTO.class))).thenReturn(savedChampionship);

        ChampionshipDTO result = championshipService.save(newChampionship);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("New Championship");
        assertThat(result.getDescription()).isEqualTo("New Championship Description");
        assertThat(result.getStart()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.getEnd()).isEqualTo(LocalDate.of(2025, 12, 31));
        verify(championshipJpaAdapter, times(1)).save(newChampionship);
    }

    @Test
    void save_ShouldUpdateExistingChampionship() {
        ChampionshipDTO updatedChampionship = new ChampionshipDTO(
                "Updated Description",
                LocalDate.of(2024, 12, 31),
                1,
                "Updated Championship",
                LocalDate.of(2024, 1, 1)
        );

        when(championshipJpaAdapter.save(any(ChampionshipDTO.class))).thenReturn(updatedChampionship);

        ChampionshipDTO result = championshipService.save(updatedChampionship);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Updated Championship");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        verify(championshipJpaAdapter, times(1)).save(updatedChampionship);
    }

    @Test
    void constructor_ShouldInitializePort() {
        ChampionshipServiceImpl service = new ChampionshipServiceImpl(championshipJpaAdapter);

        assertThat(service).isNotNull();
    }
}