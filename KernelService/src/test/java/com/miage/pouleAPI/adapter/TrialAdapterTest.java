package com.miage.pouleAPI.adapter;

import com.miage.pouleAPI.adapters.TrialAdapter;
import com.miage.pouleAPI.dtos.trial.TrialDetailDTO;
import com.miage.pouleAPI.dtos.trial.TrialSummaryDTO;
import com.miage.pouleAPI.dtos.place.PlaceDTO;
import com.miage.pouleAPI.dtos.timeslot.TimeSlotDTO;
import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.Place;
import com.miage.pouleAPI.entity.TimeSlot;
import com.miage.pouleAPI.entity.Trial;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Team;
import com.miage.pouleAPI.entity.ParticipateAt;
import com.miage.pouleAPI.entity.IsConvenedTo;
import com.miage.pouleAPI.entity.TypeScore;
import com.miage.pouleAPI.repositories.IsConvenedToRepository;
import com.miage.pouleAPI.repositories.ParticipateAtRepository;
import com.miage.pouleAPI.strategy.RankingStrategyFactory;
import com.miage.pouleAPI.strategy.TimeRankingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrialAdapter Tests")
class TrialAdapterTest {

    @Mock
    private ParticipateAtRepository participateAtRepository;
    
    @Mock
    private IsConvenedToRepository isConvenedToRepository;
    
    @Mock
    private RankingStrategyFactory rankingStrategyFactory;

    private TrialAdapter trialAdapter;
    private Trial trial;
    private Event event;
    private Competition competition;
    private TimeSlot timeSlot;
    private Place place;

    @BeforeEach
    void setUp() {
        trialAdapter = new TrialAdapter(participateAtRepository, isConvenedToRepository, rankingStrategyFactory);
        
        // Mock the ranking strategy factory to return TimeRankingStrategy by default
        lenient().when(rankingStrategyFactory.getStrategy(anyString())).thenReturn(new TimeRankingStrategy());
        
        // Mock empty rankings for tests that don't specifically test rankings (lenient to avoid UnnecessaryStubbingException)
        lenient().when(participateAtRepository.findByTrialIdOrderedByResult(anyInt())).thenReturn(Collections.emptyList());
        lenient().when(isConvenedToRepository.findByTrialIdOrderedByResult(anyInt())).thenReturn(Collections.emptyList());
        lenient().when(participateAtRepository.findByTrialIdOrderedByResultDynamic(anyInt(), anyString())).thenReturn(Collections.emptyList());
        lenient().when(isConvenedToRepository.findByTrialIdOrderedByResultDynamic(anyInt(), anyString())).thenReturn(Collections.emptyList());

        competition = new Competition();
        competition.setId(1);
        competition.setName("Championnats de France");

        timeSlot = new TimeSlot();
        timeSlot.setStart(LocalDateTime.of(2025, 6, 20, 8, 0));
        timeSlot.setEnd(LocalDateTime.of(2025, 6, 20, 14, 0));

        place = new Place();
        place.setId(2);
        place.setName("Stade Olympique");
        place.setDescription("Main stadium");
        place.setStreet("Avenue Pierre de Coubertin");
        place.setNumber("1");
        place.setCity("Paris");
        place.setZip("75012");
        place.setParking(true);
        place.setLatitude(48.8467);
        place.setLongitude(2.3775);

        event = new Event();
        event.setId(10);
        event.setName("Marathon de Paris");
        event.setDescription("42km course");
        event.setCompetition(competition);
        event.setTimeSlot(timeSlot);
        event.setPlace(place);

        TypeScore typeScore = new TypeScore();
        typeScore.setName("TIME");

        trial = new Trial();
        trial.setId(1);
        trial.setName(event.getName());
        trial.setDescription(event.getDescription());
        trial.setCompetition(event.getCompetition());
        trial.setTimeSlot(event.getTimeSlot());
        trial.setPlace(event.getPlace());
        trial.setTypeScore(typeScore);
    }

    @Test
    @DisplayName("entityToSummaryDto() - Devrait convertir Trial en TrialSummaryDTO avec idEvent")
    void testEntityToSummaryDto_Success() {
        // When
        TrialSummaryDTO dto = trialAdapter.entityToSummaryDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals(1, dto.getIdEvent());  // Trial's id IS the Event's id (JOINED inheritance)
        assertEquals("Marathon de Paris", dto.getName());
        assertEquals("42km course", dto.getDescription());
    }

