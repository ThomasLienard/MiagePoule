package com.miage.pouleAPI.repositories.adapters;

import com.miage.pouleAPI.adapters.ChampionshipJpaAdapter;
import com.miage.pouleAPI.dtos.championship.ChampionshipDTO;
import com.miage.pouleAPI.dtos.championship.CreateChampionshipRequestDTO;
import com.miage.pouleAPI.entity.Championship;
import com.miage.pouleAPI.repositories.ChampionshipRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ChampionshipJpaAdapterTest {

    @Mock
    private ChampionshipRepository repository;

    private ChampionshipJpaAdapter adapter;

    private Championship championship1;
    private Championship championship2;
    private CreateChampionshipRequestDTO championshipDTO1;
    private ChampionshipDTO championshipDTO2;

    @BeforeEach
    void setUp() {
        adapter = new ChampionshipJpaAdapter(repository);

        championship1 = new Championship(
                1,
                "Championship 1 Description",
                "Championship 1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        championship2 = new Championship(
                2,
                "Championship 2 Description",
                "Championship 2",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
        );

        championshipDTO1 = new CreateChampionshipRequestDTO(
                "Championship 1 Description",
                "Championship 1",
                LocalDate.of(2024, 12, 31),
                LocalDate.of(2024, 1, 1)
        );

        championshipDTO2 = new ChampionshipDTO(
                "Championship 1 Description",
                LocalDate.of(2024, 12, 31),
                4,
                "Championship 1",
                LocalDate.of(2024, 1, 1)
        );
    }

    @Test
    void findAll_ShouldReturnListOfChampionshipModels() {
        List<Championship> entities = Arrays.asList(championship1, championship2);
        when(repository.findAll()).thenReturn(entities);

        List<ChampionshipDTO> result = adapter.findAll();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Championship 1");
        assertThat(result.get(0).getDescription()).isEqualTo("Championship 1 Description");
        assertThat(result.get(1).getId()).isEqualTo(2);
        verify(repository, times(1)).findAll();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoEntities() {
        when(repository.findAll()).thenReturn(List.of());

        List<ChampionshipDTO> result = adapter.findAll();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(repository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnChampionshipModel_WhenExists() {
        Integer id = 1;
        when(repository.findById(id)).thenReturn(Optional.of(championship1));

        Optional<ChampionshipDTO> result = adapter.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getName()).isEqualTo("Championship 1");
        assertThat(result.get().getDescription()).isEqualTo("Championship 1 Description");
        assertThat(result.get().getStart()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(result.get().getEnd()).isEqualTo(LocalDate.of(2024, 12, 31));
        verify(repository, times(1)).findById(id);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Integer id = 999;
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<ChampionshipDTO> result = adapter.findById(id);

        assertThat(result).isEmpty();
        verify(repository, times(1)).findById(id);
    }

    @Test
    void save_ShouldConvertAndSaveChampionshipModel() {
        CreateChampionshipRequestDTO modelToSave = new CreateChampionshipRequestDTO(
                "New Championship Description",
                "New Championship",
                LocalDate.of(2025, 12, 31),
                LocalDate.of(2025, 1, 1)
        );

        Championship savedEntity = new Championship(
                3,
                "New Championship Description",
                "New Championship",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );

        when(repository.save(any(Championship.class))).thenReturn(savedEntity);

        ChampionshipDTO result = adapter.save(modelToSave);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("New Championship");
        assertThat(result.getDescription()).isEqualTo("New Championship Description");
        assertThat(result.getStart()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.getEnd()).isEqualTo(LocalDate.of(2025, 12, 31));
        verify(repository, times(1)).save(any(Championship.class));
    }

    @Test
    void save_ShouldHandleNullModel() {
        when(repository.save(any())).thenReturn(null);

        ChampionshipDTO result = adapter.save(null);

        assertThat(result).isNull();
    }

    @Test
    void toDomain_ShouldConvertEntityToModel() {
        when(repository.findById(1)).thenReturn(Optional.of(championship1));

        Optional<ChampionshipDTO> result = adapter.findById(1);

        assertThat(result).isPresent();
        ChampionshipDTO model = result.get();
        assertThat(model.getId()).isEqualTo(championship1.getId());
        assertThat(model.getName()).isEqualTo(championship1.getName());
        assertThat(model.getDescription()).isEqualTo(championship1.getDescription());
        assertThat(model.getStart()).isEqualTo(championship1.getStart());
        assertThat(model.getEnd()).isEqualTo(championship1.getEnd());
    }

    @Test
    void toEntity_ShouldConvertModelToEntity() {
        Championship savedEntity = new Championship(
                4, // L'ID généré par la BDD(simulé)
                championshipDTO1.getDescription(),
                championshipDTO1.getName(),
                championshipDTO1.getStart(),
                championshipDTO1.getEnd()
        );

        when(repository.save(any(Championship.class))).thenReturn(savedEntity);

        ChampionshipDTO result = adapter.save(championshipDTO1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(4);

        verify(repository).save(argThat(entity ->
                entity.getId() == null && //car auto-généré en base
                        entity.getName().equals(championshipDTO1.getName()) &&
                        entity.getDescription().equals(championshipDTO1.getDescription())
        ));
    }

    @Test
    void findAll_ShouldHandleNullEntitiesInList() {
        when(repository.findAll()).thenReturn(Arrays.asList(championship1, null, championship2));

        List<ChampionshipDTO> result = adapter.findAll();

        assertThat(result).hasSize(3);
        assertThat(result).containsNull();
        verify(repository, times(1)).findAll();
    }

    @Test
    void constructor_ShouldInitializeRepository() {
        ChampionshipJpaAdapter newAdapter = new ChampionshipJpaAdapter(repository);

        assertThat(newAdapter).isNotNull();
    }

    @Test
    void save_ShouldPreserveAllFields() {
        ChampionshipDTO completeModel = new ChampionshipDTO(
                "Complete Description",
                LocalDate.of(2024, 12, 31),
                5,
                "Complete Championship",
                LocalDate.of(2024, 1, 1)
        );

        CreateChampionshipRequestDTO completeRequest = new CreateChampionshipRequestDTO(
                "Complete Description",
                "Complete Championship",
                LocalDate.of(2024, 12, 31),
                LocalDate.of(2024, 1, 1)
        );

        Championship savedEntity = new Championship(
                5,
                "Complete Description",
                "Complete Championship",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        when(repository.save(any(Championship.class))).thenReturn(savedEntity);

        ChampionshipDTO result = adapter.save(completeRequest);

        assertThat(result.getId()).isEqualTo(completeModel.getId());
        assertThat(result.getName()).isEqualTo(completeModel.getName());
        assertThat(result.getDescription()).isEqualTo(completeModel.getDescription());
        assertThat(result.getStart()).isEqualTo(completeModel.getStart());
        assertThat(result.getEnd()).isEqualTo(completeModel.getEnd());
    }
}