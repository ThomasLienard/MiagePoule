package com.miage.pouleAPI.adapter;

import com.miage.pouleAPI.adapters.TrialAdapter;
import com.miage.pouleAPI.dtos.trial.TrialDetailDTO;
import com.miage.pouleAPI.entity.*;
import com.miage.pouleAPI.repositories.IsConvenedToRepository;
import com.miage.pouleAPI.repositories.ParticipateAtRepository;
import com.miage.pouleAPI.strategy.PointRankingStrategy;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests d'intégration pour valider le pattern Strategy
 * avec différents types de scores (TIME et POINTS).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TrialAdapter - Strategy Pattern Integration Tests")
class TrialAdapterStrategyIntegrationTest {

    @Mock
    private ParticipateAtRepository participateAtRepository;
    
    @Mock
    private IsConvenedToRepository isConvenedToRepository;

    private TrialAdapter trialAdapter;
    private RankingStrategyFactory rankingStrategyFactory;

    @BeforeEach
    void setUp() {
        // Créer une vraie factory avec les vraies stratégies
        rankingStrategyFactory = new RankingStrategyFactory(
            Arrays.asList(new TimeRankingStrategy(), new PointRankingStrategy())
        );
        
        trialAdapter = new TrialAdapter(participateAtRepository, isConvenedToRepository, rankingStrategyFactory);
    }

    @Test
    @DisplayName("Devrait classer correctement les résultats TIME (ordre croissant)")
    void testRankingWithTimeScore() {
        // Given - Épreuve de type TIME (sprint)
        Trial trial = createTrialWithScoreType("TIME");
        
        ApplicationUser user1 = createUser(1, "John", "Doe");
        ApplicationUser user2 = createUser(2, "Jane", "Smith");
        ApplicationUser user3 = createUser(3, "Bob", "Martin");

        IsConvenedTo convening1 = createConvening(user1, "12.5", false, true);
        IsConvenedTo convening2 = createConvening(user2, "10.8", false, true);
        IsConvenedTo convening3 = createConvening(user3, "11.2", false, true);

        // Ordre attendu en base pour TIME : ASC (croissant)
        when(isConvenedToRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "ASC"))
            .thenReturn(Arrays.asList(convening2, convening3, convening1));
        when(participateAtRepository.findByTrialIdOrderedByResultDynamic(anyInt(), anyString())).thenReturn(Arrays.asList());
        when(isConvenedToRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());
        when(participateAtRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(3, dto.getRankings().size());
        
        // Jane Smith (10.8s) devrait être première
        assertEquals(1, dto.getRankings().get(0).getRank());
        assertEquals("10.8", dto.getRankings().get(0).getResult());
        assertEquals("Jane Smith", dto.getRankings().get(0).getParticipantName());
        
        // Bob Martin (11.2s) devrait être deuxième
        assertEquals(2, dto.getRankings().get(1).getRank());
        assertEquals("11.2", dto.getRankings().get(1).getResult());
        assertEquals("Bob Martin", dto.getRankings().get(1).getParticipantName());
        
        // John Doe (12.5s) devrait être troisième
        assertEquals(3, dto.getRankings().get(2).getRank());
        assertEquals("12.5", dto.getRankings().get(2).getResult());
        assertEquals("John Doe", dto.getRankings().get(2).getParticipantName());
    }

    @Test
    @DisplayName("Devrait classer correctement les résultats POINTS (ordre décroissant)")
    void testRankingWithPointsScore() {
        // Given - Épreuve de type POINTS (gymnastique)
        Trial trial = createTrialWithScoreType("POINTS");
        
        ApplicationUser user1 = createUser(1, "Alice", "Johnson");
        ApplicationUser user2 = createUser(2, "Charlie", "Brown");
        ApplicationUser user3 = createUser(3, "Diana", "Prince");

        IsConvenedTo convening1 = createConvening(user1, "85.5", false, true);
        IsConvenedTo convening2 = createConvening(user2, "92.3", false, true);
        IsConvenedTo convening3 = createConvening(user3, "88.7", false, true);

        // Ordre attendu en base pour POINTS : DESC (décroissant)
        when(isConvenedToRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "DESC"))
            .thenReturn(Arrays.asList(convening2, convening3, convening1));
        when(participateAtRepository.findByTrialIdOrderedByResultDynamic(anyInt(), anyString())).thenReturn(Arrays.asList());
        when(isConvenedToRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());
        when(participateAtRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(3, dto.getRankings().size());
        
        // Charlie Brown (92.3 pts) devrait être premier
        assertEquals(1, dto.getRankings().get(0).getRank());
        assertEquals("92.3", dto.getRankings().get(0).getResult());
        assertEquals("Charlie Brown", dto.getRankings().get(0).getParticipantName());
        
        // Diana Prince (88.7 pts) devrait être deuxième
        assertEquals(2, dto.getRankings().get(1).getRank());
        assertEquals("88.7", dto.getRankings().get(1).getResult());
        assertEquals("Diana Prince", dto.getRankings().get(1).getParticipantName());
        
        // Alice Johnson (85.5 pts) devrait être troisième
        assertEquals(3, dto.getRankings().get(2).getRank());
        assertEquals("85.5", dto.getRankings().get(2).getResult());
        assertEquals("Alice Johnson", dto.getRankings().get(2).getParticipantName());
    }

