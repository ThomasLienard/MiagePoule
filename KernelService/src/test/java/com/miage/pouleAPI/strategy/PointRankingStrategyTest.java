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

    // ===== Tests de cas limites =====

    @Test
    @DisplayName("getResultComparator() devrait gérer les valeurs négatives (ordre décroissant)")
    void testGetResultComparatorWithNegativeValues() {
        Comparator<String> comparator = strategy.getResultComparator();

        // -5 > -10 en valeur numérique, donc -5 est meilleur rang (plus petit indice)
        assertTrue(comparator.compare("-5", "-10") < 0);
        assertTrue(comparator.compare("-10", "-5") > 0);
        assertEquals(0, comparator.compare("-5", "-5"));
    }

    @Test
    @DisplayName("getResultComparator() devrait gérer zéro comme valeur de résultat")
    void testGetResultComparatorWithZero() {
        Comparator<String> comparator = strategy.getResultComparator();

        // 0 > -1 en points, donc 0 est meilleur (plus petit indice)
        assertTrue(comparator.compare("0", "-1") < 0);
        assertTrue(comparator.compare("-1", "0") > 0);
        assertEquals(0, comparator.compare("0", "0"));
    }

    @Test
    @DisplayName("getResultComparator() devrait être symétrique (propriété antisymétrique)")
    void testGetResultComparatorAntisymmetry() {
        Comparator<String> comparator = strategy.getResultComparator();

        int cmp1 = comparator.compare("80", "90");
        int cmp2 = comparator.compare("90", "80");

        // signe opposé
        assertTrue(cmp1 > 0);
        assertTrue(cmp2 < 0);
    }

    @Test
    @DisplayName("getResultComparator() devrait être transitif")
    void testGetResultComparatorTransitivity() {
        Comparator<String> comparator = strategy.getResultComparator();

        // 100 > 80 > 50, donc compare(100,80)<0 et compare(80,50)<0 → compare(100,50)<0
        assertTrue(comparator.compare("100", "80") < 0);
        assertTrue(comparator.compare("80", "50") < 0);
        assertTrue(comparator.compare("100", "50") < 0);
    }

    // ===== Tests de tri d'une liste =====

    @Test
    @DisplayName("getResultComparator() devrait trier une liste du meilleur au moins bon score")
    void testSortingListDescending() {
        Comparator<String> comparator = strategy.getResultComparator();
        List<String> scores = Arrays.asList("70", "95", "85", "60", "100");
        scores.sort(comparator);

        // Ordre attendu : 100, 95, 85, 70, 60
        assertEquals("100", scores.get(0));
        assertEquals("95",  scores.get(1));
        assertEquals("85",  scores.get(2));
        assertEquals("70",  scores.get(3));
        assertEquals("60",  scores.get(4));
    }

    @Test
    @DisplayName("getResultComparator() devrait gérer les doublons dans une liste triée")
    void testSortingListWithDuplicates() {
        Comparator<String> comparator = strategy.getResultComparator();
        List<String> scores = Arrays.asList("80", "100", "80", "90");
        scores.sort(comparator);

        assertEquals("100", scores.get(0));
        assertEquals("90",  scores.get(1));
        // Les deux 80 doivent être en dernière position (peu importe l'ordre entre eux)
        assertEquals("80", scores.get(2));
        assertEquals("80", scores.get(3));
    }
}
