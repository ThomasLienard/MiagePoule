package com.miage.pouleAPI.repositories.adapters;

import com.miage.pouleAPI.adapters.CompetitionJpaAdapter;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import com.miage.pouleAPI.dtos.competition.CreateCompetitionRequestDTO;
import com.miage.pouleAPI.entity.Championship;
import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.repositories.ChampionshipRepository;
import com.miage.pouleAPI.repositories.CompetitionRepository;

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
class CompetitionJpaAdapterTest {

    @Mock
    private CompetitionRepository repository;

    @Mock
    private ApplicationUserRepository userRepository;

    @Mock
    private ChampionshipRepository championshipRepository;

    private CompetitionJpaAdapter adapter;

    private Championship championship;
    private Competition competition1;
    private Competition competition2;
    private CompetitionDTO competitionDTO1;
    private CreateCompetitionRequestDTO competitionDTORequest1;

    @BeforeEach
    void setUp() {
        adapter = new CompetitionJpaAdapter(repository,championshipRepository, userRepository);

        championship = new Championship(
                1,
                "Championship Description",
                "Championship 1",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        competition1 = new Competition(
                1,
                "Competition 1",
                "Competition 1 Description",
                championship,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
        );

        competition2 = new Competition(
                2,
                "Competition 2",
                "Competition 2 Description",
                championship,
                LocalDate.of(2024, 7, 1),
                LocalDate.of(2024, 12, 31)
        );

        competitionDTO1 = new CompetitionDTO(
                championship.getId(),
                "Competition 1 Description",
                LocalDate.of(2024, 6, 30),
                1,
                "Competition 1",
                LocalDate.of(2024, 1, 1)
        );

        competitionDTORequest1 = new CreateCompetitionRequestDTO(
                "Competition 1",
                "Competition 1 Description",
                championship.getId(),
                LocalDate.of(2024, 6, 30),
                LocalDate.of(2024, 1, 1)
        );
    }

    @Test
    void findAll_ShouldReturnListOfCompetitionModels() {
        List<Competition> entities = Arrays.asList(competition1, competition2);
        when(repository.findAll()).thenReturn(entities);

        List<CompetitionDTO> result = adapter.findAll();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(1);
        assertThat(result.getFirst().getName()).isEqualTo("Competition 1");
        assertThat(result.get(0).getDescription()).isEqualTo("Competition 1 Description");
        assertThat(result.get(0).getChampionshipId()).isEqualTo(championship.getId());
        assertThat(result.get(1).getId()).isEqualTo(2);
        verify(repository, times(1)).findAll();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoEntities() {
        when(repository.findAll()).thenReturn(List.of());

        List<CompetitionDTO> result = adapter.findAll();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(repository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnCompetitionModel_WhenExists() {
        Integer id = 1;
        when(repository.findById(id)).thenReturn(Optional.of(competition1));

        Optional<CompetitionDTO> result = adapter.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getName()).isEqualTo("Competition 1");
        assertThat(result.get().getDescription()).isEqualTo("Competition 1 Description");
        assertThat(result.get().getChampionshipId()).isEqualTo(championship.getId());
        assertThat(result.get().getStart()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(result.get().getEnd()).isEqualTo(LocalDate.of(2024, 6, 30));
        verify(repository, times(1)).findById(id);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Integer id = 999;
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<CompetitionDTO> result = adapter.findById(id);

        assertThat(result).isEmpty();
        verify(repository, times(1)).findById(id);
    }

    @Test
    void save_ShouldConvertAndSaveCompetitionModel() {
        when(championshipRepository.findById(championship.getId()))
                .thenReturn(Optional.of(championship));


        CreateCompetitionRequestDTO requestToSave = new CreateCompetitionRequestDTO(
                "New Competition",
                "New Competition Description",
                championship.getId(),
                LocalDate.of(2025, 6, 30),
                LocalDate.of(2025, 1, 1)
        );

        Competition savedEntity = new Competition(
                3,
                "New Competition",
                "New Competition Description",
                championship,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30)
        );

        when(repository.save(any(Competition.class))).thenReturn(savedEntity);

        CompetitionDTO result = adapter.save(requestToSave);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getName()).isEqualTo("New Competition");
        assertThat(result.getDescription()).isEqualTo("New Competition Description");
        assertThat(result.getChampionshipId()).isEqualTo(championship.getId());
        assertThat(result.getStart()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.getEnd()).isEqualTo(LocalDate.of(2025, 6, 30));
        verify(repository, times(1)).save(any(Competition.class));
    }

    @Test
    void save_ShouldHandleNullModel() {
        when(repository.save(any())).thenReturn(null);

        CompetitionDTO result = adapter.save(null);

        assertThat(result).isNull();
    }

    @Test
    void findByChampionshipId_ShouldReturnCompetitionsForChampionship() {
        Integer championshipId = 1;
        List<Competition> entities = Arrays.asList(competition1, competition2);
        when(repository.findByChampionship_Id(championshipId)).thenReturn(entities);

        List<CompetitionDTO> result = adapter.findByChampionshipId(championshipId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> c.getChampionshipId().equals(championshipId));
        verify(repository, times(1)).findByChampionship_Id(championshipId);
    }

    @Test
    void findByChampionshipId_ShouldReturnEmptyList_WhenNoCompetitions() {
        Integer championshipId = 999;
        when(repository.findByChampionship_Id(championshipId)).thenReturn(List.of());

        List<CompetitionDTO> result = adapter.findByChampionshipId(championshipId);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(repository, times(1)).findByChampionship_Id(championshipId);
    }

    @Test
    void toDomain_ShouldConvertEntityToModel_WithAllFields() {
        when(repository.findById(1)).thenReturn(Optional.of(competition1));

        Optional<CompetitionDTO> result = adapter.findById(1);

        assertThat(result).isPresent();
        CompetitionDTO model = result.get();
        assertThat(model.getId()).isEqualTo(competition1.getId());
        assertThat(model.getName()).isEqualTo(competition1.getName());
        assertThat(model.getDescription()).isEqualTo(competition1.getDescription());
        assertThat(model.getChampionshipId()).isEqualTo(competition1.getChampionship().getId());
        assertThat(model.getStart()).isEqualTo(competition1.getStart());
        assertThat(model.getEnd()).isEqualTo(competition1.getEnd());
    }

    @Test
    void toEntity_ShouldConvertModelToEntity_WithAllFields() {
        when(championshipRepository.findById(championship.getId()))
                .thenReturn(Optional.of(championship));

        CreateCompetitionRequestDTO modelToSave = competitionDTORequest1;
        CompetitionDTO modelSaved = competitionDTO1;

        Competition entityToSave = new Competition(
                modelSaved.getId(),
                modelSaved.getName(),
                modelSaved.getDescription(),
                championship,
                modelSaved.getStart(),
                modelSaved.getEnd()
        );

        when(repository.save(any(Competition.class))).thenReturn(entityToSave);

        CompetitionDTO result = adapter.save(modelToSave);

        assertThat(result).isNotNull();
        verify(repository).save(any(Competition.class));
    }


    @Test
    void findAll_ShouldHandleNullEntitiesInList() {
        when(repository.findAll()).thenReturn(Arrays.asList(competition1, null, competition2));

        List<CompetitionDTO> result = adapter.findAll();

        assertThat(result).hasSize(3);
        assertThat(result).containsNull();
        verify(repository, times(1)).findAll();
    }

    @Test
    void constructor_ShouldInitializeRepository() {
        CompetitionJpaAdapter newAdapter = new CompetitionJpaAdapter(repository,championshipRepository, userRepository);

        assertThat(newAdapter).isNotNull();
    }

    @Test
    void findByChampionshipId_ShouldConvertAllEntitiesCorrectly() {
        Integer championshipId = 1;
        Competition competition3 = new Competition(
                3,
                "Competition 3",
                "Description 3",
                championship,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 31)
        );
        
        when(repository.findByChampionship_Id(championshipId))
                .thenReturn(Arrays.asList(competition1, competition2, competition3));

        List<CompetitionDTO> result = adapter.findByChampionshipId(championshipId);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(1).getId()).isEqualTo(2);
        assertThat(result.get(2).getId()).isEqualTo(3);
        assertThat(result).allMatch(c -> c.getChampionshipId().equals(championshipId));
    }

