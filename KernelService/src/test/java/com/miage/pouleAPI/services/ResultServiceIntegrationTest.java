package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.result.BulkSetResultRequest;
import com.miage.pouleAPI.dtos.result.ResultDTO;
import com.miage.pouleAPI.dtos.result.SetResultRequest;
import com.miage.pouleAPI.dtos.result.TrialResultsDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests d'intégration ResultService — US Commissaire
 *
 * Données de référence (data.sql de test) :
 *   Trial 6 : "Past Solo Trial"  — timeslot 2025-06-15 (passé) — individuel
 *     - Athlète 3 (Marie Athlete)   : pas de résultat, non validé
 *     - Athlète 5 (John Doe)        : résultat "11.5", non validé
 *   Trial 7 : "Past Team Trial"  — timeslot 2025-06-15 (passé) — équipe
 *     - Équipe 1 (Team A)           : pas de résultat, non validé
 *     - Équipe 2 (Team B)           : résultat "11.9", non validé
 *   Trial 4 : "Team Relay Trial" — timeslot 2026-09-10 (futur)
 *     - Athlètes 3 et 4 inscrits
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests d'intégration ResultService — Gestion des résultats commissaire")
class ResultServiceIntegrationTest {

    // IDs définis dans data.sql de test
    private static final int PAST_SOLO_TRIAL  = 6;
    private static final int PAST_TEAM_TRIAL  = 7;
    private static final int FUTURE_TRIAL     = 4;

    private static final int ATHLETE_MARIE    = 3;   // résultat null
    private static final int ATHLETE_JOHN     = 5;   // résultat "11.5"
    private static final int ATHLETE_JEAN     = 4;   // inscrit trial 4 (futur)

    private static final int TEAM_A           = 1;   // résultat null
    private static final int TEAM_B           = 2;   // résultat "11.9"

    @Autowired
    private ResultServiceImpl resultService;

    // =========================================================================
    // US1 — Lecture des résultats
    // =========================================================================

    @Nested
    @DisplayName("getTrialResults — lecture des résultats")
    class GetTrialResultsTests {

        @Test
        @DisplayName("Épreuve solo passée → participants athlètes retournés")
        void getTrialResults_pastSoloTrial_returnsAthletes() {
            Optional<TrialResultsDTO> result = resultService.getTrialResults(PAST_SOLO_TRIAL);

            assertThat(result).isPresent();
            TrialResultsDTO dto = result.get();
            assertThat(dto.isTeamTrial()).isFalse();
            assertThat(dto.getResults()).hasSize(2);
            assertThat(dto.getResults())
                    .extracting(ResultDTO::getParticipantType)
                    .containsOnly("ATHLETE");
        }

        @Test
        @DisplayName("Épreuve équipe passée → participants équipes retournés")
        void getTrialResults_pastTeamTrial_returnsTeams() {
            Optional<TrialResultsDTO> result = resultService.getTrialResults(PAST_TEAM_TRIAL);

            assertThat(result).isPresent();
            TrialResultsDTO dto = result.get();
            assertThat(dto.isTeamTrial()).isTrue();
            assertThat(dto.getResults()).hasSize(2);
            assertThat(dto.getResults())
                    .extracting(ResultDTO::getParticipantType)
                    .containsOnly("TEAM");
        }

        @Test
        @DisplayName("Épreuve inconnue → Optional vide")
        void getTrialResults_unknownTrial_returnsEmpty() {
            assertThat(resultService.getTrialResults(9999)).isEmpty();
        }

        @Test
        @DisplayName("startTime et endTime sont bien renseignés")
        void getTrialResults_timesPopulated() {
            TrialResultsDTO dto = resultService.getTrialResults(PAST_SOLO_TRIAL).orElseThrow();

            assertThat(dto.getStartTime()).isNotNull();
            assertThat(dto.getEndTime()).isNotNull();
            assertThat(dto.getStartTime()).isBefore(dto.getEndTime());
        }

        @Test
        @DisplayName("Le nom de l'épreuve est correctement retourné")
        void getTrialResults_trialNameReturned() {
            TrialResultsDTO dto = resultService.getTrialResults(PAST_SOLO_TRIAL).orElseThrow();

            assertThat(dto.getTrialName()).isEqualTo("Past Solo Trial");
        }
    }

    // =========================================================================
    // US1 — Saisie d'un résultat athlète
    // =========================================================================

