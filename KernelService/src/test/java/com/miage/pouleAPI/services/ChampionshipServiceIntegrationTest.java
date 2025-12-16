package com.miage.pouleAPI.services;

import com.miage.pouleAPI.domains.ChampionshipModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChampionshipServiceIntegrationTest {

    @Autowired
    private ChampionshipService championshipService;

    @Test
    void shouldSaveAndFindChampionship() {
        ChampionshipModel model = new ChampionshipModel(
                "Champ IT",
                LocalDate.of(2026, 8, 20),
                2,
                "ChampIT",
                LocalDate.of(2026, 8, 10)
        );

        ChampionshipModel saved = championshipService.save(model);
        assertNotNull(saved.getId());

        var found = championshipService.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("ChampIT", found.get().getName());
    }

    @Test
    void shouldFindAllChampionships() {
        championshipService.save(new ChampionshipModel(
                "C1", LocalDate.of(2026, 1, 10), 5, "C1", LocalDate.of(2026, 1, 1)
        ));
        championshipService.save(new ChampionshipModel(
                "C2", LocalDate.of(2026, 2, 10), 6, "C2", LocalDate.of(2026, 2, 1)
        ));

        List<ChampionshipModel> all = championshipService.findAll();
        assertTrue(all.size() >= 2);
    }
}