    @Test
    @DisplayName("Devrait gérer le même score avec TIME et POINTS différemment")
    void testSameScoreDifferentTypes() {
        // Given - Deux épreuves avec les mêmes valeurs numériques mais types différents
        Trial timeTrial = createTrialWithScoreType("TIME");
        Trial pointsTrial = createTrialWithScoreType("POINTS");
        
        ApplicationUser user1 = createUser(1, "User", "One");
        ApplicationUser user2 = createUser(2, "User", "Two");

        IsConvenedTo timeConvening1 = createConvening(user1, "90", false, true);
        IsConvenedTo timeConvening2 = createConvening(user2, "100", false, true);
        
        IsConvenedTo pointsConvening1 = createConvening(user1, "90", false, true);
        IsConvenedTo pointsConvening2 = createConvening(user2, "100", false, true);

        // Pour TIME : 90 < 100, donc 90 est meilleur (rang 1)
        when(isConvenedToRepository.findByTrialIdOrderedByResultDynamic(timeTrial.getId(), "ASC"))
            .thenReturn(Arrays.asList(timeConvening1, timeConvening2));
        
        // Pour POINTS : 100 > 90, donc 100 est meilleur (rang 1)
        when(isConvenedToRepository.findByTrialIdOrderedByResultDynamic(pointsTrial.getId(), "DESC"))
            .thenReturn(Arrays.asList(pointsConvening2, pointsConvening1));
        
        when(participateAtRepository.findByTrialIdOrderedByResultDynamic(anyInt(), anyString())).thenReturn(Arrays.asList());
        when(isConvenedToRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());
        when(participateAtRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());

        // When
        TrialDetailDTO timeDto = trialAdapter.entityToDetailDto(timeTrial);
        TrialDetailDTO pointsDto = trialAdapter.entityToDetailDto(pointsTrial);

        // Then - Pour TIME, 90 doit être premier
        assertEquals(1, timeDto.getRankings().get(0).getRank());
        assertEquals("90", timeDto.getRankings().get(0).getResult());
        assertEquals(2, timeDto.getRankings().get(1).getRank());
        assertEquals("100", timeDto.getRankings().get(1).getResult());
        
        // Then - Pour POINTS, 100 doit être premier
        assertEquals(1, pointsDto.getRankings().get(0).getRank());
        assertEquals("100", pointsDto.getRankings().get(0).getResult());
        assertEquals(2, pointsDto.getRankings().get(1).getRank());
        assertEquals("90", pointsDto.getRankings().get(1).getResult());
    }

    // ============ Méthodes utilitaires ============

    private Trial createTrialWithScoreType(String scoreType) {
        TypeScore typeScore = new TypeScore();
        typeScore.setName(scoreType);

        Trial trial = new Trial();
        trial.setId(1);
        trial.setName("Test Trial");
        trial.setDescription("Test Description");
        trial.setTypeScore(typeScore);
        
        Competition competition = new Competition();
        competition.setId(1);
        competition.setName("Test Competition");
        trial.setCompetition(competition);
        
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStart(LocalDateTime.now());
        timeSlot.setEnd(LocalDateTime.now().plusHours(2));
        trial.setTimeSlot(timeSlot);
        
        Place place = new Place();
        place.setId(1);
        place.setName("Test Place");
        trial.setPlace(place);
        
        return trial;
    }

    private ApplicationUser createUser(Integer id, String name, String lastname) {
        ApplicationUser user = new ApplicationUser();
        user.setId(id);
        user.setName(name);
        user.setLastname(lastname);
        return user;
    }

    private IsConvenedTo createConvening(ApplicationUser user, String result, Boolean isForfeit, Boolean isValidated) {
        IsConvenedTo convening = new IsConvenedTo();
        convening.setUser(user);
        convening.setResult(result);
        convening.setIsForfeit(isForfeit);
        convening.setIsValidated(isValidated);
        return convening;
    }
}
