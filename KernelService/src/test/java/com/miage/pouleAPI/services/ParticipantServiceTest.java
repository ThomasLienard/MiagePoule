package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.participant.AthleteDTO;
import com.miage.pouleAPI.dtos.participant.ParticipantDTO;
import com.miage.pouleAPI.dtos.participant.TrialParticipantsDTO;
import com.miage.pouleAPI.entity.*;
import com.miage.pouleAPI.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires ParticipantService - Gestion des participants aux épreuves")
class ParticipantServiceTest {

    @Mock
    private TrialRepository trialRepository;

    @Mock
    private ApplicationUserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ParticipateAtRepository participateAtRepository;

    @Mock
    private IsConvenedToRepository isConvenedToRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ParticipantServiceImpl participantService;

    private Trial testTrial;
    private ApplicationUser testAthlete;
    private ApplicationUser testCommissaire;
    private Team testTeam;
    private Country france;
    private Role athleteRole;
    private Role commissaireRole;

    @BeforeEach
    void setUp() {
        france = new Country();
        france.setCode("FR");

        athleteRole = new Role();
        athleteRole.setRoleName("ATHLETE");

        commissaireRole = new Role();
        commissaireRole.setRoleName("COMMISSAIRE");

        testTrial = new Trial();
        testTrial.setId(1);
        testTrial.setName("100m Sprint");

        testAthlete = new ApplicationUser();
        testAthlete.setId(10);
        testAthlete.setName("John");
        testAthlete.setLastname("Doe");
        testAthlete.setEmail("john.doe@test.com");
        testAthlete.setRole(athleteRole);
        testAthlete.setCountry(france);

        testCommissaire = new ApplicationUser();
        testCommissaire.setId(5);
        testCommissaire.setName("Commissaire");
        testCommissaire.setLastname("Test");
        testCommissaire.setEmail("commissaire@test.com");
        testCommissaire.setRole(commissaireRole);

        testTeam = new Team();
        testTeam.setId(1);
        testTeam.setName("Team France");
        testTeam.setCountry(france);
    }

    // ===== TESTS INSCRIPTION ATHLÈTE =====

    @Nested
    @DisplayName("Tests addAthleteToTrial")
    class AddAthleteToTrialTests {

