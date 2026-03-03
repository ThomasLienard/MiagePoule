package com.miage.pouleAPI.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
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
}