    @Nested
    @DisplayName("setAthleteResult — saisie résultat athlète")
    class SetAthleteResultTests {

        @Test
        @DisplayName("Saisie réussie pour un athlète sans résultat")
        void setAthleteResult_noExistingResult_success() {
            ResultDTO result = resultService.setAthleteResult(PAST_SOLO_TRIAL, ATHLETE_MARIE, "10.9");

            assertThat(result.getResult()).isEqualTo("10.9");
            assertThat(result.getParticipantType()).isEqualTo("ATHLETE");
            assertThat(result.getIsValidated()).isFalse();

            // Vérifié en DB
            TrialResultsDTO dto = resultService.getTrialResults(PAST_SOLO_TRIAL).orElseThrow();
            assertThat(dto.getResults())
                    .filteredOn(r -> r.getParticipantId().equals(ATHLETE_MARIE))
                    .extracting(ResultDTO::getResult)
                    .containsExactly("10.9");
        }

        @Test
        @DisplayName("US2 — Modification d'un résultat existant")
        void setAthleteResult_existingResult_updated() {
            // John a déjà "11.5", on l'écrase
            ResultDTO result = resultService.setAthleteResult(PAST_SOLO_TRIAL, ATHLETE_JOHN, "10.8");

            assertThat(result.getResult()).isEqualTo("10.8");
        }

