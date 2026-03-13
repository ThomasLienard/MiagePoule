package com.miage.pouleAPI.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PointRankingStrategy Tests")
class PointRankingStrategyTest {
    
    private final PointRankingStrategy strategy = new PointRankingStrategy();
    
    // ===== Tests de configuration de la stratégie =====

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
    @DisplayName("getResultComparator() ne devrait pas retourner null")
    void testGetResultComparatorNotNull() {
        assertNotNull(strategy.getResultComparator());
    }

    // ===== Tests de comparaison de base =====
    
    @Test
    @DisplayName("getResultComparator() devrait comparer les points en ordre décroissant")
    void testGetResultComparator() {
        Comparator<Double> comparator = strategy.getResultComparator();
        
        // Le score le plus haut doit être avant le score le plus bas
        assertTrue(comparator.compare(100.0, 95.0) < 0);
        assertTrue(comparator.compare(95.0, 100.0) > 0);
        assertEquals(0, comparator.compare(100.0, 100.0));
    }
    
    @Test
    @DisplayName("getResultComparator() devrait gérer les valeurs null")
    void testGetResultComparatorWithNull() {
        Comparator<Double> comparator = strategy.getResultComparator();
        
        assertTrue(comparator.compare(null, 100.0) > 0);
        assertTrue(comparator.compare(100.0, null) < 0);
        assertEquals(0, comparator.compare(null, null));
    }

    // ===== Tests de cas limites =====

    @Test
    @DisplayName("getResultComparator() devrait gérer les valeurs négatives (ordre décroissant)")
    void testGetResultComparatorWithNegativeValues() {
        Comparator<Double> comparator = strategy.getResultComparator();

        // -5 > -10 en valeur numérique, donc -5 est meilleur rang (plus petit indice)
        assertTrue(comparator.compare(-5.0, -10.0) < 0);
        assertTrue(comparator.compare(-10.0, -5.0) > 0);
        assertEquals(0, comparator.compare(-5.0, -5.0));
    }

    @Test
    @DisplayName("getResultComparator() devrait gérer zéro comme valeur de résultat")
    void testGetResultComparatorWithZero() {
        Comparator<Double> comparator = strategy.getResultComparator();

        // 0 > -1 en points, donc 0 est meilleur (plus petit indice)
        assertTrue(comparator.compare(0.0, -1.0) < 0);
        assertTrue(comparator.compare(-1.0, 0.0) > 0);
        assertEquals(0, comparator.compare(0.0, 0.0));
    }

    @Test
    @DisplayName("getResultComparator() devrait être symétrique (propriété antisymétrique)")
    void testGetResultComparatorAntisymmetry() {
        Comparator<Double> comparator = strategy.getResultComparator();

        int cmp1 = comparator.compare(80.0, 90.0);
        int cmp2 = comparator.compare(90.0, 80.0);

        // signe opposé
        assertTrue(cmp1 > 0);
        assertTrue(cmp2 < 0);
    }

    @Test
    @DisplayName("getResultComparator() devrait être transitif")
    void testGetResultComparatorTransitivity() {
        Comparator<Double> comparator = strategy.getResultComparator();

        // 100 > 80 > 50, donc compare(100,80)<0 et compare(80,50)<0 → compare(100,50)<0
        assertTrue(comparator.compare(100.0, 80.0) < 0);
        assertTrue(comparator.compare(80.0, 50.0) < 0);
        assertTrue(comparator.compare(100.0, 50.0) < 0);
    }

    // ===== Tests de tri d'une liste =====

    @Test
    @DisplayName("getResultComparator() devrait trier une liste du meilleur au moins bon score")
    void testSortingListDescending() {
        Comparator<Double> comparator = strategy.getResultComparator();
        List<Double> scores = Arrays.asList(70.0, 95.0, 85.0, 60.0, 100.0);
        scores.sort(comparator);

        // Ordre attendu : 100, 95, 85, 70, 60
        assertEquals(100.0, scores.get(0));
        assertEquals(95.0,  scores.get(1));
        assertEquals(85.0,  scores.get(2));
        assertEquals(70.0,  scores.get(3));
        assertEquals(60.0,  scores.get(4));
    }

    @Test
    @DisplayName("getResultComparator() devrait gérer les doublons dans une liste triée")
    void testSortingListWithDuplicates() {
        Comparator<Double> comparator = strategy.getResultComparator();
        List<Double> scores = Arrays.asList(80.0, 100.0, 80.0, 90.0);
        scores.sort(comparator);

        assertEquals(100.0, scores.get(0));
        assertEquals(90.0,  scores.get(1));
        // Les deux 80 doivent être en dernière position (peu importe l'ordre entre eux)
        assertEquals(80.0, scores.get(2));
        assertEquals(80.0, scores.get(3));
    }
}
