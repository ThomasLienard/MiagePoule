package com.miage.pouleAPI.adapters;


import com.miage.pouleAPI.dtos.place.PlaceDTO;
import com.miage.pouleAPI.dtos.ranking.RankingDTO;
import com.miage.pouleAPI.dtos.timeslot.TimeSlotDTO;
import com.miage.pouleAPI.dtos.trial.SoloParticipantDTO;
import com.miage.pouleAPI.dtos.trial.TeamParticipantDTO;
import com.miage.pouleAPI.dtos.trial.TrialDetailDTO;
import com.miage.pouleAPI.dtos.trial.TrialSummaryDTO;
import com.miage.pouleAPI.entity.*;
import com.miage.pouleAPI.repositories.IsConvenedToRepository;
import com.miage.pouleAPI.repositories.ParticipateAtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class TrialAdapter {
    
    private ParticipateAtRepository participateAtRepository;
    private IsConvenedToRepository isConvenedToRepository;
    
    @Autowired
    public TrialAdapter(ParticipateAtRepository participateAtRepository, IsConvenedToRepository isConvenedToRepository) {
        this.participateAtRepository = participateAtRepository;
        this.isConvenedToRepository = isConvenedToRepository;
    }

    // ===== Conversions Entity -> DTO =====
    
    public TrialSummaryDTO entityToSummaryDto(Trial trial) {
        if (trial == null) return null;
        
        return new TrialSummaryDTO(
            trial.getId(),
            trial.getId(),  // Trial's ID is also the Event's ID (JOINED inheritance)
            trial.getName(),
            trial.getDescription(),
            false  // Par défaut, pas de forfait (cette méthode est utilisée pour les listes génériques)
        );
    }
    
    public List<TrialSummaryDTO> entityListToSummaryDtoList(List<Trial> trials) {
    if (trials == null) return new ArrayList<>();
    return trials.stream()
        .map(this::entityToSummaryDto)
        .filter(Objects::nonNull)  // ← Ajouter ce filtre
        .toList();
}

    
    public TrialDetailDTO entityToDetailDto(Trial trial) {
        if (trial == null) return null;
        
        TrialDetailDTO dto = new TrialDetailDTO();
        dto.setId(trial.getId());
        dto.setName(trial.getName());
        dto.setDescription(trial.getDescription());
        
        // Competition name
        if (trial.getCompetition() != null) {
            dto.setCompetitionName(trial.getCompetition().getName());
        }
        
        // TimeSlot
        if (trial.getTimeSlot() != null) {
            dto.setTimeSlot(timeSlotToDto(trial.getTimeSlot()));
        }
        
        // Place
        if (trial.getPlace() != null) {
            dto.setPlace(placeToDto(trial.getPlace()));
        }
        
        // Rankings from ParticipateAt and IsConvenedTo
        dto.setRankings(buildRankings(trial.getId()));
        
        // Participants (solo or teams)
        List<ParticipateAt> teamParticipations = participateAtRepository.findByTrialId(trial.getId());
        List<IsConvenedTo> soloParticipations = isConvenedToRepository.findByTrialId(trial.getId());
        
        if (!teamParticipations.isEmpty()) {
            dto.setTeamEvent(true);
            dto.setTeamParticipants(buildTeamParticipants(teamParticipations));
            dto.setSoloParticipants(new ArrayList<>());
        } else {
            dto.setTeamEvent(false);
            dto.setSoloParticipants(buildSoloParticipants(soloParticipations));
            dto.setTeamParticipants(new ArrayList<>());
        }
        
        return dto;
    }
    
    // ===== Conversions DTO -> Entity =====
    
    public Trial summaryDtoToEntity(TrialSummaryDTO dto) {
        if (dto == null) return null;
        
        Trial trial = new Trial();
        trial.setId(dto.getId());
        trial.setName(dto.getName());
        trial.setDescription(dto.getDescription());
        
        return trial;
    }
    
    public Trial detailDtoToEntity(TrialDetailDTO dto) {
        if (dto == null) return null;
        
        Trial trial = new Trial();
        trial.setId(dto.getId());
        trial.setName(dto.getName());
        trial.setDescription(dto.getDescription());
        
        if (dto.getTimeSlot() != null) {
            trial.setTimeSlot(dtoToTimeSlot(dto.getTimeSlot()));
        }
        
        if (dto.getPlace() != null) {
            trial.setPlace(dtoToPlace(dto.getPlace()));
        }
        
        return trial;
    }
    
    // ===== Méthodes privées pour les sous-objets =====
    
    private TimeSlotDTO timeSlotToDto(TimeSlot timeSlot) {
        if (timeSlot == null) return null;
        
        return new TimeSlotDTO(
            timeSlot.getStart(),
            timeSlot.getEnd()
        );
    }
    
    private TimeSlot dtoToTimeSlot(TimeSlotDTO dto) {
        if (dto == null) return null;
        
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStart(dto.getStart());
        timeSlot.setEnd(dto.getEnd());
        
        return timeSlot;
    }
    
    private PlaceDTO placeToDto(Place place) {
        if (place == null) return null;
        
        PlaceDTO dto = new PlaceDTO();
        dto.setId(place.getId());
        dto.setName(place.getName());
        dto.setDescription(place.getDescription());
        dto.setStreet(place.getStreet());
        dto.setNumber(place.getNumber());
        dto.setCity(place.getCity());
        dto.setZip(place.getZip());
        dto.setParking(place.getParking());
        dto.setLatitude(place.getLatitude());
        dto.setLongitude(place.getLongitude());
        
        return dto;
    }
    
    private Place dtoToPlace(PlaceDTO dto) {
        if (dto == null) return null;
        
        Place place = new Place();
        place.setId(dto.getId());
        place.setName(dto.getName());
        place.setDescription(dto.getDescription());
        place.setStreet(dto.getStreet());
        place.setNumber(dto.getNumber());
        place.setCity(dto.getCity());
        place.setZip(dto.getZip());
        place.setParking(dto.getParking());
        place.setLatitude(dto.getLatitude());
        place.setLongitude(dto.getLongitude());
        
        return place;
    }
    
    // ===== Method to build rankings from ParticipateAt and IsConvenedTo =====
    
    private List<RankingDTO> buildRankings(Integer trialId) {
        List<RankingDTO> rankings = new ArrayList<>();
        
        // Get team results (ParticipateAt)
        List<ParticipateAt> teamResults = participateAtRepository.findByTrialIdOrderedByResult(trialId);
        int teamRank = 1;
        for (ParticipateAt participation : teamResults) {
            if (participation.getTeam() != null && (participation.getResult() != null || participation.getIsForfeit())) {
                RankingDTO ranking = new RankingDTO();
                if (Boolean.FALSE.equals(participation.getIsForfeit())) {
                    ranking.setRank(teamRank);
                    teamRank++;
                }
                ranking.setResult(participation.getResult());
                ranking.setParticipantName(participation.getTeam().getName());
                ranking.setParticipantType("TEAM");
                ranking.setParticipantId(participation.getTeam().getId());
                ranking.setIsForfeit(participation.getIsForfeit());
                rankings.add(ranking);
            }
        }
        
        // Get athlete results (IsConvenedTo)
        List<IsConvenedTo> athleteResults = isConvenedToRepository.findByTrialIdOrderedByResult(trialId);
        int athleteRank = 1;
        for (IsConvenedTo convening : athleteResults) {
            if (convening.getUser() != null && (convening.getResult() != null || convening.getIsForfeit())) {
                RankingDTO ranking = new RankingDTO();
                if (Boolean.FALSE.equals(convening.getIsForfeit())) {
                    ranking.setRank(athleteRank);
                    athleteRank++;
                }
                ranking.setResult(convening.getResult());
                ranking.setParticipantName(convening.getUser().getName() + " " + convening.getUser().getLastname());
                ranking.setParticipantType("ATHLETE");
                ranking.setParticipantId(convening.getUser().getId());
                ranking.setIsForfeit(convening.getIsForfeit());
                rankings.add(ranking);
            }
        }
        
        return rankings;
    }

    
    private List<SoloParticipantDTO> buildSoloParticipants(List<IsConvenedTo> soloParticipations) {
        return soloParticipations.stream()
                .map(convening -> {
                    if (convening.getUser() != null) {
                        SoloParticipantDTO participant = new SoloParticipantDTO();
                        participant.setId(convening.getUser().getId());
                        participant.setFirstName(convening.getUser().getName());
                        participant.setLastName(convening.getUser().getLastname());
                        participant.setFullName(convening.getUser().getName() + " " + convening.getUser().getLastname());
                        return participant;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
    
    private List<TeamParticipantDTO> buildTeamParticipants(List<ParticipateAt> teamParticipations) {
        return teamParticipations.stream()
                .map(participation -> {
                    if (participation.getTeam() != null) {
                        TeamParticipantDTO teamDto = new TeamParticipantDTO();
                        teamDto.setId(participation.getTeam().getId());
                        teamDto.setName(participation.getTeam().getName());
                        if (participation.getTeam().getCountry() != null) {
                            teamDto.setCountry(participation.getTeam().getCountry().getCode());
                        }
                        // Build team members list
                        List<SoloParticipantDTO> members = participation.getTeam().getUsers().stream()
                                .map(user -> {
                                    SoloParticipantDTO member = new SoloParticipantDTO();
                                    member.setId(user.getId());
                                    member.setFirstName(user.getName());
                                    member.setLastName(user.getLastname());
                                    member.setFullName(user.getName() + " " + user.getLastname());
                                    return member;
                                })
                                .toList();
                        teamDto.setMembers(members);
                        return teamDto;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
