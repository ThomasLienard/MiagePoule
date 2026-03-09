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

        IsConvenedTo convening1 = createConvening(user1, 12.5, false, true);
        IsConvenedTo convening2 = createConvening(user2, 10.8, false, true);
        IsConvenedTo convening3 = createConvening(user3, 11.2, false, true);

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
        assertEquals(10.8, dto.getRankings().get(0).getResult());
        assertEquals("Jane Smith", dto.getRankings().get(0).getParticipantName());
        
        // Bob Martin (11.2s) devrait être deuxième
        assertEquals(2, dto.getRankings().get(1).getRank());
        assertEquals(11.2, dto.getRankings().get(1).getResult());
        assertEquals("Bob Martin", dto.getRankings().get(1).getParticipantName());
        
        // John Doe (12.5s) devrait être troisième
        assertEquals(3, dto.getRankings().get(2).getRank());
        assertEquals(12.5, dto.getRankings().get(2).getResult());
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

        IsConvenedTo convening1 = createConvening(user1, 85.5, false, true);
        IsConvenedTo convening2 = createConvening(user2, 92.3, false, true);
        IsConvenedTo convening3 = createConvening(user3, 88.7, false, true);

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
        assertEquals(92.3, dto.getRankings().get(0).getResult());
        assertEquals("Charlie Brown", dto.getRankings().get(0).getParticipantName());
        
        // Diana Prince (88.7 pts) devrait être deuxième
        assertEquals(2, dto.getRankings().get(1).getRank());
        assertEquals(88.7, dto.getRankings().get(1).getResult());
        assertEquals("Diana Prince", dto.getRankings().get(1).getParticipantName());
        
        // Alice Johnson (85.5 pts) devrait être troisième
        assertEquals(3, dto.getRankings().get(2).getRank());
        assertEquals(85.5, dto.getRankings().get(2).getResult());
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

        IsConvenedTo timeConvening1 = createConvening(user1, 90.0, false, true);
        IsConvenedTo timeConvening2 = createConvening(user2, 100.0, false, true);
        
        IsConvenedTo pointsConvening1 = createConvening(user1, 90.0, false, true);
        IsConvenedTo pointsConvening2 = createConvening(user2, 100.0, false, true);

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
        assertEquals(90.0, timeDto.getRankings().get(0).getResult());
        assertEquals(2, timeDto.getRankings().get(1).getRank());
        assertEquals(100.0, timeDto.getRankings().get(1).getResult());
        
        // Then - Pour POINTS, 100 doit être premier
        assertEquals(1, pointsDto.getRankings().get(0).getRank());
        assertEquals(100.0, pointsDto.getRankings().get(0).getResult());
        assertEquals(2, pointsDto.getRankings().get(1).getRank());
        assertEquals(90.0, pointsDto.getRankings().get(1).getResult());
    }

    @Test
    @DisplayName("Devrait retourner un classement vide si les résultats ne sont pas tous validés")
    void testRankingEmptyWhenResultsNotAllValidated() {
        // Given - Épreuve avec un athlète non encore validé
        Trial trial = createTrialWithScoreType("TIME");

        ApplicationUser user1 = createUser(1, "Alice", "Dupont");
        ApplicationUser user2 = createUser(2, "Bob", "Martin");

        // user1 validé, user2 non validé
        IsConvenedTo convening1 = createConvening(user1, 11.0, false, true);
        IsConvenedTo convening2 = createConvening(user2, 12.0, false, false);

        when(isConvenedToRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "ASC"))
            .thenReturn(Arrays.asList(convening1, convening2));
        when(participateAtRepository.findByTrialIdOrderedByResultDynamic(anyInt(), anyString())).thenReturn(Arrays.asList());
        when(isConvenedToRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());
        when(participateAtRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then - aucun ranking car pas tous validés
        assertNotNull(dto);
        assertTrue(dto.getRankings().isEmpty(),
            "Le classement doit être vide si tous les résultats ne sont pas validés");
    }

    @Test
    @DisplayName("Devrait exclure les athlètes forfait du rang et les placer sans rang")
    void testRankingForfeitAthleteHasNoRank() {
        // Given - Épreuve avec un athlète forfait
        Trial trial = createTrialWithScoreType("TIME");

        ApplicationUser user1 = createUser(1, "Clara", "Petit");
        ApplicationUser user2 = createUser(2, "Damien", "Gros");
        ApplicationUser user3 = createUser(3, "Eva", "Long");

        // user3 est forfait
        IsConvenedTo convening1 = createConvening(user1, 10.0, false, true);
        IsConvenedTo convening2 = createConvening(user2, 11.5, false, true);
        IsConvenedTo convening3 = createConvening(user3, null, true, false);

        when(isConvenedToRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "ASC"))
            .thenReturn(Arrays.asList(convening1, convening2, convening3));
        when(participateAtRepository.findByTrialIdOrderedByResultDynamic(anyInt(), anyString())).thenReturn(Arrays.asList());
        when(isConvenedToRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());
        when(participateAtRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(3, dto.getRankings().size());

        // Clara en rang 1
        assertEquals(1, dto.getRankings().get(0).getRank());
        assertEquals("Clara Petit", dto.getRankings().get(0).getParticipantName());
        assertFalse(dto.getRankings().get(0).getIsForfeit());

        // Damien en rang 2
        assertEquals(2, dto.getRankings().get(1).getRank());
        assertEquals("Damien Gros", dto.getRankings().get(1).getParticipantName());
        assertFalse(dto.getRankings().get(1).getIsForfeit());

        // Eva est forfait, sans rang
        assertNull(dto.getRankings().get(2).getRank());
        assertEquals("Eva Long", dto.getRankings().get(2).getParticipantName());
        assertTrue(dto.getRankings().get(2).getIsForfeit());
    }

    @Test
    @DisplayName("Devrait classer correctement les équipes avec la stratégie TIME (ordre croissant)")
    void testTeamRankingWithTimeScore() {
        // Given - Épreuve par équipes de type TIME
        Trial trial = createTrialWithScoreType("TIME");

        Team team1 = createTeam(1, "Team Alpha");
        Team team2 = createTeam(2, "Team Beta");
        Team team3 = createTeam(3, "Team Gamma");

        ParticipateAt pa1 = createTeamParticipation(team1, 55.0, false, true);
        ParticipateAt pa2 = createTeamParticipation(team2, 52.3, false, true);
        ParticipateAt pa3 = createTeamParticipation(team3, 58.7, false, true);

        // Pour TIME : tri ASC (52.3, 55.0, 58.7)
        when(participateAtRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "ASC"))
            .thenReturn(Arrays.asList(pa2, pa1, pa3));
        when(isConvenedToRepository.findByTrialIdOrderedByResultDynamic(anyInt(), anyString())).thenReturn(Arrays.asList());
        when(participateAtRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList(pa1, pa2, pa3));
        when(isConvenedToRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then - Team Beta (52.3s) est première
        assertNotNull(dto);
        assertEquals(3, dto.getRankings().size());

        assertEquals(1, dto.getRankings().get(0).getRank());
        assertEquals(52.3, dto.getRankings().get(0).getResult());
        assertEquals("Team Beta", dto.getRankings().get(0).getParticipantName());
        assertEquals("TEAM", dto.getRankings().get(0).getParticipantType());

        assertEquals(2, dto.getRankings().get(1).getRank());
        assertEquals(55.0, dto.getRankings().get(1).getResult());
        assertEquals("Team Alpha", dto.getRankings().get(1).getParticipantName());

        assertEquals(3, dto.getRankings().get(2).getRank());
        assertEquals(58.7, dto.getRankings().get(2).getResult());
        assertEquals("Team Gamma", dto.getRankings().get(2).getParticipantName());
    }

    @Test
    @DisplayName("Devrait classer correctement les équipes avec la stratégie POINTS (ordre décroissant)")
    void testTeamRankingWithPointsScore() {
        // Given - Épreuve par équipes de type POINTS
        Trial trial = createTrialWithScoreType("POINTS");

        Team team1 = createTeam(1, "Team Rouge");
        Team team2 = createTeam(2, "Team Bleu");
        Team team3 = createTeam(3, "Team Vert");

        ParticipateAt pa1 = createTeamParticipation(team1, 78.0, false, true);
        ParticipateAt pa2 = createTeamParticipation(team2, 91.5, false, true);
        ParticipateAt pa3 = createTeamParticipation(team3, 84.2, false, true);

        // Pour POINTS : tri DESC (91.5, 84.2, 78.0)
        when(participateAtRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "DESC"))
            .thenReturn(Arrays.asList(pa2, pa3, pa1));
        when(isConvenedToRepository.findByTrialIdOrderedByResultDynamic(anyInt(), anyString())).thenReturn(Arrays.asList());
        when(participateAtRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList(pa1, pa2, pa3));
        when(isConvenedToRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then - Team Bleu (91.5 pts) est première
        assertNotNull(dto);
        assertEquals(3, dto.getRankings().size());

        assertEquals(1, dto.getRankings().get(0).getRank());
        assertEquals(91.5, dto.getRankings().get(0).getResult());
        assertEquals("Team Bleu", dto.getRankings().get(0).getParticipantName());
        assertEquals("TEAM", dto.getRankings().get(0).getParticipantType());

        assertEquals(2, dto.getRankings().get(1).getRank());
        assertEquals(84.2, dto.getRankings().get(1).getResult());
        assertEquals("Team Vert", dto.getRankings().get(1).getParticipantName());

        assertEquals(3, dto.getRankings().get(2).getRank());
        assertEquals(78.0, dto.getRankings().get(2).getResult());
        assertEquals("Team Rouge", dto.getRankings().get(2).getParticipantName());
    }

    @Test
    @DisplayName("Devrait retourner un classement vide pour une équipe si les résultats ne sont pas tous validés")
    void testTeamRankingEmptyWhenNotAllValidated() {
        // Given - une équipe non validée
        Trial trial = createTrialWithScoreType("POINTS");

        Team team1 = createTeam(1, "Team A");
        Team team2 = createTeam(2, "Team B");

        ParticipateAt pa1 = createTeamParticipation(team1, 88.0, false, true);
        ParticipateAt pa2 = createTeamParticipation(team2, 75.0, false, false); // non validé

        when(participateAtRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "DESC"))
            .thenReturn(Arrays.asList(pa1, pa2));
        when(isConvenedToRepository.findByTrialIdOrderedByResultDynamic(anyInt(), anyString())).thenReturn(Arrays.asList());
        when(participateAtRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList(pa1, pa2));
        when(isConvenedToRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertTrue(dto.getRankings().isEmpty(),
            "Le classement doit être vide si toutes les équipes ne sont pas validées");
    }

    @Test
    @DisplayName("Devrait utiliser TIME par défaut quand le TypeScore est null")
    void testDefaultsToTimeWhenScoreTypeIsNull() {
        // Given - Épreuve sans TypeScore défini
        Trial trial = createTrialWithScoreType("TIME");
        trial.setTypeScore(null);

        ApplicationUser user1 = createUser(1, "Leo", "Petit");
        ApplicationUser user2 = createUser(2, "Marie", "Grand");

        IsConvenedTo convening1 = createConvening(user1, 9.5, false, true);
        IsConvenedTo convening2 = createConvening(user2, 10.2, false, true);

        // Quand typeScore est null, la stratégie utilisée devrait être "TIME" (valeur par défaut)
        when(isConvenedToRepository.findByTrialIdOrderedByResultDynamic(trial.getId(), "ASC"))
            .thenReturn(Arrays.asList(convening1, convening2));
        when(participateAtRepository.findByTrialIdOrderedByResultDynamic(anyInt(), anyString())).thenReturn(Arrays.asList());
        when(isConvenedToRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());
        when(participateAtRepository.findByTrialId(anyInt())).thenReturn(Arrays.asList());

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then - Leo (9.5) doit être premier (ordre croissant = TIME)
        assertNotNull(dto);
        assertEquals(2, dto.getRankings().size());
        assertEquals(1, dto.getRankings().get(0).getRank());
        assertEquals(9.5, dto.getRankings().get(0).getResult());
        assertEquals("Leo Petit", dto.getRankings().get(0).getParticipantName());
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

    private IsConvenedTo createConvening(ApplicationUser user, Double result, Boolean isForfeit, Boolean isValidated) {
        IsConvenedTo convening = new IsConvenedTo();
        convening.setUser(user);
        convening.setResult(result);
        convening.setIsForfeit(isForfeit);
        convening.setIsValidated(isValidated);
        return convening;
    }

    private Team createTeam(Integer id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        return team;
    }

    private ParticipateAt createTeamParticipation(Team team, Double result, Boolean isForfeit, Boolean isValidated) {
        ParticipateAt pa = new ParticipateAt();
        pa.setTeam(team);
        pa.setResult(result);
        pa.setIsForfeit(isForfeit);
        pa.setIsValidated(isValidated);
        return pa;
    }
}
