package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.repositories.interfaces.EventRepository;
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
    @DisplayName("findAll() doit retourner les 2 events de data.sql")
    void findAll_returnsDataSqlEvents() {
        List<Event> events = eventRepository.findAll();


        assertThat(events).isNotEmpty().hasSize(2);
        assertThat(events)
            .extracting(Event::getId)
            .containsExactlyInAnyOrder(1, 2);
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
        assertThat(e.getTypeEvent().getName()).isEqualTo("TRAINING");

        assertThat(e.getPlace()).isNotNull();
        assertThat(e.getPlace().getName()).isEqualTo("Olympic Stadium");

        assertThat(e.getTimeSlot()).isNotNull();
        assertThat(e.getTimeSlot().getId()).isEqualTo(2);

        assertThat(e.getCompetition()).isNotNull();
        assertThat(e.getCompetition().getId()).isEqualTo(2);
    }
}
