package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.result.BulkSetResultRequest;
import com.miage.pouleAPI.dtos.result.ResultDTO;
import com.miage.pouleAPI.dtos.result.SetResultRequest;
import com.miage.pouleAPI.dtos.result.TrialResultsDTO;
import com.miage.pouleAPI.entity.*;
import com.miage.pouleAPI.repositories.IsConvenedToRepository;
import com.miage.pouleAPI.repositories.ParticipateAtRepository;
import com.miage.pouleAPI.repositories.TrialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires ResultService — US Commissaire (saisie, modification, validation)")
class ResultServiceImplTest {

    @Mock private TrialRepository trialRepository;
    @Mock private IsConvenedToRepository isConvenedToRepository;
    @Mock private ParticipateAtRepository participateAtRepository;

    @InjectMocks
    private ResultServiceImpl resultService;

    // ── Données communes ───────────────────────────────────────────────────────

    private Trial pastTrial;       // épreuve déjà commencée (editable)
    private Trial futureTrial;     // épreuve pas encore commencée (bloquée)
    private Trial noSlotTrial;     // épreuve sans créneau (editable par défaut)

    private ApplicationUser athlete;
    private Team team;
    private Country france;

    private IsConvenedTo convocation;
    private ParticipateAt participation;

    @BeforeEach
    void setUp() {
        france = new Country();
        france.setCode("FR");

        // Épreuve déjà commencée
        TimeSlot pastSlot = new TimeSlot();
        pastSlot.setStart(LocalDateTime.now().minusHours(2));
        pastSlot.setEnd(LocalDateTime.now().plusHours(1));

        pastTrial = new Trial();
        pastTrial.setId(1);
        pastTrial.setName("100m Final");
        pastTrial.setTimeSlot(pastSlot);

        // Épreuve dans le futur
        TimeSlot futureSlot = new TimeSlot();
        futureSlot.setStart(LocalDateTime.now().plusDays(1));
        futureSlot.setEnd(LocalDateTime.now().plusDays(1).plusHours(2));

        futureTrial = new Trial();
        futureTrial.setId(2);
        futureTrial.setName("200m Final");
        futureTrial.setTimeSlot(futureSlot);

        // Épreuve sans créneau
        noSlotTrial = new Trial();
        noSlotTrial.setId(3);
        noSlotTrial.setName("Sans créneau");
        noSlotTrial.setTimeSlot(null);

        // Athlète
        athlete = new ApplicationUser();
        athlete.setId(10);
        athlete.setName("Marie");
        athlete.setLastname("Dupont");
        athlete.setCountry(france);

        // Équipe
        team = new Team();
        team.setId(5);
        team.setName("Team France");
        team.setCountry(france);

        // Convocation athlète (lien épreuve-athlète)
        convocation = new IsConvenedTo();
        convocation.setUser(athlete);
        convocation.setResult(null);
        convocation.setIsValidated(false);
        convocation.setIsForfeit(false);

        // Participation équipe (lien épreuve-équipe)
        participation = new ParticipateAt();
        participation.setTeam(team);
        participation.setResult(null);
        participation.setIsValidated(false);
        participation.setIsForfeit(false);
    }

    // =========================================================================
    // getTrialResults
    // =========================================================================

    @Nested
    @DisplayName("getTrialResults — lecture des résultats")
    class GetTrialResultsTests {

        @Test
        @DisplayName("Épreuve inconnue → Optional vide")
        void getTrialResults_unknownTrial_returnsEmpty() {
            when(trialRepository.findById(99)).thenReturn(Optional.empty());

            Optional<TrialResultsDTO> result = resultService.getTrialResults(99);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Épreuve individuelle → résultats athlètes retournés")
        void getTrialResults_athleteTrial_returnsAthleteResults() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(participateAtRepository.hasTeamParticipation(1)).thenReturn(false);
            when(isConvenedToRepository.findByTrialId(1)).thenReturn(List.of(convocation));

            Optional<TrialResultsDTO> result = resultService.getTrialResults(1);

            assertThat(result).isPresent();
            TrialResultsDTO dto = result.get();
            assertThat(dto.isTeamTrial()).isFalse();
            assertThat(dto.getResults()).hasSize(1);
            assertThat(dto.getResults().get(0).getParticipantName()).isEqualTo("Marie Dupont");
            assertThat(dto.getResults().get(0).getParticipantType()).isEqualTo("ATHLETE");
        }

