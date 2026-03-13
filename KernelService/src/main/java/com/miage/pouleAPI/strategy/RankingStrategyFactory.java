package com.miage.pouleAPI.strategy;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory pour obtenir la stratégie de ranking appropriée
 * en fonction du type de score.
 */
@Component
public class RankingStrategyFactory {
    
    private final Map<String, RankingStrategy> strategies;
    
    public RankingStrategyFactory(List<RankingStrategy> strategyList) {
        this.strategies = new HashMap<>();
        for (RankingStrategy strategy : strategyList) {
            strategies.put(strategy.getScoreType().toUpperCase(), strategy);
        }
    }
    
    /**
     * Retourne la stratégie appropriée pour un type de score donné.
     * 
     * @param scoreType Le type de score (TIME, POINTS, etc.)
     * @return La stratégie correspondante
     * @throws IllegalArgumentException si le type de score n'est pas supporté
     */
    public RankingStrategy getStrategy(String scoreType) {
        if (scoreType == null) {
            throw new IllegalArgumentException("Score type cannot be null");
        }
        
        RankingStrategy strategy = strategies.get(scoreType.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported score type: " + scoreType);
        }
        
        return strategy;
    }
    
    /**
     * Vérifie si un type de score est supporté.
     * 
     * @param scoreType Le type de score à vérifier
     * @return true si le type est supporté, false sinon
     */
    public boolean isSupported(String scoreType) {
        return scoreType != null && strategies.containsKey(scoreType.toUpperCase());
    }
}
