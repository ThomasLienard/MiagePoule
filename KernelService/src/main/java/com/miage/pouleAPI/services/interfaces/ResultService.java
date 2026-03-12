package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.dtos.result.BulkSetResultRequest;
import com.miage.pouleAPI.dtos.result.ResultDTO;
import com.miage.pouleAPI.dtos.result.TrialResultsDTO;

import java.util.List;
import java.util.Optional;

public interface ResultService {

    /**
     * Récupère tous les résultats d'une épreuve (athletes ou équipes selon le type)
     */
    Optional<TrialResultsDTO> getTrialResults(Integer trialId);

    /**
     * Saisit ou modifie le résultat d'un athlète pour une épreuve
     */
    ResultDTO setAthleteResult(Integer trialId, Integer athleteId, Double result);

    /**
     * Saisit ou modifie le résultat d'une équipe pour une épreuve
     */
    ResultDTO setTeamResult(Integer trialId, Integer teamId, Double result);

    /**
     * Modifie plusieurs résultats en une seule opération
     */
    List<ResultDTO> setBulkResults(Integer trialId, BulkSetResultRequest request);

    /**
     * Valide le résultat d'un athlète
     */
    ResultDTO validateAthleteResult(Integer trialId, Integer athleteId);

    /**
     * Valide le résultat d'une équipe
     */
    ResultDTO validateTeamResult(Integer trialId, Integer teamId);

    /**
     * Valide tous les résultats d'une épreuve d'un seul coup
     */
    TrialResultsDTO validateAllResults(Integer trialId);

    /**
     * Invalide (dévalide) le résultat d'un athlète
     */
    ResultDTO invalidateAthleteResult(Integer trialId, Integer athleteId);

    /**
     * Invalide (dévalide) le résultat d'une équipe
     */
    ResultDTO invalidateTeamResult(Integer trialId, Integer teamId);
}
