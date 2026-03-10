package com.miage.pouleAPI.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.miage.pouleAPI.dtos.ranking.RankingDTO;

/**
 * Interface définissant la stratégie de calcul de ranking
 * selon le type de score (TIME, POINTS, etc.)
 */
public interface RankingStrategy {
    
    /**
     * Retourne le comparateur approprié pour trier les résultats
     * @return Comparateur de Double représentant les résultats
     */
    Comparator<Double> getResultComparator();
    
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

    default List<RankingDTO> calculateRankings(List<RankingDTO> participants) {
        // 1. Validation
        List<RankingDTO> nonForfeit = participants.stream()
            .filter(p -> !Boolean.TRUE.equals(p.getIsForfeit()))
            .toList();
        boolean allValidated = !nonForfeit.isEmpty()
            && nonForfeit.stream().allMatch(p -> 
                Boolean.TRUE.equals(p.getIsValidated()) && p.getResult() != null);
        if (!allValidated) return List.of();

        // 2. Tri + ex-aequo
        List<RankingDTO> sorted = nonForfeit.stream()
            .sorted((p1, p2) -> getResultComparator().compare(p1.getResult(), p2.getResult()))
            .toList();

        // 3. Recalcul des rangs
        List<RankingDTO> rankings = new ArrayList<>();
        int currentRank = 1;
        int position = 1;
        Double previousResult = null;

        for (RankingDTO p : sorted) {
            if (previousResult == null || 
                getResultComparator().compare(p.getResult(), previousResult) != 0) {
                currentRank = position;
            }
            // Créer nouveau DTO avec bon rang
            rankings.add(new RankingDTO(
                currentRank,
                p.getResult(),
                p.getParticipantName(),
                p.getParticipantType(),
                p.getParticipantId(),
                false,
                true
            ));
            previousResult = p.getResult();
            position++;
        }

        // 4. Forfaits 
        participants.stream()
            .filter(RankingDTO::getIsForfeit)
            .forEach(rankings::add);

        return rankings;
    }
}
