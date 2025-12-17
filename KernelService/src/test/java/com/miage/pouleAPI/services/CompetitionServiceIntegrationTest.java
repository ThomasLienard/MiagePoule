package com.miage.pouleAPI.services;

import com.miage.pouleAPI.domains.CompetitionModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CompetitionServiceIntegrationTest {

    @Autowired
    private CompetitionService competitionService;

    @Test
    void shouldFindCompetitionsByChampionshipFromDataSql() {
        List<CompetitionModel> comps = competitionService.findByChampionship(1);

        assertNotNull(comps);
        assertEquals(2, comps.size());

        assertTrue(comps.stream().anyMatch(c ->
                c.getId().equals(1) &&
                        c.getChampionshipId().equals(1) &&
                        "100m Sprint".equals(c.getName()) &&
                        "Short distance run".equals(c.getDescription())
        ));

        assertTrue(comps.stream().anyMatch(c ->
                c.getId().equals(2) &&
                        c.getChampionshipId().equals(1) &&
                        "Marathon".equals(c.getName()) &&
                        "Long distance run".equals(c.getDescription())
        ));
    }

    @Test
    void shouldFindCompetitionByIdFromDataSql() {
        Optional<CompetitionModel> compOpt = competitionService.findById(2);

        assertTrue(compOpt.isPresent());
        CompetitionModel comp = compOpt.get();

        assertEquals(2, comp.getId());
        assertEquals(1, comp.getChampionshipId());
        assertEquals("Marathon", comp.getName());
        assertEquals("Long distance run", comp.getDescription());
    }
}