    @Test
    @DisplayName("entityToSummaryDto() - Devrait retourner null pour Trial null")
    void testEntityToSummaryDto_NullTrial() {
        // When
        TrialSummaryDTO dto = trialAdapter.entityToSummaryDto(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("entityToSummaryDto() - Devrait retourner DTO pour Trial même sans Event séparé")
    void testEntityToSummaryDto_NullEvent() {
        // Given - Trial IS-A Event now, no separate Event
        Trial emptyTrial = new Trial();
        emptyTrial.setId(1);
        emptyTrial.setName(null);
        emptyTrial.setDescription(null);

        // When
        TrialSummaryDTO dto = trialAdapter.entityToSummaryDto(emptyTrial);

        // Then
        assertNotNull(dto);  // Returns DTO even with null fields (Trial IS Event)
        assertEquals(1, dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
    }

    @Test
    @DisplayName("entityListToSummaryDtoList() - Devrait convertir liste de Trials avec idEvent")
    void testEntityListToSummaryDtoList_Success() {
        // Given
        Trial trial2 = new Trial();
        trial2.setId(2);
        trial2.setName("100m Sprint");
        trial2.setDescription("Sprint rapide");
        
        List<Trial> trials = Arrays.asList(trial, trial2);

        // When
        List<TrialSummaryDTO> dtos = trialAdapter.entityListToSummaryDtoList(trials);

        // Then
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals(1, dtos.get(0).getId());
        assertEquals(1, dtos.get(0).getIdEvent());  // Trial's id IS Event's id
        assertEquals("Marathon de Paris", dtos.get(0).getName());
        assertEquals(2, dtos.get(1).getId());
        assertEquals(2, dtos.get(1).getIdEvent());  // Trial's id IS Event's id
        assertEquals("100m Sprint", dtos.get(1).getName());
    }

    @Test
    @DisplayName("entityListToSummaryDtoList() - Devrait inclure tous les Trials")
    void testEntityListToSummaryDtoList_FilterNullEvents() {
        // Given - Trial IS-A Event, no separate filtering needed
        Trial trialWithoutName = new Trial();
        trialWithoutName.setId(2);
        trialWithoutName.setName(null);
        
        List<Trial> trials = Arrays.asList(trial, trialWithoutName);

        // When
        List<TrialSummaryDTO> dtos = trialAdapter.entityListToSummaryDtoList(trials);

        // Then
        assertNotNull(dtos);
        assertEquals(2, dtos.size());  // Both trials included (no Event null check)
        assertEquals("Marathon de Paris", dtos.get(0).getName());
        assertEquals(1, dtos.get(0).getIdEvent());
        assertNull(dtos.get(1).getName());  // Second trial has null name
        assertEquals(2, dtos.get(1).getIdEvent());
    }

    @Test
    @DisplayName("entityListToSummaryDtoList() - Devrait retourner liste vide pour liste vide")
    void testEntityListToSummaryDtoList_EmptyList() {
        // When
        List<TrialSummaryDTO> dtos = trialAdapter.entityListToSummaryDtoList(Collections.emptyList());

        // Then
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    @DisplayName("entityListToSummaryDtoList() - Devrait retourner liste vide pour liste null")
    void testEntityListToSummaryDtoList_NullList() {
        // When
        List<TrialSummaryDTO> dtos = trialAdapter.entityListToSummaryDtoList(null);

        // Then
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait convertir Trial complet en TrialDetailDTO")
    void testEntityToDetailDto_Complete() {
        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals("Marathon de Paris", dto.getName());
        assertEquals("42km course", dto.getDescription());
        assertEquals("Championnats de France", dto.getCompetitionName());
        
        assertNotNull(dto.getTimeSlot());
        assertEquals(timeSlot.getStart(), dto.getTimeSlot().getStart());
        assertEquals(timeSlot.getEnd(), dto.getTimeSlot().getEnd());
        
        assertNotNull(dto.getPlace());
        assertEquals("Stade Olympique", dto.getPlace().getName());
        assertEquals("Paris", dto.getPlace().getCity());
        assertEquals(48.8467, dto.getPlace().getLatitude());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait gérer Trial sans Competition")
    void testEntityToDetailDto_WithoutCompetition() {
        // Given - Trial IS Event, set competition on trial
        trial.setCompetition(null);

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertNull(dto.getCompetitionName());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait gérer Trial sans TimeSlot")
    void testEntityToDetailDto_WithoutTimeSlot() {
        // Given - Trial IS Event, set timeSlot on trial
        trial.setTimeSlot(null);

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertNull(dto.getTimeSlot());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait gérer Trial sans Place")
    void testEntityToDetailDto_WithoutPlace() {
        // Given - Trial IS Event, set place on trial
        trial.setPlace(null);

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertNull(dto.getPlace());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait retourner null pour Trial null")
    void testEntityToDetailDto_NullTrial() {
        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait retourner DTO pour Trial m\u00eame avec attributs null")
    void testEntityToDetailDto_NullEvent() {
        // Given - Trial IS-A Event now, no separate Event check needed
        Trial emptyTrial = new Trial();
        emptyTrial.setId(1);
        emptyTrial.setName(null);
        emptyTrial.setDescription(null);

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(emptyTrial);

        // Then
        assertNotNull(dto);  // Returns DTO even with null fields
        assertEquals(1, dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
    }

    @Test
    @DisplayName("summaryDtoToEntity() - Devrait convertir TrialSummaryDTO en Trial")
    void testSummaryDtoToEntity_Success() {
        // Given
        TrialSummaryDTO dto = new TrialSummaryDTO(1, 10, "Test Trial", "Test Description", false);

        // When
        Trial result = trialAdapter.summaryDtoToEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNotNull(result);
        assertEquals("Test Trial", result.getName());
        assertEquals("Test Description", result.getDescription());
    }

    @Test
    @DisplayName("summaryDtoToEntity() - Devrait retourner null pour DTO null")
    void testSummaryDtoToEntity_NullDto() {
        // When
        Trial result = trialAdapter.summaryDtoToEntity(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("detailDtoToEntity() - Devrait convertir TrialDetailDTO en Trial")
    void testDetailDtoToEntity_Success() {
        // Given
        TrialDetailDTO dto = new TrialDetailDTO();
        dto.setId(1);
        dto.setName("Test Trial");
        dto.setDescription("Test Description");

        // When
        Trial result = trialAdapter.detailDtoToEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNotNull(result);
        assertEquals("Test Trial", result.getName());
        assertEquals("Test Description", result.getDescription());
    }

    @Test
    @DisplayName("detailDtoToEntity() - Devrait retourner null pour DTO null")
    void testDetailDtoToEntity_NullDto() {
        // When
        Trial result = trialAdapter.detailDtoToEntity(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("entityToSummaryDto() - Devrait gérer Event avec ID null")
    void testEntityToSummaryDto_EventIdNull() {
        // Given - Trial IS Event, Trial.id cannot be null in DTO
        trial.setId(null);

        // When
        TrialSummaryDTO dto = trialAdapter.entityToSummaryDto(trial);

        // Then
        assertNotNull(dto);
        assertNull(dto.getId());  // Trial id is null
        assertNull(dto.getIdEvent());  // Trial's id IS Event's id
        assertEquals("Marathon de Paris", dto.getName());
    }

    @Test
    @DisplayName("detailDtoToEntity() - Devrait convertir TimeSlotDTO et PlaceDTO en entités dans Event")
    void testDetailDtoToEntity_WithNestedObjects() {
        // Given
        TrialDetailDTO dto = new TrialDetailDTO();
        dto.setId(5);
        dto.setName("Nested Trial");
        dto.setDescription("With nested objects");

        TimeSlotDTO timeSlotDTO = new TimeSlotDTO(
            LocalDateTime.of(2025, 8, 1, 10, 0),
            LocalDateTime.of(2025, 8, 1, 12, 30)
        );
        dto.setTimeSlot(timeSlotDTO);

        PlaceDTO placeDTO = new PlaceDTO();
        placeDTO.setId(7);
        placeDTO.setName("Complexe Sportif");
        placeDTO.setDescription("Gymnase couvert");
        placeDTO.setStreet("Rue des Sports");
        placeDTO.setNumber("15");
        placeDTO.setCity("Marseille");
        placeDTO.setZip("13000");
        placeDTO.setParking(false);
        placeDTO.setLatitude(43.2965);
        placeDTO.setLongitude(5.3698);
        dto.setPlace(placeDTO);

        // When
        Trial result = trialAdapter.detailDtoToEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getId());
        assertNotNull(result);
        assertEquals("Nested Trial", result.getName());
        assertEquals("With nested objects", result.getDescription());

        assertNotNull(result.getTimeSlot());
        assertEquals(timeSlotDTO.getStart(), result.getTimeSlot().getStart());
        assertEquals(timeSlotDTO.getEnd(), result.getTimeSlot().getEnd());

        assertNotNull(result.getPlace());
        assertEquals(placeDTO.getId(), result.getPlace().getId());
        assertEquals(placeDTO.getName(), result.getPlace().getName());
        assertEquals(placeDTO.getDescription(), result.getPlace().getDescription());
        assertEquals(placeDTO.getStreet(), result.getPlace().getStreet());
        assertEquals(placeDTO.getNumber(), result.getPlace().getNumber());
        assertEquals(placeDTO.getCity(), result.getPlace().getCity());
        assertEquals(placeDTO.getZip(), result.getPlace().getZip());
        assertEquals(placeDTO.getParking(), result.getPlace().getParking());
        assertEquals(placeDTO.getLatitude(), result.getPlace().getLatitude());
        assertEquals(placeDTO.getLongitude(), result.getPlace().getLongitude());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait construire les participants solo correctement")
    void testEntityToDetailDto_SoloParticipants() {
        // Given
        ApplicationUser user1 = new ApplicationUser();
        user1.setId(1);
        user1.setName("John");
        user1.setLastname("Doe");

        ApplicationUser user2 = new ApplicationUser();
        user2.setId(2);
        user2.setName("Jane");
        user2.setLastname("Smith");

        IsConvenedTo participation1 = new IsConvenedTo();
        participation1.setUser(user1);

        IsConvenedTo participation2 = new IsConvenedTo();
        participation2.setUser(user2);

        List<IsConvenedTo> soloParticipations = Arrays.asList(participation1, participation2);

        when(isConvenedToRepository.findByTrialId(trial.getId())).thenReturn(soloParticipations);

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertFalse(dto.isTeamEvent());
        assertEquals(2, dto.getSoloParticipants().size());
        assertEquals("John", dto.getSoloParticipants().get(0).getFirstName());
        assertEquals("Doe", dto.getSoloParticipants().get(0).getLastName());
        assertEquals("John Doe", dto.getSoloParticipants().get(0).getFullName());
        assertEquals("Jane", dto.getSoloParticipants().get(1).getFirstName());
        assertEquals("Smith", dto.getSoloParticipants().get(1).getLastName());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait ignorer les participants solo avec user null")
    void testEntityToDetailDto_SoloParticipants_WithNullUser() {
        // Given
        ApplicationUser user1 = new ApplicationUser();
        user1.setId(1);
        user1.setName("John");
        user1.setLastname("Doe");

        IsConvenedTo participation1 = new IsConvenedTo();
        participation1.setUser(user1);

        IsConvenedTo participation2 = new IsConvenedTo();
        participation2.setUser(null);  // Null user should be filtered out

        List<IsConvenedTo> soloParticipations = Arrays.asList(participation1, participation2);

        when(isConvenedToRepository.findByTrialId(trial.getId())).thenReturn(soloParticipations);

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(1, dto.getSoloParticipants().size());
        assertEquals("John", dto.getSoloParticipants().get(0).getFirstName());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait construire les participants équipe correctement")
    void testEntityToDetailDto_TeamParticipants() {
        // Given
        Team team1 = new Team();
        team1.setId(1);
        team1.setName("Team Alpha");

        Team team2 = new Team();
        team2.setId(2);
        team2.setName("Team Beta");

        ParticipateAt participation1 = new ParticipateAt();
        participation1.setTeam(team1);

        ParticipateAt participation2 = new ParticipateAt();
        participation2.setTeam(team2);

        List<ParticipateAt> teamParticipations = Arrays.asList(participation1, participation2);

        when(participateAtRepository.findByTrialId(trial.getId())).thenReturn(teamParticipations);

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertTrue(dto.isTeamEvent());
        assertEquals(2, dto.getTeamParticipants().size());
        assertEquals("Team Alpha", dto.getTeamParticipants().get(0).getName());
        assertEquals("Team Beta", dto.getTeamParticipants().get(1).getName());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait ignorer les participants équipe avec team null")
    void testEntityToDetailDto_TeamParticipants_WithNullTeam() {
        // Given
        Team team1 = new Team();
        team1.setId(1);
        team1.setName("Team Alpha");

        ParticipateAt participation1 = new ParticipateAt();
        participation1.setTeam(team1);

        ParticipateAt participation2 = new ParticipateAt();
        participation2.setTeam(null);  // Null team should be filtered out

        List<ParticipateAt> teamParticipations = Arrays.asList(participation1, participation2);

        when(participateAtRepository.findByTrialId(trial.getId())).thenReturn(teamParticipations);

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(1, dto.getTeamParticipants().size());
        assertEquals("Team Alpha", dto.getTeamParticipants().get(0).getName());
    }

    @Test
    @DisplayName("buildRankings() - Devrait construire les classements pour les équipes")
    void testBuildRankings_Teams() {
        // Given
        Team team1 = new Team();
        team1.setId(1);
        team1.setName("Team Alpha");

        Team team2 = new Team();
        team2.setId(2);
        team2.setName("Team Beta");

        ParticipateAt participation1 = new ParticipateAt();
        participation1.setTeam(team1);
        participation1.setResult(45.0);
        participation1.setIsForfeit(false);
        participation1.setIsValidated(true);

        ParticipateAt participation2 = new ParticipateAt();
        participation2.setTeam(team2);
        participation2.setResult(52.0);
        participation2.setIsForfeit(false);
        participation2.setIsValidated(true);

        when(participateAtRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "ASC"))
            .thenReturn(Arrays.asList(participation1, participation2));

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(2, dto.getRankings().size());
        assertEquals(1, dto.getRankings().get(0).getRank());
        assertEquals(45.0, dto.getRankings().get(0).getResult());
        assertEquals("Team Alpha", dto.getRankings().get(0).getParticipantName());
        assertEquals("TEAM", dto.getRankings().get(0).getParticipantType());
        
        assertEquals(2, dto.getRankings().get(1).getRank());
        assertEquals(52.0, dto.getRankings().get(1).getResult());
        assertEquals("Team Beta", dto.getRankings().get(1).getParticipantName());
    }

    @Test
    @DisplayName("buildRankings() - Devrait construire les classements pour les athlètes sans forfait")
    void testBuildRankings_Athletes() {
        // Given
        ApplicationUser user1 = new ApplicationUser();
        user1.setId(1);
        user1.setName("John");
        user1.setLastname("Doe");

        ApplicationUser user2 = new ApplicationUser();
        user2.setId(2);
        user2.setName("Jane");
        user2.setLastname("Smith");

        IsConvenedTo convening1 = new IsConvenedTo();
        convening1.setUser(user1);
        convening1.setResult(100.0);
        convening1.setIsForfeit(false);
        convening1.setIsValidated(true);

        IsConvenedTo convening2 = new IsConvenedTo();
        convening2.setUser(user2);
        convening2.setResult(95.0);
        convening2.setIsForfeit(false);
        convening2.setIsValidated(true);

        when(isConvenedToRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "ASC"))
            .thenReturn(Arrays.asList(convening1, convening2));

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(2, dto.getRankings().size());
        assertEquals(1, dto.getRankings().get(0).getRank());
        assertEquals(100.0, dto.getRankings().get(0).getResult());
        assertEquals("John Doe", dto.getRankings().get(0).getParticipantName());
        assertEquals("ATHLETE", dto.getRankings().get(0).getParticipantType());
        
        assertEquals(2, dto.getRankings().get(1).getRank());
        assertEquals(95.0, dto.getRankings().get(1).getResult());
        assertEquals("Jane Smith", dto.getRankings().get(1).getParticipantName());
    }

    @Test
    @DisplayName("buildRankings() - Devrait construire les classements pour les athlètes avec forfait")
    void testBuildRankings_AthletesForfeit() {
        // Given
        ApplicationUser user1 = new ApplicationUser();
        user1.setId(1);
        user1.setName("John");
        user1.setLastname("Doe");

        ApplicationUser user2 = new ApplicationUser();
        user2.setId(2);
        user2.setName("Jane");
        user2.setLastname("Smith");

        IsConvenedTo convening1 = new IsConvenedTo();
        convening1.setUser(user1);
        convening1.setResult(null);
        convening1.setIsForfeit(true);

        IsConvenedTo convening2 = new IsConvenedTo();
        convening2.setUser(user2);
        convening2.setResult(100.0);
        convening2.setIsForfeit(false);
        convening2.setIsValidated(true);

        when(isConvenedToRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "ASC"))
                .thenReturn(Arrays.asList(convening1, convening2));

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(2, dto.getRankings().size());

        assertEquals(null, dto.getRankings().get(0).getRank());
        assertEquals(null, dto.getRankings().get(0).getResult());
        assertEquals(true, dto.getRankings().get(0).getIsForfeit());
        assertEquals("John Doe", dto.getRankings().get(0).getParticipantName());
        assertEquals("ATHLETE", dto.getRankings().get(0).getParticipantType());

        assertEquals(1, dto.getRankings().get(1).getRank());
        assertEquals(100.0, dto.getRankings().get(1).getResult());
        assertEquals(false, dto.getRankings().get(1).getIsForfeit());
        assertEquals("Jane Smith", dto.getRankings().get(1).getParticipantName());
        assertEquals("ATHLETE", dto.getRankings().get(1).getParticipantType());
    }

    @Test
    @DisplayName("buildRankings() - Devrait ignorer les résultats avec team/user null")
    void testBuildRankings_WithNullParticipants() {
        // Given
        Team team1 = new Team();
        team1.setId(1);
        team1.setName("Team Alpha");

        ParticipateAt participation1 = new ParticipateAt();
        participation1.setTeam(team1);
        participation1.setResult(45.0);
        participation1.setIsForfeit(false);
        participation1.setIsValidated(true);

        ParticipateAt participation2 = new ParticipateAt();
        participation2.setTeam(null);
        participation2.setResult(52.0);
        participation2.setIsForfeit(false);
        participation2.setIsValidated(true);

        when(participateAtRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "ASC"))
            .thenReturn(Arrays.asList(participation1, participation2));

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(1, dto.getRankings().size());
        assertEquals("Team Alpha", dto.getRankings().get(0).getParticipantName());
    }

    @Test
    @DisplayName("buildRankings() - Aucun classement public si un participant non-forfait n'a pas encore de résultat validé")
    void testBuildRankings_WithNullResults() {
        // Given : un participant a un résultat validé, l'autre n'a pas encore de résultat
        // → aucun classement ne doit être retourné (affichage différé jusqu'à validation complète)
        Team team1 = new Team();
        team1.setId(1);
        team1.setName("Team Alpha");

        ParticipateAt participation1 = new ParticipateAt();
        participation1.setTeam(team1);
        participation1.setResult(45.0);
        participation1.setIsForfeit(false);
        participation1.setIsValidated(true);

        ParticipateAt participation2 = new ParticipateAt();
        participation2.setTeam(team1);
        participation2.setResult(null);  // Résultat manquant → pas encore validé
        participation2.setIsForfeit(false);
        participation2.setIsValidated(false);

        when(participateAtRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "ASC"))
            .thenReturn(Arrays.asList(participation1, participation2));

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then : le classement reste vide tant que tous les résultats ne sont pas validés
        assertNotNull(dto);
        assertEquals(0, dto.getRankings().size());
    }

    @Test
    @DisplayName("buildRankings() - Gérer le forfait")
    void testBuildRankings_WithForfeit() {
        // Given
        Team team1 = new Team();
        team1.setId(1);
        team1.setName("Team Alpha");

        ParticipateAt participation1 = new ParticipateAt();
        participation1.setTeam(team1);
        participation1.setResult(null);
        participation1.setIsForfeit(true);

        ParticipateAt participation2 = new ParticipateAt();
        participation2.setTeam(team1);
        participation2.setResult(45.0);
        participation2.setIsForfeit(false);
        participation2.setIsValidated(true);

        when(participateAtRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "ASC"))
                .thenReturn(Arrays.asList(participation1, participation2));

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(2, dto.getRankings().size());
        assertNull(dto.getRankings().get(0).getResult());
        assertEquals(45.0, dto.getRankings().get(1).getResult());
        assertEquals(true, dto.getRankings().get(0).getIsForfeit());
        assertEquals(false, dto.getRankings().get(1).getIsForfeit());
        assertNull(dto.getRankings().get(0).getRank());
        assertEquals(1, dto.getRankings().get(1).getRank());
    }
}

