package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.championship.ChampionshipDTO;
import com.miage.pouleAPI.services.interfaces.ChampionshipService;

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
class ChampionshipServiceIntegrationTest {

    @Autowired
    private ChampionshipService championshipService;

    @Test
    void shouldFindAllChampionshipsFromDataSql() {
        List<ChampionshipDTO> all = championshipService.findAll();

        assertNotNull(all);
        assertEquals(2, all.size());

        assertTrue(all.stream().anyMatch(c ->
                c.getId().equals(1) &&
                        "World Cup".equals(c.getName()) &&
                        "World level championship".equals(c.getDescription())
        ));

        assertTrue(all.stream().anyMatch(c ->
                c.getId().equals(2) &&
                        "National League".equals(c.getName()) &&
                        "National level championship".equals(c.getDescription())
        ));
    }

    @Test
    void shouldFindChampionshipByIdFromDataSql() {
        Optional<ChampionshipDTO> champOpt = championshipService.findById(1);

        assertTrue(champOpt.isPresent());
        ChampionshipDTO champ = champOpt.get();

        assertEquals(1, champ.getId());
        assertEquals("World Cup", champ.getName());
        assertEquals("World level championship", champ.getDescription());
    }
}