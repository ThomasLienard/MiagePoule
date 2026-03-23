package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.Trial;
import com.miage.pouleAPI.entity.Event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Intégration TrialRepository avec data.sql")
class TrialRepositoryIntegrationTest {

    @Autowired
    private TrialRepository trialRepository;


    @Test
    @DisplayName("findAll() doit retourner le trial défini dans data.sql")
    void findAll_shouldReturnTrialsFromDataSql() {
        List<Trial> trials = trialRepository.findAll();

        // Il y a au moins le trial (id_trial=1) inséré dans data.sql
        assertThat(trials).isNotEmpty();
        assertThat(trials)
            .extracting(Trial::getId)
            .contains(1);
    }

    @Test
    @DisplayName("findById(2) doit retourner le trial lié à l'event 2")
    void findById_shouldReturnTrialWithEvent() {
        Optional<Trial> optTrial = trialRepository.findById(2);

        assertThat(optTrial).isPresent();
        Trial trial = optTrial.get();

        // Vérifie l'id du trial
        assertThat(trial.getId()).isEqualTo(2);

        // Vérifie la relation avec Event (id_event=2 dans data.sql)
        Event event = trial;
        assertThat(event).isNotNull();
        assertThat(event.getId()).isEqualTo(2);

        // Optionnel : vérifier quelques champs de l'event
        assertThat(event.getName()).isEqualTo("Final Sprint Race");
        assertThat(event.getDescription()).isEqualTo("Official competition");
    }

    @Test
    @DisplayName("findById(999) doit retourner Optional.empty()")
    void findById_unknownId_shouldReturnEmpty() {
        Optional<Trial> optTrial = trialRepository.findById(999);

        assertThat(optTrial).isEmpty();
    }

    @Test
    @DisplayName("Les entités Trial doivent être correctement mappées avec les autres relations")
    void trial_shouldHaveMappedRelations() {
        Optional<Trial> optTrial = trialRepository.findById(2);

        assertThat(optTrial).isPresent();
        Trial trial = optTrial.get();

        Event event = trial;
        assertThat(event).isNotNull();

        // Depuis data.sql : id_event=2, type_event_name='TRIAL', id_place=1, id_time_slot=2, id_competition=2
        assertThat(event.getTypeEvent()).isNotNull();
        assertThat(event.getTypeEvent().getName()).isEqualTo("TRIAL");

        assertThat(event.getPlace()).isNotNull();
        assertThat(event.getPlace().getName()).isEqualTo("Olympic Stadium");

        assertThat(event.getTimeSlot()).isNotNull();
        assertThat(event.getTimeSlot().getId()).isEqualTo(2);

        assertThat(event.getCompetition()).isNotNull();
        assertThat(event.getCompetition().getId()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByCompetitionId(1) doit retourner les épreuves de la compétition 1")
    void findByCompetitionId_returnsTrialsByCompetitionId() {
        List<Trial> trials = trialRepository.findByCompetitionId(1);

        assertThat(trials).isNotEmpty();
        assertThat(trials).allMatch(t -> t != null && t.getCompetition().getId() == 1);
    }

    @Test
    @DisplayName("findByCompetitionId(2) doit retourner les épreuves de la compétition 2")
    void findByCompetitionId_returnsTrialsForCompetition2() {
        List<Trial> trials = trialRepository.findByCompetitionId(2);

        assertThat(trials)
            .extracting(t -> t.getId())
            .contains(2);
        
        assertThat(trials).allMatch(t -> t.getCompetition().getId() == 2);
    }

    @Test
    @DisplayName("findByCompetitionId(999) doit retourner une liste vide pour une compétition inexistante")
    void findByCompetitionId_returnsEmptyListForNonExistentCompetition() {
        List<Trial> trials = trialRepository.findByCompetitionId(999);

        assertThat(trials).isEmpty();
    }
}
