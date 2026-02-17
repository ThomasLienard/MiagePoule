package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.participant.ParticipantDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests d'intégration - Déclaration de forfait par un sportif")
class AthleteDeclareWithdrawalIntegrationTest {

    @Autowired
    private ParticipantServiceImpl participantService;

    @Nested
    @DisplayName("Tests athleteDeclareWithdrawal")
    class AthleteDeclareWithdrawalTests {

        @Test
        @DisplayName("Devrait permettre à un athlète de déclarer forfait")
        void athleteDeclareWithdrawal_shouldSucceed() {
            // L'athlète athlete@test.com (id=3) est inscrit à l'épreuve Trial 5 (data.sql)
            // Cette épreuve doit être dans le futur pour ce test
            
            ParticipantDTO result = participantService.athleteDeclareWithdrawal(5, "athlete@test.com");

            assertThat(result).isNotNull();
            assertThat(result.isForfeit()).isTrue();
            assertThat(result.getType()).isEqualTo("ATHLETE");
        }

        @Test
        @DisplayName("Devrait échouer si l'athlète n'est pas inscrit")
        void athleteDeclareWithdrawal_shouldFailWhenNotRegistered() {
            // L'athlète n'est pas inscrit à Trial 2 (qui est dans le futur)
            assertThatThrownBy(() -> participantService.athleteDeclareWithdrawal(2, "athlete@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Vous n'êtes pas inscrit à cette épreuve");
        }

        @Test
        @DisplayName("Devrait échouer si l'épreuve n'existe pas")
        void athleteDeclareWithdrawal_shouldFailWhenTrialNotFound() {
            assertThatThrownBy(() -> participantService.athleteDeclareWithdrawal(999, "athlete@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Épreuve non trouvée");
        }

        @Test
        @DisplayName("Devrait échouer si l'athlète n'existe pas")
        void athleteDeclareWithdrawal_shouldFailWhenAthleteNotFound() {
            assertThatThrownBy(() -> participantService.athleteDeclareWithdrawal(5, "nonexistent@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Athlète non trouvé");
        }

        @Test
        @DisplayName("Devrait échouer si déjà forfait")
        void athleteDeclareWithdrawal_shouldFailWhenAlreadyForfeit() {
            // Premier forfait
            participantService.athleteDeclareWithdrawal(5, "athlete@test.com");
            
            // Deuxième tentative
            assertThatThrownBy(() -> participantService.athleteDeclareWithdrawal(5, "athlete@test.com"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Vous êtes déjà déclaré forfait pour cette épreuve");
        }

        // Note: Le test pour l'épreuve terminée nécessiterait soit:
        // - Une épreuve dans le passé dans data.sql avec un athlète inscrit
        // - Ou la capacité de modifier les dates des TimeSlot pendant le test
        // Ce test serait à implémenter selon la stratégie de test choisie
    }
}
