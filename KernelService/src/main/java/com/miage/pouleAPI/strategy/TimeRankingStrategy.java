package com.miage.pouleAPI.strategy;

import org.springframework.stereotype.Component;

import java.util.Comparator;

/**
 * Stratégie de ranking basée sur le temps.
 * Pour les épreuves chronométrées, le meilleur temps est le plus bas (ordre croissant).
 */
@Component
public class TimeRankingStrategy implements RankingStrategy {
    
    @Override
    public Comparator<Double> getResultComparator() {
        return (result1, result2) -> {
            if (result1 == null && result2 == null) return 0;
            if (result1 == null) return 1;
            if (result2 == null) return -1;
            
            return result1.compareTo(result2);
            
        };
    }
    
    @Override
    public String getScoreType() {
        return "TIME";
    }
    
    @Override
    public String getSortOrder() {
        return "ASC";  // Ordre croissant pour le temps (le plus petit est le meilleur)
    }
}
