package com.miage.pouleAPI.strategy;

import org.springframework.stereotype.Component;

import java.util.Comparator;

/**
 * Stratégie de ranking basée sur les points.
 * Pour les épreuves à points, le meilleur score est le plus élevé (ordre décroissant).
 */
@Component
public class PointRankingStrategy implements RankingStrategy {
    
    @Override
    public Comparator<String> getResultComparator() {
        return (result1, result2) -> {
            if (result1 == null && result2 == null) return 0;
            if (result1 == null) return 1;
            if (result2 == null) return -1;
            
            try {
                Double points1 = Double.parseDouble(result1);
                Double points2 = Double.parseDouble(result2);
                // Points le plus élevé = meilleur classement (ordre inversé)
                return points2.compareTo(points1);
            } catch (NumberFormatException e) {
                // Si la conversion échoue, on compare en tant que String (ordre inversé)
                return result2.compareTo(result1);
            }
        };
    }
    
    @Override
    public String getScoreType() {
        return "POINTS";
    }
    
    @Override
    public String getSortOrder() {
        return "DESC";  // Ordre décroissant pour les points (le plus grand est le meilleur)
    }
}
