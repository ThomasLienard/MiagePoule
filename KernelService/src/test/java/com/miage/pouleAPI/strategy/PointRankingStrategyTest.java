package com.miage.pouleAPI.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PointRankingStrategy Tests")
class PointRankingStrategyTest {
    
    private final PointRankingStrategy strategy = new PointRankingStrategy();
    
    @Test
    @DisplayName("getScoreType() devrait retourner POINTS")
    void testGetScoreType() {
        assertEquals("POINTS", strategy.getScoreType());
    }
    
    @Test
    @DisplayName("getSortOrder() devrait retourner DESC")
    void testGetSortOrder() {
        assertEquals("DESC", strategy.getSortOrder());
    }
    
    @Test
    @DisplayName("getResultComparator() devrait comparer les points en ordre décroissant")
    void testGetResultComparator() {
        Comparator<String> comparator = strategy.getResultComparator();
        
        // Le score le plus haut doit être avant le score le plus bas
        assertTrue(comparator.compare("100", "95") < 0);
        assertTrue(comparator.compare("95", "100") > 0);
        assertEquals(0, comparator.compare("100", "100"));
    }
    
    @Test
    @DisplayName("getResultComparator() devrait gérer les valeurs null")
    void testGetResultComparatorWithNull() {
        Comparator<String> comparator = strategy.getResultComparator();
        
        assertTrue(comparator.compare(null, "100") > 0);
        assertTrue(comparator.compare("100", null) < 0);
        assertEquals(0, comparator.compare(null, null));
    }
    
    @Test
    @DisplayName("getResultComparator() devrait gérer les valeurs non numériques")
    void testGetResultComparatorWithNonNumeric() {
        Comparator<String> comparator = strategy.getResultComparator();
        
        // Doit comparer en tant que String (ordre inversé) si la conversion échoue
        assertTrue(comparator.compare("abc", "def") > 0);
    }
    
    @Test
    @DisplayName("getResultComparator() devrait gérer les valeurs décimales")
    void testGetResultComparatorWithDecimals() {
        Comparator<String> comparator = strategy.getResultComparator();
        
        assertTrue(comparator.compare("95.5", "95.2") < 0);
        assertTrue(comparator.compare("95.2", "95.5") > 0);
    }
}
