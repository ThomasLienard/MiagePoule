package com.miage.pouleAPI.strategy;

import java.util.Comparator;

/**
 * Interface définissant la stratégie de calcul de ranking
 * selon le type de score (TIME, POINTS, etc.)
 */
public interface RankingStrategy {
    
    /**
     * Retourne le comparateur approprié pour trier les résultats
     * @return Comparateur de String représentant les résultats
     */
    Comparator<String> getResultComparator();
    
    /**
     * Retourne le nom du type de score géré par cette stratégie
     * @return Le nom du type de score (TIME, POINTS, etc.)
     */
    String getScoreType();
    
    /**
     * Retourne l'ordre SQL à utiliser pour trier les résultats (ASC ou DESC)
     * @return "ASC" ou "DESC"
     */
    String getSortOrder();
}
