package com.miage.pouleAPI.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimeRankingStrategy Tests")
class TimeRankingStrategyTest {
    
    private final TimeRankingStrategy strategy = new TimeRankingStrategy();
    
    @Test
    @DisplayName("getScoreType() devrait retourner TIME")
    void testGetScoreType() {
        assertEquals("TIME", strategy.getScoreType());
    }
    
    @Test
    @DisplayName("getSortOrder() devrait retourner ASC")
    void testGetSortOrder() {
        assertEquals("ASC", strategy.getSortOrder());
    }
    
    @Test
    @DisplayName("getResultComparator() devrait comparer les temps en ordre croissant")
    void testGetResultComparator() {
        Comparator<String> comparator = strategy.getResultComparator();
        
        // Le temps le plus bas doit être avant le temps le plus haut
        assertTrue(comparator.compare("10.5", "12.3") < 0);
        assertTrue(comparator.compare("12.3", "10.5") > 0);
        assertEquals(0, comparator.compare("10.5", "10.5"));
    }
    
    @Test
    @DisplayName("getResultComparator() devrait gérer les valeurs null")
    void testGetResultComparatorWithNull() {
        Comparator<String> comparator = strategy.getResultComparator();
        
        assertTrue(comparator.compare(null, "10.5") > 0);
        assertTrue(comparator.compare("10.5", null) < 0);
        assertEquals(0, comparator.compare(null, null));
    }
    
    @Test
    @DisplayName("getResultComparator() devrait gérer les valeurs non numériques")
    void testGetResultComparatorWithNonNumeric() {
        Comparator<String> comparator = strategy.getResultComparator();
        
        // Doit comparer en tant que String si la conversion en double échoue
        assertTrue(comparator.compare("abc", "def") < 0);
    }
}
