package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.dtos.participant.AthleteDTO;
import com.miage.pouleAPI.dtos.participant.ParticipantDTO;
import com.miage.pouleAPI.dtos.participant.TrialParticipantsDTO;
import com.miage.pouleAPI.dtos.participant.TrialParticipantsFullDTO;

import java.util.List;
import java.util.Optional;

public interface ParticipantService {

    Optional<AthleteDTO> getAthleteById(Integer athleteId);

    /**
     * Récupère les participants et participants potentiels d'une épreuve
     */
    Optional<TrialParticipantsDTO> getTrialParticipants(Integer trialId);
    
    /**
     * Récupère les participants et tous les participants potentiels (athlètes ET équipes)
     */
    Optional<TrialParticipantsFullDTO> getTrialParticipantsFull(Integer trialId);
    
    /**
     * Inscrit un athlète à une épreuve solo
     */
    ParticipantDTO addAthleteToTrial(Integer trialId, Integer athleteId);
    
    /**
     * Inscrit une équipe à une épreuve en équipe
     */
    ParticipantDTO addTeamToTrial(Integer trialId, Integer teamId);
    
    /**
     * Déclare un athlète forfait à une épreuve
     */
    ParticipantDTO forfeitAthlete(Integer trialId, Integer athleteId);
    
    /**
     * Déclare une équipe forfait à une épreuve
     */
    ParticipantDTO forfeitTeam(Integer trialId, Integer teamId);
    
    /**
     * Annule le forfait d'un athlète
     */
    ParticipantDTO unforfeitAthlete(Integer trialId, Integer athleteId);
    
    /**
     * Annule le forfait d'une équipe
     */
    ParticipantDTO unforfeitTeam(Integer trialId, Integer teamId);
    
    /**
     * Retire un athlète d'une épreuve
     */
    void removeAthleteFromTrial(Integer trialId, Integer athleteId);
    
    /**
     * Retire une équipe d'une épreuve
     */
    void removeTeamFromTrial(Integer trialId, Integer teamId);
    
    /**
     * Récupère les épreuves pour lesquelles le commissaire peut gérer les participants
     */
    List<TrialParticipantsDTO> getTrialsForCommissaire();
    
    /**
     * Permet à un sportif de déclarer forfait pour une épreuve
     * Vérifie que l'épreuve n'est pas encore terminée
     */
    ParticipantDTO athleteDeclareWithdrawal(Integer trialId, String athleteEmail);
}
