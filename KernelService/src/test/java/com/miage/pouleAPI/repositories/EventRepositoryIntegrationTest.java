package com.miage.pouleAPI.repositories;

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
@DisplayName("Intégration EventRepository avec data.sql de test")
class EventRepositoryIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    @DisplayName("findAll() doit retourner les 5 events de data.sql")
    void findAll_returnsDataSqlEvents() {
        List<Event> events = eventRepository.findAll();


        assertThat(events).isNotEmpty().hasSize(5);
        assertThat(events)
            .extracting(Event::getId)
            .containsExactlyInAnyOrder(1, 2, 3, 4, 5);
    }

    @Test
    @DisplayName("findById(2) doit retourner 'Final Sprint Race' avec ses relations")
    void findById_returnsEventWithRelations() {
        Optional<Event> opt = eventRepository.findById(2);

        assertThat(opt).isPresent();
        Event e = opt.get();

        assertThat(e.getId()).isEqualTo(2);
        assertThat(e.getName()).isEqualTo("Final Sprint Race");
        assertThat(e.getDescription()).isEqualTo("Official competition");

        assertThat(e.getTypeEvent()).isNotNull();
        assertThat(e.getTypeEvent().getName()).isEqualTo("TRIAL");

        assertThat(e.getPlace()).isNotNull();
        assertThat(e.getPlace().getName()).isEqualTo("Olympic Stadium");

        assertThat(e.getTimeSlot()).isNotNull();
        assertThat(e.getTimeSlot().getId()).isEqualTo(2);

        assertThat(e.getCompetition()).isNotNull();
        assertThat(e.getCompetition().getId()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByCompetitionId(1) doit retourner les événements de la compétition 1")
    void findByCompetitionId_returnsEventsByCompetitionId() {
        List<Event> events = eventRepository.findByCompetitionId(1);

        assertThat(events).isNotEmpty();
        assertThat(events).allMatch(e -> e.getCompetition() != null && e.getCompetition().getId() == 1);
    }

    @Test
    @DisplayName("findByCompetitionId(2) doit retourner les événements de la compétition 2")
    void findByCompetitionId_returnsEventsForCompetition2() {
        List<Event> events = eventRepository.findByCompetitionId(2);

        assertThat(events)
            .extracting(Event::getId)
            .contains(2);
        
        assertThat(events).allMatch(e -> e.getCompetition().getId() == 2);
    }

    @Test
    @DisplayName("findByCompetitionId(999) doit retourner une liste vide pour une compétition inexistante")
    void findByCompetitionId_returnsEmptyListForNonExistentCompetition() {
        List<Event> events = eventRepository.findByCompetitionId(999);

        assertThat(events).isEmpty();
    }
}