        @Test
        @DisplayName("Devrait inscrire un athlète avec succès")
        void addAthleteToTrial_shouldSucceed() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(testTrial));
            when(userRepository.findById(10)).thenReturn(Optional.of(testAthlete));
            when(participateAtRepository.hasTeamParticipation(1)).thenReturn(false);
            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.empty());
            when(isConvenedToRepository.save(any(IsConvenedTo.class))).thenAnswer(i -> i.getArgument(0));

            ParticipantDTO result = participantService.addAthleteToTrial(1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10);
            assertThat(result.getName()).isEqualTo("John Doe");
            assertThat(result.getType()).isEqualTo("ATHLETE");
            assertThat(result.getCountry()).isEqualTo("FR");
            assertThat(result.isForfeit()).isFalse();

            verify(isConvenedToRepository).save(any(IsConvenedTo.class));
        }

        @Test
        @DisplayName("Devrait échouer si l'épreuve n'existe pas")
        void addAthleteToTrial_shouldFailWhenTrialNotFound() {
            when(trialRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> participantService.addAthleteToTrial(999, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Épreuve non trouvée");
        }

        @Test
        @DisplayName("Devrait échouer si l'athlète n'existe pas")
        void addAthleteToTrial_shouldFailWhenAthleteNotFound() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(testTrial));
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> participantService.addAthleteToTrial(1, 999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Athlète non trouvé");
        }

        @Test
        @DisplayName("Devrait échouer si l'utilisateur n'est pas un athlète")
        void addAthleteToTrial_shouldFailWhenUserIsNotAthlete() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(testTrial));
            when(userRepository.findById(5)).thenReturn(Optional.of(testCommissaire));

            assertThatThrownBy(() -> participantService.addAthleteToTrial(1, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("L'utilisateur n'est pas un athlète");
        }

        @Test
        @DisplayName("Devrait échouer si l'épreuve a déjà des équipes inscrites")
        void addAthleteToTrial_shouldFailWhenTrialHasTeams() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(testTrial));
            when(userRepository.findById(10)).thenReturn(Optional.of(testAthlete));
            when(participateAtRepository.hasTeamParticipation(1)).thenReturn(true);

            assertThatThrownBy(() -> participantService.addAthleteToTrial(1, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cette épreuve est réservée aux équipes");
        }

        @Test
        @DisplayName("Devrait échouer si l'athlète est déjà inscrit")
        void addAthleteToTrial_shouldFailWhenAthleteAlreadyRegistered() {
            IsConvenedTo existingInscription = new IsConvenedTo();
            
            when(trialRepository.findById(1)).thenReturn(Optional.of(testTrial));
            when(userRepository.findById(10)).thenReturn(Optional.of(testAthlete));
            when(participateAtRepository.hasTeamParticipation(1)).thenReturn(false);
            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.of(existingInscription));

            assertThatThrownBy(() -> participantService.addAthleteToTrial(1, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("L'athlète est déjà inscrit à cette épreuve");
        }
    }

    // ===== TESTS INSCRIPTION ÉQUIPE =====

    @Nested
    @DisplayName("Tests addTeamToTrial")
    class AddTeamToTrialTests {

        @Test
        @DisplayName("Devrait inscrire une équipe avec succès")
        void addTeamToTrial_shouldSucceed() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(testTrial));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(isConvenedToRepository.hasAthleteParticipation(1)).thenReturn(false);
            when(participateAtRepository.findByTrialIdAndTeamId(1, 1)).thenReturn(Optional.empty());
            when(participateAtRepository.save(any(ParticipateAt.class))).thenAnswer(i -> i.getArgument(0));

            ParticipantDTO result = participantService.addTeamToTrial(1, 1);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Team France");
            assertThat(result.getType()).isEqualTo("TEAM");
            assertThat(result.getCountry()).isEqualTo("FR");
            assertThat(result.isForfeit()).isFalse();

            verify(participateAtRepository).save(any(ParticipateAt.class));
        }

        @Test
        @DisplayName("Devrait échouer si l'équipe n'existe pas")
        void addTeamToTrial_shouldFailWhenTeamNotFound() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(testTrial));
            when(teamRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> participantService.addTeamToTrial(1, 999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Équipe non trouvée");
        }

        @Test
        @DisplayName("Devrait échouer si l'épreuve a déjà des athlètes inscrits")
        void addTeamToTrial_shouldFailWhenTrialHasAthletes() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(testTrial));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(isConvenedToRepository.hasAthleteParticipation(1)).thenReturn(true);

            assertThatThrownBy(() -> participantService.addTeamToTrial(1, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cette épreuve est réservée aux athlètes individuels");
        }

        @Test
        @DisplayName("Devrait échouer si l'équipe est déjà inscrite")
        void addTeamToTrial_shouldFailWhenTeamAlreadyRegistered() {
            ParticipateAt existingInscription = new ParticipateAt();
            
            when(trialRepository.findById(1)).thenReturn(Optional.of(testTrial));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(isConvenedToRepository.hasAthleteParticipation(1)).thenReturn(false);
            when(participateAtRepository.findByTrialIdAndTeamId(1, 1)).thenReturn(Optional.of(existingInscription));

            assertThatThrownBy(() -> participantService.addTeamToTrial(1, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("L'équipe est déjà inscrite à cette épreuve");
        }
    }

    // ===== TESTS FORFAIT ATHLÈTE =====

    @Nested
    @DisplayName("Tests forfeitAthlete")
    class ForfeitAthleteTests {

        @Test
        @DisplayName("Devrait déclarer un athlète forfait avec succès")
        void forfeitAthlete_shouldSucceed() {
            IsConvenedTo inscription = new IsConvenedTo();
            inscription.setUser(testAthlete);
            inscription.setTrial(testTrial);
            inscription.setIsForfeit(false);

            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.of(inscription));
            when(isConvenedToRepository.save(any(IsConvenedTo.class))).thenAnswer(i -> i.getArgument(0));

            ParticipantDTO result = participantService.forfeitAthlete(1, 10);

            assertThat(result).isNotNull();
            assertThat(result.isForfeit()).isTrue();
            verify(isConvenedToRepository).save(inscription);
            assertThat(inscription.getIsForfeit()).isTrue();
        }

        @Test
        @DisplayName("Devrait échouer si l'athlète n'est pas inscrit")
        void forfeitAthlete_shouldFailWhenNotRegistered() {
            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> participantService.forfeitAthlete(1, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("L'athlète n'est pas inscrit à cette épreuve");
        }
    }

    // ===== TESTS FORFAIT ÉQUIPE =====

    @Nested
    @DisplayName("Tests forfeitTeam")
    class ForfeitTeamTests {

        @Test
        @DisplayName("Devrait déclarer une équipe forfait avec succès")
        void forfeitTeam_shouldSucceed() {
            ParticipateAt inscription = new ParticipateAt();
            inscription.setTeam(testTeam);
            inscription.setTrial(testTrial);
            inscription.setIsForfeit(false);

            when(participateAtRepository.findByTrialIdAndTeamId(1, 1)).thenReturn(Optional.of(inscription));
            when(participateAtRepository.save(any(ParticipateAt.class))).thenAnswer(i -> i.getArgument(0));

            ParticipantDTO result = participantService.forfeitTeam(1, 1);

            assertThat(result).isNotNull();
            assertThat(result.isForfeit()).isTrue();
            verify(participateAtRepository).save(inscription);
            assertThat(inscription.getIsForfeit()).isTrue();
        }

        @Test
        @DisplayName("Devrait échouer si l'équipe n'est pas inscrite")
        void forfeitTeam_shouldFailWhenNotRegistered() {
            when(participateAtRepository.findByTrialIdAndTeamId(1, 1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> participantService.forfeitTeam(1, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("L'équipe n'est pas inscrite à cette épreuve");
        }
    }

    // ===== TESTS ANNULATION FORFAIT ATHLÈTE =====

    @Nested
    @DisplayName("Tests unforfeitAthlete")
    class UnforfeitAthleteTests {

        @Test
        @DisplayName("Devrait annuler le forfait d'un athlète avec succès")
        void unforfeitAthlete_shouldSucceed() {
            IsConvenedTo inscription = new IsConvenedTo();
            inscription.setUser(testAthlete);
            inscription.setTrial(testTrial);
            inscription.setIsForfeit(true);

            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.of(inscription));
            when(isConvenedToRepository.save(any(IsConvenedTo.class))).thenAnswer(i -> i.getArgument(0));

            ParticipantDTO result = participantService.unforfeitAthlete(1, 10);

            assertThat(result).isNotNull();
            assertThat(result.isForfeit()).isFalse();
            verify(isConvenedToRepository).save(inscription);
            assertThat(inscription.getIsForfeit()).isFalse();
        }

        @Test
        @DisplayName("Devrait échouer si l'athlète n'est pas inscrit")
        void unforfeitAthlete_shouldFailWhenNotRegistered() {
            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> participantService.unforfeitAthlete(1, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("L'athlète n'est pas inscrit à cette épreuve");
        }
    }

    // ===== TESTS ANNULATION FORFAIT ÉQUIPE =====

    @Nested
    @DisplayName("Tests unforfeitTeam")
    class UnforfeitTeamTests {

        @Test
        @DisplayName("Devrait annuler le forfait d'une équipe avec succès")
        void unforfeitTeam_shouldSucceed() {
            ParticipateAt inscription = new ParticipateAt();
            inscription.setTeam(testTeam);
            inscription.setTrial(testTrial);
            inscription.setIsForfeit(true);

            when(participateAtRepository.findByTrialIdAndTeamId(1, 1)).thenReturn(Optional.of(inscription));
            when(participateAtRepository.save(any(ParticipateAt.class))).thenAnswer(i -> i.getArgument(0));

            ParticipantDTO result = participantService.unforfeitTeam(1, 1);

            assertThat(result).isNotNull();
            assertThat(result.isForfeit()).isFalse();
            verify(participateAtRepository).save(inscription);
            assertThat(inscription.getIsForfeit()).isFalse();
        }

        @Test
        @DisplayName("Devrait échouer si l'équipe n'est pas inscrite")
        void unforfeitTeam_shouldFailWhenNotRegistered() {
            when(participateAtRepository.findByTrialIdAndTeamId(1, 1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> participantService.unforfeitTeam(1, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("L'équipe n'est pas inscrite à cette épreuve");
        }
    }

    // ===== TESTS SUPPRESSION ATHLÈTE =====

    @Nested
    @DisplayName("Tests removeAthleteFromTrial")
    class RemoveAthleteFromTrialTests {

        @Test
        @DisplayName("Devrait retirer un athlète avec succès")
        void removeAthleteFromTrial_shouldSucceed() {
            IsConvenedTo inscription = new IsConvenedTo();
            inscription.setUser(testAthlete);
            inscription.setTrial(testTrial);

            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.of(inscription));
            doNothing().when(isConvenedToRepository).delete(inscription);

            participantService.removeAthleteFromTrial(1, 10);

            verify(isConvenedToRepository).delete(inscription);
        }

        @Test
        @DisplayName("Devrait échouer si l'athlète n'est pas inscrit")
        void removeAthleteFromTrial_shouldFailWhenNotRegistered() {
            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> participantService.removeAthleteFromTrial(1, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("L'athlète n'est pas inscrit à cette épreuve");
        }
    }

    // ===== TESTS SUPPRESSION ÉQUIPE =====

    @Nested
    @DisplayName("Tests removeTeamFromTrial")
    class RemoveTeamFromTrialTests {

        @Test
        @DisplayName("Devrait retirer une équipe avec succès")
        void removeTeamFromTrial_shouldSucceed() {
            ParticipateAt inscription = new ParticipateAt();
            inscription.setTeam(testTeam);
            inscription.setTrial(testTrial);

            when(participateAtRepository.findByTrialIdAndTeamId(1, 1)).thenReturn(Optional.of(inscription));
            doNothing().when(participateAtRepository).delete(inscription);

            participantService.removeTeamFromTrial(1, 1);

            verify(participateAtRepository).delete(inscription);
        }

        @Test
        @DisplayName("Devrait échouer si l'équipe n'est pas inscrite")
        void removeTeamFromTrial_shouldFailWhenNotRegistered() {
            when(participateAtRepository.findByTrialIdAndTeamId(1, 1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> participantService.removeTeamFromTrial(1, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("L'équipe n'est pas inscrite à cette épreuve");
        }
    }

    // ===== TESTS GET TRIAL PARTICIPANTS =====

    @Nested
    @DisplayName("Tests getTrialParticipants")
    class GetTrialParticipantsTests {

        @Test
        @DisplayName("Devrait retourner les participants d'une épreuve solo")
        void getTrialParticipants_shouldReturnAthleteParticipants() {
            IsConvenedTo inscription = new IsConvenedTo();
            inscription.setUser(testAthlete);
            inscription.setIsForfeit(false);

            when(trialRepository.findById(1)).thenReturn(Optional.of(testTrial));
            when(participateAtRepository.hasTeamParticipation(1)).thenReturn(false);
            when(isConvenedToRepository.findByTrialIdOrderedByResult(1)).thenReturn(List.of(inscription));
            when(userRepository.findAthletesNotInTrial(1)).thenReturn(List.of());

            Optional<TrialParticipantsDTO> result = participantService.getTrialParticipants(1);

            assertThat(result).isPresent();
            assertThat(result.get().getTrialId()).isEqualTo(1);
            assertThat(result.get().getTrialName()).isEqualTo("100m Sprint");
            assertThat(result.get().isTeamTrial()).isFalse();
            assertThat(result.get().getParticipants()).hasSize(1);
            assertThat(result.get().getParticipants().get(0).getName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Devrait retourner les participants d'une épreuve en équipe")
        void getTrialParticipants_shouldReturnTeamParticipants() {
            ParticipateAt inscription = new ParticipateAt();
            inscription.setTeam(testTeam);
            inscription.setIsForfeit(false);

            when(trialRepository.findById(1)).thenReturn(Optional.of(testTrial));
            when(participateAtRepository.hasTeamParticipation(1)).thenReturn(true);
            when(participateAtRepository.findByTrialIdOrderedByResult(1)).thenReturn(List.of(inscription));
            when(teamRepository.findTeamsNotInTrial(1)).thenReturn(List.of());

            Optional<TrialParticipantsDTO> result = participantService.getTrialParticipants(1);

            assertThat(result).isPresent();
            assertThat(result.get().isTeamTrial()).isTrue();
            assertThat(result.get().getParticipants()).hasSize(1);
            assertThat(result.get().getParticipants().get(0).getName()).isEqualTo("Team France");
        }

        @Test
        @DisplayName("Devrait retourner empty si l'épreuve n'existe pas")
        void getTrialParticipants_shouldReturnEmptyWhenTrialNotFound() {
            when(trialRepository.findById(999)).thenReturn(Optional.empty());

            Optional<TrialParticipantsDTO> result = participantService.getTrialParticipants(999);

            assertThat(result).isEmpty();
        }
    }

    // ===== TESTS GET TRIALS FOR COMMISSAIRE =====

    @Nested
    @DisplayName("Tests getTrialsForCommissaire")
    class GetTrialsForCommissaireTests {

        @Test
        @DisplayName("Devrait retourner les épreuves assignées au commissaire connecté")
        void getTrialsForCommissaire_shouldReturnAssignedTrials() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("commissaire@test.com");
            when(userRepository.findByEmail("commissaire@test.com")).thenReturn(Optional.of(testCommissaire));
            when(trialRepository.findActiveTrialsAssignedToUser(eq(5), any(LocalDateTime.class)))
                    .thenReturn(List.of(testTrial));
            when(trialRepository.findById(1)).thenReturn(Optional.of(testTrial));
            when(participateAtRepository.hasTeamParticipation(1)).thenReturn(false);
            when(isConvenedToRepository.findByTrialIdOrderedByResult(1)).thenReturn(List.of());
            when(userRepository.findAthletesNotInTrial(1)).thenReturn(List.of());

            List<TrialParticipantsDTO> result = participantService.getTrialsForCommissaire();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTrialId()).isEqualTo(1);
            assertThat(result.get(0).getTrialName()).isEqualTo("100m Sprint");
        }

        @Test
        @DisplayName("Devrait échouer si le commissaire n'est pas trouvé")
        void getTrialsForCommissaire_shouldFailWhenCommissaireNotFound() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("unknown@test.com");
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> participantService.getTrialsForCommissaire())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Commissaire non trouvé");
        }
    }

    @Nested
    @DisplayName("Test getAthleteById")
    class GetAthleteByIdTests {

        @Test
        @DisplayName("Devrait retourner l'athlète grâce à son identifiant")
        void getAthleteById_shouldReturnAthlete() {
            ApplicationUser athleteUser = new ApplicationUser();
            athleteUser.setId(1);
            athleteUser.setName("Jean");
            athleteUser.setLastname("Poule");
            athleteUser.setCountry(france);

            AthleteDTO expected = new AthleteDTO(
                    1,
                    "Jean Poule",
                    "FR"
            );
            when(userRepository.findById(1)).thenReturn(Optional.of(athleteUser));

            Optional<AthleteDTO> result = participantService.getAthleteById(1);

            assertThat(result).isPresent();
            AthleteDTO athleteResult = result.get();

            assertThat(athleteResult).isEqualTo(expected);
        }

        @Test
        @DisplayName("Devrait retourner vide quand l'athlète n'existe pas")
        void getAthleteById_shouldPasReturnAthlete() {
            ApplicationUser athleteUser = new ApplicationUser();
            athleteUser.setId(1);
            athleteUser.setName("Jean");
            athleteUser.setLastname("Poule");
            athleteUser.setCountry(france);

            AthleteDTO expected = new AthleteDTO(
                    1,
                    "Jean Poule",
                    "FR"
            );
            when(userRepository.findById(1)).thenReturn(Optional.of(athleteUser));

            Optional<AthleteDTO> result = participantService.getAthleteById(1);

            assertThat(result).isPresent();
            AthleteDTO athleteResult = result.get();

            assertThat(athleteResult).isEqualTo(expected);
        }
    }
}