    @Test
    void save_ShouldHandleUpdateOfExistingCompetition() {
        when(championshipRepository.findById(championship.getId()))
                .thenReturn(Optional.of(championship));
        CreateCompetitionRequestDTO existingModel = new CreateCompetitionRequestDTO(
                "Updated Competition",
                "Updated Description",
                championship.getId(),
                LocalDate.of(2024, 6, 30),
                LocalDate.of(2024, 1, 1)
        );

        Competition updatedEntity = new Competition(
                1,
                "Updated Competition",
                "Updated Description",
                championship,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
        );

        when(repository.save(any(Competition.class))).thenReturn(updatedEntity);

        CompetitionDTO result = adapter.save(existingModel);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Updated Competition");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        verify(repository, times(1)).save(any(Competition.class));
    }

    @Test
    void findByChampionshipId_ShouldHandleEmptyResultFromRepository() {
        Integer nonExistentChampionshipId = 999;
        when(repository.findByChampionship_Id(nonExistentChampionshipId)).thenReturn(List.of());

        List<CompetitionDTO> result = adapter.findByChampionshipId(nonExistentChampionshipId);

        assertThat(result).isEmpty();
        verify(repository, times(1)).findByChampionship_Id(nonExistentChampionshipId);
    }

    @Test
    void toDomain_ShouldHandleNullEntity() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        Optional<CompetitionDTO> result = adapter.findById(1);

        assertThat(result).isEmpty();
    }
}