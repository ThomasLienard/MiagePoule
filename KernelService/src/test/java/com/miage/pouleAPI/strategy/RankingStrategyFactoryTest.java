package com.miage.pouleAPI.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RankingStrategyFactory Tests")
class RankingStrategyFactoryTest {
    
    private RankingStrategyFactory factory;
    
    @BeforeEach
    void setUp() {
        List<RankingStrategy> strategies = Arrays.asList(
            new TimeRankingStrategy(),
            new PointRankingStrategy()
        );
        factory = new RankingStrategyFactory(strategies);
    }

    // ===== Tests de récupération des stratégies =====
    
    @Test
    @DisplayName("getStrategy() devrait retourner TimeRankingStrategy pour TIME")
    void testGetStrategyForTime() {
        RankingStrategy strategy = factory.getStrategy("TIME");
        
        assertNotNull(strategy);
        assertInstanceOf(TimeRankingStrategy.class, strategy);
        assertEquals("TIME", strategy.getScoreType());
    }
    
    @Test
    @DisplayName("getStrategy() devrait retourner PointRankingStrategy pour POINTS")
    void testGetStrategyForPoints() {
        RankingStrategy strategy = factory.getStrategy("POINTS");
        
        assertNotNull(strategy);
        assertInstanceOf(PointRankingStrategy.class, strategy);
        assertEquals("POINTS", strategy.getScoreType());
    }
    
    @Test
    @DisplayName("getStrategy() devrait être insensible à la casse")
    void testGetStrategyCaseInsensitive() {
        RankingStrategy strategy1 = factory.getStrategy("time");
        RankingStrategy strategy2 = factory.getStrategy("Time");
        RankingStrategy strategy3 = factory.getStrategy("TIME");
        
        assertInstanceOf(TimeRankingStrategy.class, strategy1);
        assertInstanceOf(TimeRankingStrategy.class, strategy2);
        assertInstanceOf(TimeRankingStrategy.class, strategy3);
    }
    
    @Test
    @DisplayName("getStrategy() devrait lancer une exception pour un type non supporté")
    void testGetStrategyUnsupportedType() {
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getStrategy("UNSUPPORTED");
        });
    }
    
    @Test
    @DisplayName("getStrategy() devrait lancer une exception pour null")
    void testGetStrategyWithNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getStrategy(null);
        });
    }

    @Test
    @DisplayName("getStrategy() devrait retourner la même instance à chaque appel")
    void testGetStrategyReturnsSameInstance() {
        RankingStrategy s1 = factory.getStrategy("TIME");
        RankingStrategy s2 = factory.getStrategy("TIME");
        assertSame(s1, s2);
    }

    @Test
    @DisplayName("getStrategy() TIME devrait avoir un comparateur et sortOrder ASC cohérents")
    void testGetStrategyTimeCoherence() {
        RankingStrategy strategy = factory.getStrategy("TIME");

        assertEquals("ASC", strategy.getSortOrder());
        assertNotNull(strategy.getResultComparator());
        // Vérification que le comparateur trie bien dans le sens croissant (meilleur temps = plus petit)
        assertTrue(strategy.getResultComparator().compare(9.0, 10.0) < 0);
    }

    @Test
    @DisplayName("getStrategy() POINTS devrait avoir un comparateur et sortOrder DESC cohérents")
    void testGetStrategyPointsCoherence() {
        RankingStrategy strategy = factory.getStrategy("POINTS");

        assertEquals("DESC", strategy.getSortOrder());
        assertNotNull(strategy.getResultComparator());
        // Vérification que le comparateur trie bien dans le sens décroissant (meilleur score = plus grand)
        assertTrue(strategy.getResultComparator().compare(100.0, 80.0) < 0);
    }

    // ===== Tests isSupported =====
    
    @Test
    @DisplayName("isSupported() devrait retourner true pour les types supportés")
    void testIsSupported() {
        assertTrue(factory.isSupported("TIME"));
        assertTrue(factory.isSupported("POINTS"));
        assertTrue(factory.isSupported("time"));
        assertTrue(factory.isSupported("points"));
    }
    
    @Test
    @DisplayName("isSupported() devrait retourner false pour les types non supportés")
    void testIsNotSupported() {
        assertFalse(factory.isSupported("UNSUPPORTED"));
        assertFalse(factory.isSupported(null));
    }

    @Test
    @DisplayName("isSupported() devrait être insensible à la casse")
    void testIsSupportedCaseInsensitive() {
        assertTrue(factory.isSupported("time"));
        assertTrue(factory.isSupported("Time"));
        assertTrue(factory.isSupported("TIME"));
        assertTrue(factory.isSupported("points"));
        assertTrue(factory.isSupported("Points"));
        assertTrue(factory.isSupported("POINTS"));
    }

    // ===== Tests de construction de la factory =====

    @Test
    @DisplayName("Factory initialisée avec une seule stratégie ne devrait supporter que ce type")
    void testFactoryWithSingleStrategy() {
        RankingStrategyFactory singleFactory = new RankingStrategyFactory(
            Collections.singletonList(new TimeRankingStrategy())
        );

        assertTrue(singleFactory.isSupported("TIME"));
        assertFalse(singleFactory.isSupported("POINTS"));
        assertThrows(IllegalArgumentException.class, () -> singleFactory.getStrategy("POINTS"));
    }

    @Test
    @DisplayName("Factory initialisée avec une liste vide ne devrait supporter aucun type")
    void testFactoryWithEmptyStrategyList() {
        RankingStrategyFactory emptyFactory = new RankingStrategyFactory(Collections.emptyList());

        assertFalse(emptyFactory.isSupported("TIME"));
        assertFalse(emptyFactory.isSupported("POINTS"));
        assertThrows(IllegalArgumentException.class, () -> emptyFactory.getStrategy("TIME"));
    }

    @Test
    @DisplayName("getStrategy() devrait lancer IllegalArgumentException avec le bon message pour type inconnu")
    void testGetStrategyExceptionMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            factory.getStrategy("DISTANCE")
        );
        assertTrue(ex.getMessage().contains("DISTANCE"));
    }
}