        @Test
        @DisplayName("Athlète non inscrit → IllegalArgumentException")
        void setAthleteResult_notRegistered_throws() {
            assertThatThrownBy(() -> resultService.setAthleteResult(PAST_SOLO_TRIAL, ATHLETE_JEAN, "11.0"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("n'est pas inscrit");
        }

        @Test
        @DisplayName("Épreuve non commencée → IllegalStateException")
        void setAthleteResult_trialNotStarted_throws() {
            assertThatThrownBy(() -> resultService.setAthleteResult(FUTURE_TRIAL, ATHLETE_JEAN, "11.0"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("n'a pas encore commencé");
        }

        @Test
        @DisplayName("Épreuve introuvable → IllegalArgumentException")
        void setAthleteResult_trialNotFound_throws() {
            assertThatThrownBy(() -> resultService.setAthleteResult(9999, ATHLETE_MARIE, "11.0"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Épreuve non trouvée");
        }
    }

    // =========================================================================
    // US1 — Saisie d'un résultat équipe
    // =========================================================================

    @Nested
    @DisplayName("setTeamResult — saisie résultat équipe")
    class SetTeamResultTests {

        @Test
        @DisplayName("Saisie réussie pour une équipe sans résultat")
        void setTeamResult_noExistingResult_success() {
            ResultDTO result = resultService.setTeamResult(PAST_TEAM_TRIAL, TEAM_A, "11.3");

            assertThat(result.getResult()).isEqualTo("11.3");
            assertThat(result.getParticipantType()).isEqualTo("TEAM");
            assertThat(result.getIsValidated()).isFalse();
        }

        @Test
        @DisplayName("US2 — Modification d'un résultat équipe existant")
        void setTeamResult_existingResult_updated() {
            ResultDTO result = resultService.setTeamResult(PAST_TEAM_TRIAL, TEAM_B, "11.7");

            assertThat(result.getResult()).isEqualTo("11.7");
        }

        @Test
        @DisplayName("Équipe non inscrite → IllegalArgumentException")
        void setTeamResult_notRegistered_throws() {
            assertThatThrownBy(() -> resultService.setTeamResult(PAST_TEAM_TRIAL, 99, "11.0"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("n'est pas inscrite");
        }

        @Test
        @DisplayName("Épreuve non commencée → IllegalStateException")
        void setTeamResult_trialNotStarted_throws() {
            assertThatThrownBy(() -> resultService.setTeamResult(FUTURE_TRIAL, TEAM_A, "11.0"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // =========================================================================
    // US2 — Modification en masse
    // =========================================================================

    @Nested
    @DisplayName("setBulkResults — modification en masse")
    class SetBulkResultsTests {

        @Test
        @DisplayName("Saisie en masse pour plusieurs athlètes")
        void setBulkResults_athletes_allUpdated() {
            SetResultRequest r1 = new SetResultRequest();
            r1.setParticipantId(ATHLETE_MARIE);
            r1.setParticipantType("ATHLETE");
            r1.setResult("10.7");

            SetResultRequest r2 = new SetResultRequest();
            r2.setParticipantId(ATHLETE_JOHN);
            r2.setParticipantType("ATHLETE");
            r2.setResult("10.5");

            BulkSetResultRequest bulk = new BulkSetResultRequest();
            bulk.setResults(List.of(r1, r2));

            List<ResultDTO> results = resultService.setBulkResults(PAST_SOLO_TRIAL, bulk);

            assertThat(results).hasSize(2);
            assertThat(results).extracting(ResultDTO::getResult)
                    .containsExactlyInAnyOrder("10.7", "10.5");
        }

        @Test
        @DisplayName("Saisie en masse pour plusieurs équipes")
        void setBulkResults_teams_allUpdated() {
            SetResultRequest r1 = new SetResultRequest();
            r1.setParticipantId(TEAM_A);
            r1.setParticipantType("TEAM");
            r1.setResult("11.1");

            SetResultRequest r2 = new SetResultRequest();
            r2.setParticipantId(TEAM_B);
            r2.setParticipantType("TEAM");
            r2.setResult("11.4");

            BulkSetResultRequest bulk = new BulkSetResultRequest();
            bulk.setResults(List.of(r1, r2));

            List<ResultDTO> results = resultService.setBulkResults(PAST_TEAM_TRIAL, bulk);

            assertThat(results).hasSize(2);
            assertThat(results).extracting(ResultDTO::getResult)
                    .containsExactlyInAnyOrder("11.1", "11.4");
        }

        @Test
        @DisplayName("Épreuve non commencée → IllegalStateException")
        void setBulkResults_trialNotStarted_throws() {
            BulkSetResultRequest emptyRequest = new BulkSetResultRequest();

            assertThatThrownBy(() -> resultService.setBulkResults(FUTURE_TRIAL, emptyRequest))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Type de participant invalide → IllegalArgumentException")
        void setBulkResults_invalidType_throws() {
            SetResultRequest req = new SetResultRequest();
            req.setParticipantId(1);
            req.setParticipantType("ROBOT");
            req.setResult("11.0");

            BulkSetResultRequest bulk = new BulkSetResultRequest();
            bulk.setResults(List.of(req));

            assertThatThrownBy(() -> resultService.setBulkResults(PAST_SOLO_TRIAL, bulk))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Type de participant invalide");
        }
    }

    // =========================================================================
    // US3 — Validation individuelle
    // =========================================================================

    @Nested
    @DisplayName("validateAthleteResult / validateTeamResult — validation individuelle")
    class ValidateIndividualTests {

        @Test
        @DisplayName("Valider résultat athlète → isValidated passe à true en DB")
        void validateAthleteResult_persistedInDatabase() {
            ResultDTO validated = resultService.validateAthleteResult(PAST_SOLO_TRIAL, ATHLETE_JOHN);

            assertThat(validated.getIsValidated()).isTrue();

            // Vérification en relisant depuis la DB
            TrialResultsDTO dto = resultService.getTrialResults(PAST_SOLO_TRIAL).orElseThrow();
            assertThat(dto.getResults())
                    .filteredOn(r -> r.getParticipantId().equals(ATHLETE_JOHN))
                    .extracting(ResultDTO::getIsValidated)
                    .containsExactly(true);
        }

        @Test
        @DisplayName("Valider résultat équipe → isValidated passe à true en DB")
        void validateTeamResult_persistedInDatabase() {
            ResultDTO validated = resultService.validateTeamResult(PAST_TEAM_TRIAL, TEAM_B);

            assertThat(validated.getIsValidated()).isTrue();

            TrialResultsDTO dto = resultService.getTrialResults(PAST_TEAM_TRIAL).orElseThrow();
            assertThat(dto.getResults())
                    .filteredOn(r -> r.getParticipantId().equals(TEAM_B))
                    .extracting(ResultDTO::getIsValidated)
                    .containsExactly(true);
        }

        @Test
        @DisplayName("Valider athlète non inscrit → IllegalArgumentException")
        void validateAthleteResult_notRegistered_throws() {
            assertThatThrownBy(() -> resultService.validateAthleteResult(PAST_SOLO_TRIAL, ATHLETE_JEAN))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Valider sur épreuve future → IllegalStateException")
        void validateAthleteResult_trialNotStarted_throws() {
            assertThatThrownBy(() -> resultService.validateAthleteResult(FUTURE_TRIAL, ATHLETE_JEAN))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("n'a pas encore commencé");
        }

        @Test
        @DisplayName("Valider équipe sur épreuve future → IllegalStateException")
        void validateTeamResult_trialNotStarted_throws() {
            assertThatThrownBy(() -> resultService.validateTeamResult(FUTURE_TRIAL, TEAM_A))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // =========================================================================
    // US3 — Validation en masse
    // =========================================================================

    @Nested
    @DisplayName("validateAllResults — validation en masse")
    class ValidateAllResultsTests {

        @Test
        @DisplayName("Valider tout sur épreuve solo → tous les athlètes validés en DB")
        void validateAllResults_soloTrial_allAthletesValidated() {
            TrialResultsDTO dto = resultService.validateAllResults(PAST_SOLO_TRIAL);

            assertThat(dto.getResults())
                    .extracting(ResultDTO::getIsValidated)
                    .containsOnly(true);

            // Relecture DB
            TrialResultsDTO reread = resultService.getTrialResults(PAST_SOLO_TRIAL).orElseThrow();
            assertThat(reread.getResults())
                    .extracting(ResultDTO::getIsValidated)
                    .containsOnly(true);
        }

        @Test
        @DisplayName("Valider tout sur épreuve équipe → toutes les équipes validées en DB")
        void validateAllResults_teamTrial_allTeamsValidated() {
            TrialResultsDTO dto = resultService.validateAllResults(PAST_TEAM_TRIAL);

            assertThat(dto.getResults())
                    .extracting(ResultDTO::getIsValidated)
                    .containsOnly(true);
        }

        @Test
        @DisplayName("Épreuve non commencée → IllegalStateException")
        void validateAllResults_trialNotStarted_throws() {
            assertThatThrownBy(() -> resultService.validateAllResults(FUTURE_TRIAL))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("n'a pas encore commencé");
        }
    }

    // =========================================================================
    // US3 — Invalidation individuelle
    // =========================================================================

    @Nested
    @DisplayName("invalidateAthleteResult / invalidateTeamResult — invalidation")
    class InvalidateTests {

        @Test
        @DisplayName("Invalider un athlète déjà validé → isValidated repasse à false")
        void invalidateAthleteResult_afterValidation_falseInDB() {
            // Valider d'abord
            resultService.validateAthleteResult(PAST_SOLO_TRIAL, ATHLETE_JOHN);

            // Puis invalider
            ResultDTO result = resultService.invalidateAthleteResult(PAST_SOLO_TRIAL, ATHLETE_JOHN);
            assertThat(result.getIsValidated()).isFalse();

            // Vérification DB
            TrialResultsDTO dto = resultService.getTrialResults(PAST_SOLO_TRIAL).orElseThrow();
            assertThat(dto.getResults())
                    .filteredOn(r -> r.getParticipantId().equals(ATHLETE_JOHN))
                    .extracting(ResultDTO::getIsValidated)
                    .containsExactly(false);
        }

        @Test
        @DisplayName("Invalider une équipe déjà validée → isValidated repasse à false")
        void invalidateTeamResult_afterValidation_falseInDB() {
            resultService.validateTeamResult(PAST_TEAM_TRIAL, TEAM_B);

            ResultDTO result = resultService.invalidateTeamResult(PAST_TEAM_TRIAL, TEAM_B);
            assertThat(result.getIsValidated()).isFalse();
        }

        @Test
        @DisplayName("Invalider athlète non inscrit → IllegalArgumentException")
        void invalidateAthleteResult_notRegistered_throws() {
            assertThatThrownBy(() -> resultService.invalidateAthleteResult(PAST_SOLO_TRIAL, ATHLETE_JEAN))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Invalider équipe non inscrite → IllegalArgumentException")
        void invalidateTeamResult_notRegistered_throws() {
            assertThatThrownBy(() -> resultService.invalidateTeamResult(PAST_TEAM_TRIAL, 99))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("n'est pas inscrite");
        }

        @Test
        @DisplayName("Épreuve non commencée → IllegalStateException")
        void invalidateAthleteResult_trialNotStarted_throws() {
            assertThatThrownBy(() -> resultService.invalidateAthleteResult(FUTURE_TRIAL, ATHLETE_JEAN))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
