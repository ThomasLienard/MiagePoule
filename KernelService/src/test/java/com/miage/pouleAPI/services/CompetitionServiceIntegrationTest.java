package com.miage.pouleAPI.services;

import com.miage.pouleAPI.domains.ChampionshipModel;
import com.miage.pouleAPI.domains.CompetitionModel;
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
class CompetitionServiceIntegrationTest {

    @Autowired
    private ChampionshipService championshipService;

    @Autowired
    private CompetitionService competitionService;

    @Test
    void shouldSaveAndFindCompetition() {
        ChampionshipModel champ = new ChampionshipModel(
                "Champ IT",
                LocalDate.of(2026, 5, 20),
                3,
                "ChampIT",
                LocalDate.of(2026, 5, 10)
        );
        ChampionshipModel savedChamp = championshipService.save(champ);

        CompetitionModel comp = new CompetitionModel(
                savedChamp.getId(),
                "Comp IT",
                LocalDate.of(2026, 5, 15),
                2,
                "CompIT",
                LocalDate.of(2026, 5, 14)
        );

        CompetitionModel savedComp = competitionService.save(comp);
        assertNotNull(savedComp.getId());
        assertEquals(savedChamp.getId(), savedComp.getChampionshipId());

        var found = competitionService.findById(savedComp.getId());
        assertTrue(found.isPresent());
        assertEquals("CompIT", found.get().getName());
    }

    @Test
    void shouldFindByChampionship() {
        ChampionshipModel champ = new ChampionshipModel(
                "Champ list",
                LocalDate.of(2026, 6, 30),
                7,
                "ChampList",
                LocalDate.of(2026, 6, 20)
        );
        ChampionshipModel savedChamp = championshipService.save(champ);

        competitionService.save(new CompetitionModel(
                savedChamp.getId(), "Comp1",
                LocalDate.of(2026, 6, 22), 8,
                "Comp1", LocalDate.of(2026, 6, 21)
        ));
        competitionService.save(new CompetitionModel(
                savedChamp.getId(), "Comp2",
                LocalDate.of(2026, 6, 24), 9,
                "Comp2", LocalDate.of(2026, 6, 23)
        ));

        List<CompetitionModel> comps =
                competitionService.findByChampionship(savedChamp.getId());

        assertEquals(2, comps.size());
        assertTrue(comps.stream()
                .allMatch(c -> c.getChampionshipId().equals(savedChamp.getId())));
    }
}
