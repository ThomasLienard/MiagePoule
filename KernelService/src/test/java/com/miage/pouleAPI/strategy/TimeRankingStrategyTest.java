package com.miage.pouleAPI.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimeRankingStrategy Tests")
class TimeRankingStrategyTest {
    
    private final TimeRankingStrategy strategy = new TimeRankingStrategy();

    // ===== Tests de configuration de la stratégie =====
    
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
    @DisplayName("getResultComparator() ne devrait pas retourner null")
    void testGetResultComparatorNotNull() {
        assertNotNull(strategy.getResultComparator());
    }

    // ===== Tests de comparaison de base =====
    
    @Test
    @DisplayName("getResultComparator() devrait comparer les temps en ordre croissant")
    void testGetResultComparator() {
        Comparator<Double> comparator = strategy.getResultComparator();
        
        // Le temps le plus bas doit être avant le temps le plus haut
        assertTrue(comparator.compare(10.5, 12.3) < 0);
        assertTrue(comparator.compare(12.3, 10.5) > 0);
        assertEquals(0, comparator.compare(10.5, 10.5));
    }
    
    @Test
    @DisplayName("getResultComparator() devrait gérer les valeurs null")
    void testGetResultComparatorWithNull() {
        Comparator<Double> comparator = strategy.getResultComparator();
        
        assertTrue(comparator.compare(null, 10.5) > 0);
        assertTrue(comparator.compare(10.5, null) < 0);
        assertEquals(0, comparator.compare(null, null));
    }

    // ===== Tests de cas limites =====

    @Test
    @DisplayName("getResultComparator() devrait gérer des temps très proches (précision décimale)")
    void testGetResultComparatorWithVeryCloseTimes() {
        Comparator<Double> comparator = strategy.getResultComparator();

        // 9.99 < 10.00 donc 9.99 est meilleur (plus petit indice)
        assertTrue(comparator.compare(9.99, 10.00) < 0);
        assertTrue(comparator.compare(10.0, 9.99) > 0);
        assertEquals(0, comparator.compare(9.99, 9.99));
    }

    @Test
    @DisplayName("getResultComparator() devrait gérer des valeurs entières en tant que temps")
    void testGetResultComparatorWithIntegerTimes() {
        Comparator<Double> comparator = strategy.getResultComparator();

        assertTrue(comparator.compare(10.0, 11.0) < 0);
        assertTrue(comparator.compare(11.0, 10.0) > 0);
        assertEquals(0, comparator.compare(10.0, 10.0));
    }

    @Test
    @DisplayName("getResultComparator() devrait être antisymétrique")
    void testGetResultComparatorAntisymmetry() {
        Comparator<Double> comparator = strategy.getResultComparator();

        int cmp1 = comparator.compare(10.5, 12.3);
        int cmp2 = comparator.compare(12.3, 10.5);

        assertTrue(cmp1 < 0);
        assertTrue(cmp2 > 0);
    }

    @Test
    @DisplayName("getResultComparator() devrait être transitif")
    void testGetResultComparatorTransitivity() {
        Comparator<Double> comparator = strategy.getResultComparator();

        // 9.8 < 10.0 < 11.2, donc compare(9.8,10.0)<0 et compare(10.0,11.2)<0 → compare(9.8,11.2)<0
        assertTrue(comparator.compare(9.8, 10.0) < 0);
        assertTrue(comparator.compare(10.0, 11.2) < 0);
        assertTrue(comparator.compare(9.8, 11.2) < 0);
    }

    @Test
    @DisplayName("getResultComparator() devrait gérer des temps identiques comme égalité")
    void testGetResultComparatorEquality() {
        Comparator<Double> comparator = strategy.getResultComparator();

        assertEquals(0, comparator.compare(10.500, 10.500));
        assertEquals(0, comparator.compare(60.0, 60.0));
    }

    // ===== Tests de tri d'une liste =====

    @Test
    @DisplayName("getResultComparator() devrait trier une liste du meilleur au moins bon temps")
    void testSortingListAscending() {
        Comparator<Double> comparator = strategy.getResultComparator();
        List<Double> times = Arrays.asList(12.5, 9.8, 11.0, 10.3, 9.9);
        times.sort(comparator);

        // Ordre attendu : 9.8, 9.9, 10.3, 11.0, 12.5
        assertEquals(9.8,  times.get(0));
        assertEquals(9.9,  times.get(1));
        assertEquals(10.3, times.get(2));
        assertEquals(11.0, times.get(3));
        assertEquals(12.5, times.get(4));
    }

    @Test
    @DisplayName("getResultComparator() devrait gérer les doublons dans une liste triée de temps")
    void testSortingListWithDuplicates() {
        Comparator<Double> comparator = strategy.getResultComparator();
        List<Double> times = Arrays.asList(12.0, 10.5, 12.0, 11.0);
        times.sort(comparator);

        assertEquals(10.5, times.get(0));
        assertEquals(11.0, times.get(1));
        assertEquals(12.0, times.get(2));
        assertEquals(12.0, times.get(3));
    }
}