        @Test
        @DisplayName("Épreuve en équipe → résultats équipes retournés")
        void getTrialResults_teamTrial_returnsTeamResults() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(participateAtRepository.hasTeamParticipation(1)).thenReturn(true);
            when(participateAtRepository.findByTrialId(1)).thenReturn(List.of(participation));

            Optional<TrialResultsDTO> result = resultService.getTrialResults(1);

            assertThat(result).isPresent();
            TrialResultsDTO dto = result.get();
            assertThat(dto.isTeamTrial()).isTrue();
            assertThat(dto.getResults()).hasSize(1);
            assertThat(dto.getResults().get(0).getParticipantName()).isEqualTo("Team France");
            assertThat(dto.getResults().get(0).getParticipantType()).isEqualTo("TEAM");
        }

        @Test
        @DisplayName("Les dates start/end sont bien renseignées dans le DTO")
        void getTrialResults_timesArePopulated() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(participateAtRepository.hasTeamParticipation(1)).thenReturn(false);
            when(isConvenedToRepository.findByTrialId(1)).thenReturn(List.of());

            TrialResultsDTO dto = resultService.getTrialResults(1).orElseThrow();

            assertThat(dto.getStartTime()).isEqualTo(pastTrial.getTimeSlot().getStart());
            assertThat(dto.getEndTime()).isEqualTo(pastTrial.getTimeSlot().getEnd());
        }
    }

    // =========================================================================
    // setAthleteResult
    // =========================================================================

    @Nested
    @DisplayName("setAthleteResult — saisie/modification résultat athlète")
    class SetAthleteResultTests {

        @Test
        @DisplayName("Saisie réussie → résultat mis à jour")
        void setAthleteResult_success() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.of(convocation));
            when(isConvenedToRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            ResultDTO result = resultService.setAthleteResult(1, 10, "11.5");

            assertThat(result.getResult()).isEqualTo("11.5");
            assertThat(result.getParticipantName()).isEqualTo("Marie Dupont");
            assertThat(result.getParticipantType()).isEqualTo("ATHLETE");
            assertThat(result.getIsValidated()).isFalse();
        }

        @Test
        @DisplayName("Épreuve introuvable → IllegalArgumentException")
        void setAthleteResult_trialNotFound_throws() {
            when(trialRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> resultService.setAthleteResult(99, 10, "11.5"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Épreuve non trouvée");
        }

        @Test
        @DisplayName("Athlète non inscrit → IllegalArgumentException")
        void setAthleteResult_athleteNotRegistered_throws() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(isConvenedToRepository.findByTrialIdAndUserId(1, 99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> resultService.setAthleteResult(1, 99, "11.5"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("n'est pas inscrit");
        }

        @Test
        @DisplayName("Épreuve pas encore commencée → IllegalStateException")
        void setAthleteResult_trialNotStarted_throws() {
            when(trialRepository.findById(2)).thenReturn(Optional.of(futureTrial));

            assertThatThrownBy(() -> resultService.setAthleteResult(2, 10, "11.5"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("n'a pas encore commencé");
        }

        @Test
        @DisplayName("Épreuve sans créneau → saisie autorisée")
        void setAthleteResult_noTimeSlot_succeeds() {
            when(trialRepository.findById(3)).thenReturn(Optional.of(noSlotTrial));
            when(isConvenedToRepository.findByTrialIdAndUserId(3, 10)).thenReturn(Optional.of(convocation));
            when(isConvenedToRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            assertThatCode(() -> resultService.setAthleteResult(3, 10, "11.5"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Résultat négatif → IllegalArgumentException")
        void setAthleteResult_negativeResult_throws() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));

            assertThatThrownBy(() -> resultService.setAthleteResult(1, 10, "-5.5"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ne peut pas être négatif");
        }

        @Test
        @DisplayName("Résultat non numérique → IllegalArgumentException")
        void setAthleteResult_nonNumericResult_throws() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));

            assertThatThrownBy(() -> resultService.setAthleteResult(1, 10, "abc"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nombre valide");
        }

        @Test
        @DisplayName("Résultat avec suffixe → IllegalArgumentException")
        void setAthleteResult_resultWithSuffix_throws() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));

            assertThatThrownBy(() -> resultService.setAthleteResult(1, 10, "11.5s"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nombre valide");
        }

        @Test
        @DisplayName("Résultat null → accepté (suppression)")
        void setAthleteResult_nullResult_succeeds() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.of(convocation));
            when(isConvenedToRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            assertThatCode(() -> resultService.setAthleteResult(1, 10, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Résultat vide → accepté (suppression)")
        void setAthleteResult_emptyResult_succeeds() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.of(convocation));
            when(isConvenedToRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            assertThatCode(() -> resultService.setAthleteResult(1, 10, ""))
                    .doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // setTeamResult
    // =========================================================================

    @Nested
    @DisplayName("setTeamResult — saisie/modification résultat équipe")
    class SetTeamResultTests {

        @Test
        @DisplayName("Saisie réussie → résultat équipe mis à jour")
        void setTeamResult_success() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(participateAtRepository.findByTrialIdAndTeamId(1, 5)).thenReturn(Optional.of(participation));
            when(participateAtRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            ResultDTO result = resultService.setTeamResult(1, 5, "11.2");

            assertThat(result.getResult()).isEqualTo("11.2");
            assertThat(result.getParticipantName()).isEqualTo("Team France");
            assertThat(result.getParticipantType()).isEqualTo("TEAM");
        }

        @Test
        @DisplayName("Équipe non inscrite → IllegalArgumentException")
        void setTeamResult_teamNotRegistered_throws() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(participateAtRepository.findByTrialIdAndTeamId(1, 99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> resultService.setTeamResult(1, 99, "11.2"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("n'est pas inscrite");
        }

        @Test
        @DisplayName("Épreuve pas encore commencée → IllegalStateException")
        void setTeamResult_trialNotStarted_throws() {
            when(trialRepository.findById(2)).thenReturn(Optional.of(futureTrial));

            assertThatThrownBy(() -> resultService.setTeamResult(2, 5, "11.2"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("n'a pas encore commencé");
        }

        @Test
        @DisplayName("Résultat négatif → IllegalArgumentException")
        void setTeamResult_negativeResult_throws() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));

            assertThatThrownBy(() -> resultService.setTeamResult(1, 5, "-10.5"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ne peut pas être négatif");
        }

        @Test
        @DisplayName("Résultat non numérique → IllegalArgumentException")
        void setTeamResult_nonNumericResult_throws() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));

            assertThatThrownBy(() -> resultService.setTeamResult(1, 5, "invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nombre valide");
        }
    }

    // =========================================================================
    // setBulkResults
    // =========================================================================

    @Nested
    @DisplayName("setBulkResults — saisie en masse")
    class SetBulkResultsTests {

        @Test
        @DisplayName("Saisie en masse athlètes réussie → liste de ResultDTO")
        void setBulkResults_athletes_success() {
            convocation.setUser(athlete);
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.of(convocation));
            when(isConvenedToRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            SetResultRequest req = new SetResultRequest();
            req.setParticipantId(10);
            req.setParticipantType("ATHLETE");
            req.setResult("11.5");

            BulkSetResultRequest bulk = new BulkSetResultRequest();
            bulk.setResults(List.of(req));

            List<ResultDTO> results = resultService.setBulkResults(1, bulk);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getResult()).isEqualTo("11.5");
        }

        @Test
        @DisplayName("Saisie en masse équipes réussie → liste de ResultDTO")
        void setBulkResults_teams_success() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(participateAtRepository.findByTrialIdAndTeamId(1, 5)).thenReturn(Optional.of(participation));
            when(participateAtRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            SetResultRequest req = new SetResultRequest();
            req.setParticipantId(5);
            req.setParticipantType("TEAM");
            req.setResult("11.2");

            BulkSetResultRequest bulk = new BulkSetResultRequest();
            bulk.setResults(List.of(req));

            List<ResultDTO> results = resultService.setBulkResults(1, bulk);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getParticipantType()).isEqualTo("TEAM");
        }

        @Test
        @DisplayName("Type de participant invalide → IllegalArgumentException")
        void setBulkResults_invalidType_throws() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));

            SetResultRequest req = new SetResultRequest();
            req.setParticipantId(1);
            req.setParticipantType("INCONNU");
            req.setResult("11.5");

            BulkSetResultRequest bulk = new BulkSetResultRequest();
            bulk.setResults(List.of(req));

            assertThatThrownBy(() -> resultService.setBulkResults(1, bulk))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Type de participant invalide");
        }

        @Test
        @DisplayName("Épreuve pas encore commencée → IllegalStateException")
        void setBulkResults_trialNotStarted_throws() {
            when(trialRepository.findById(2)).thenReturn(Optional.of(futureTrial));
            BulkSetResultRequest emptyRequest = new BulkSetResultRequest();

            assertThatThrownBy(() -> resultService.setBulkResults(2, emptyRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("n'a pas encore commencé");
        }
    }

    // =========================================================================
    // validateAthleteResult
    // =========================================================================

    @Nested
    @DisplayName("validateAthleteResult — validation résultat athlète")
    class ValidateAthleteResultTests {

        @Test
        @DisplayName("Validation réussie → isValidated passe à true")
        void validateAthleteResult_success() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.of(convocation));
            when(isConvenedToRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            ResultDTO result = resultService.validateAthleteResult(1, 10);

            assertThat(result.getIsValidated()).isTrue();
            verify(isConvenedToRepository).save(argThat(c -> Boolean.TRUE.equals(c.getIsValidated())));
        }

        @Test
        @DisplayName("Athlète non inscrit → IllegalArgumentException")
        void validateAthleteResult_notRegistered_throws() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(isConvenedToRepository.findByTrialIdAndUserId(1, 99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> resultService.validateAthleteResult(1, 99))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Épreuve pas encore commencée → IllegalStateException")
        void validateAthleteResult_trialNotStarted_throws() {
            when(trialRepository.findById(2)).thenReturn(Optional.of(futureTrial));

            assertThatThrownBy(() -> resultService.validateAthleteResult(2, 10))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("n'a pas encore commencé");
        }
    }

    // =========================================================================
    // validateTeamResult
    // =========================================================================

    @Nested
    @DisplayName("validateTeamResult — validation résultat équipe")
    class ValidateTeamResultTests {

        @Test
        @DisplayName("Validation réussie → isValidated passe à true")
        void validateTeamResult_success() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(participateAtRepository.findByTrialIdAndTeamId(1, 5)).thenReturn(Optional.of(participation));
            when(participateAtRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            ResultDTO result = resultService.validateTeamResult(1, 5);

            assertThat(result.getIsValidated()).isTrue();
            verify(participateAtRepository).save(argThat(p -> Boolean.TRUE.equals(p.getIsValidated())));
        }

        @Test
        @DisplayName("Épreuve pas encore commencée → IllegalStateException")
        void validateTeamResult_trialNotStarted_throws() {
            when(trialRepository.findById(2)).thenReturn(Optional.of(futureTrial));

            assertThatThrownBy(() -> resultService.validateTeamResult(2, 5))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // =========================================================================
    // validateAllResults
    // =========================================================================

    @Nested
    @DisplayName("validateAllResults — validation en masse")
    class ValidateAllResultsTests {

        @Test
        @DisplayName("Épreuve individuelle : tous les athlètes sont validés")
        void validateAllResults_athleteTrial_validatesAll() {
            IsConvenedTo c2 = new IsConvenedTo();
            c2.setUser(athlete);
            c2.setIsValidated(false);
            c2.setIsForfeit(false);

            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(participateAtRepository.hasTeamParticipation(1)).thenReturn(false);
            when(isConvenedToRepository.findByTrialId(1)).thenReturn(List.of(convocation, c2));
            when(isConvenedToRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
            // deuxième appel interne à getTrialResults
            when(participateAtRepository.hasTeamParticipation(1)).thenReturn(false);

            resultService.validateAllResults(1);

            verify(isConvenedToRepository).saveAll(argThat(list -> {
                for (IsConvenedTo c : list) {
                    if (!Boolean.TRUE.equals(c.getIsValidated())) return false;
                }
                return true;
            }));
        }

        @Test
        @DisplayName("Épreuve en équipe : toutes les équipes sont validées")
        void validateAllResults_teamTrial_validatesAll() {
            ParticipateAt p2 = new ParticipateAt();
            p2.setTeam(team);
            p2.setIsValidated(false);
            p2.setIsForfeit(false);

            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(participateAtRepository.hasTeamParticipation(1)).thenReturn(true);
            when(participateAtRepository.findByTrialId(1)).thenReturn(List.of(participation, p2));
            when(participateAtRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

            resultService.validateAllResults(1);

            verify(participateAtRepository).saveAll(argThat(list -> {
                for (ParticipateAt p : list) {
                    if (!Boolean.TRUE.equals(p.getIsValidated())) return false;
                }
                return true;
            }));
        }

        @Test
        @DisplayName("Épreuve pas encore commencée → IllegalStateException")
        void validateAllResults_trialNotStarted_throws() {
            when(trialRepository.findById(2)).thenReturn(Optional.of(futureTrial));

            assertThatThrownBy(() -> resultService.validateAllResults(2))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("n'a pas encore commencé");
        }
    }

    // =========================================================================
    // invalidateAthleteResult
    // =========================================================================

    @Nested
    @DisplayName("invalidateAthleteResult — invalidation résultat athlète")
    class InvalidateAthleteResultTests {

        @Test
        @DisplayName("Invalidation réussie → isValidated repasse à false")
        void invalidateAthleteResult_success() {
            convocation.setIsValidated(true);

            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(isConvenedToRepository.findByTrialIdAndUserId(1, 10)).thenReturn(Optional.of(convocation));
            when(isConvenedToRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            ResultDTO result = resultService.invalidateAthleteResult(1, 10);

            assertThat(result.getIsValidated()).isFalse();
            verify(isConvenedToRepository).save(argThat(c -> Boolean.FALSE.equals(c.getIsValidated())));
        }

        @Test
        @DisplayName("Épreuve pas encore commencée → IllegalStateException")
        void invalidateAthleteResult_trialNotStarted_throws() {
            when(trialRepository.findById(2)).thenReturn(Optional.of(futureTrial));

            assertThatThrownBy(() -> resultService.invalidateAthleteResult(2, 10))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // =========================================================================
    // invalidateTeamResult
    // =========================================================================

    @Nested
    @DisplayName("invalidateTeamResult — invalidation résultat équipe")
    class InvalidateTeamResultTests {

        @Test
        @DisplayName("Invalidation réussie → isValidated repasse à false")
        void invalidateTeamResult_success() {
            participation.setIsValidated(true);

            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(participateAtRepository.findByTrialIdAndTeamId(1, 5)).thenReturn(Optional.of(participation));
            when(participateAtRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            ResultDTO result = resultService.invalidateTeamResult(1, 5);

            assertThat(result.getIsValidated()).isFalse();
            verify(participateAtRepository).save(argThat(p -> Boolean.FALSE.equals(p.getIsValidated())));
        }

        @Test
        @DisplayName("Épreuve pas encore commencée → IllegalStateException")
        void invalidateTeamResult_trialNotStarted_throws() {
            when(trialRepository.findById(2)).thenReturn(Optional.of(futureTrial));

            assertThatThrownBy(() -> resultService.invalidateTeamResult(2, 5))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Équipe non inscrite → IllegalArgumentException")
        void invalidateTeamResult_teamNotRegistered_throws() {
            when(trialRepository.findById(1)).thenReturn(Optional.of(pastTrial));
            when(participateAtRepository.findByTrialIdAndTeamId(1, 99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> resultService.invalidateTeamResult(1, 99))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("n'est pas inscrite");
        }
    }
}
