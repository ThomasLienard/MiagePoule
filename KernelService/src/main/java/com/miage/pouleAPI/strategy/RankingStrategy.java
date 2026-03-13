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

        // 2. Assign ranks in DB order (DB already sorted via findByTrialIdOrderedByResultDynamic)
        // Forfeits retain their original position; non-forfeits are ranked in order with tie support
        List<RankingDTO> rankings = new ArrayList<>();
        int currentRank = 1;
        int nonForfeitPosition = 1;
        Double previousResult = null;

        for (RankingDTO p : participants) {
            if (Boolean.TRUE.equals(p.getIsForfeit())) {
                rankings.add(new RankingDTO(
                    null,
                    p.getResult(),
                    p.getParticipantName(),
                    p.getParticipantType(),
                    p.getParticipantId(),
                    true,
                    p.getIsValidated()
                ));
            } else {
                if (previousResult == null ||
                    getResultComparator().compare(p.getResult(), previousResult) != 0) {
                    currentRank = nonForfeitPosition;
                }
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
                nonForfeitPosition++;
            }
        }

        return rankings;
    }
}
