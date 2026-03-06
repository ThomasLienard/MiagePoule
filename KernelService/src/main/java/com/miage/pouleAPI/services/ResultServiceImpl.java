package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.result.BulkSetResultRequest;
import com.miage.pouleAPI.dtos.result.ResultDTO;
import com.miage.pouleAPI.dtos.result.SetResultRequest;
import com.miage.pouleAPI.dtos.result.TrialResultsDTO;
import com.miage.pouleAPI.entity.IsConvenedTo;
import com.miage.pouleAPI.entity.ParticipateAt;
import com.miage.pouleAPI.entity.Trial;
import com.miage.pouleAPI.repositories.IsConvenedToRepository;
import com.miage.pouleAPI.repositories.ParticipateAtRepository;
import com.miage.pouleAPI.repositories.TrialRepository;
import com.miage.pouleAPI.services.interfaces.NotificationService;
import com.miage.pouleAPI.services.interfaces.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResultServiceImpl implements ResultService {

    private static final String ATHLETE_TYPE = "ATHLETE";
    private static final String TEAM_TYPE = "TEAM";
    private static final String TRIAL_NOT_FOUND = "Épreuve non trouvée";
    private static final String ATHLETE_NOT_REGISTERED = "L'athlète n'est pas inscrit à cette épreuve";
    private static final String TEAM_NOT_REGISTERED = "L'équipe n'est pas inscrite à cette épreuve";
    private static final String TRIAL_NOT_STARTED = "Impossible de saisir un résultat : l'épreuve n'a pas encore commencé";
    private static final String INVALID_RESULT_FORMAT = "Le résultat doit être un nombre valide (chiffres uniquement)";
    private static final String NEGATIVE_RESULT = "Le résultat ne peut pas être négatif";

    private void validateResult(String result) {
        // Permettre null pour supprimer un résultat
        if (result == null || result.trim().isEmpty()) {
            return;
        }

        // Vérifier que c'est un nombre valide
        try {
            double value = Double.parseDouble(result);
            // Vérifier que le nombre est non négatif
            if (value < 0) {
                throw new IllegalArgumentException(NEGATIVE_RESULT);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(INVALID_RESULT_FORMAT);
        }
    }

    private void checkTrialStarted(Trial trial) {
        if (trial.getTimeSlot() == null) return;
        if (LocalDateTime.now().isBefore(trial.getTimeSlot().getStart())) {
            throw new IllegalStateException(TRIAL_NOT_STARTED);
        }
    }

    /**
     * Vérifie si tous les résultats d'une épreuve sont validés
     * @param trialId l'identifiant de l'épreuve
     * @return true si tous les résultats sont validés, false sinon
     */
    private boolean hasAllResultsValidated(Integer trialId) {
        boolean isTeamTrial = participateAtRepository.hasTeamParticipation(trialId);

        if (isTeamTrial) {
            List<ParticipateAt> participations = participateAtRepository.findByTrialId(trialId);
            return participations.stream().allMatch(p -> Boolean.TRUE.equals(p.getIsValidated()));
        } else {
            List<IsConvenedTo> convocations = isConvenedToRepository.findByTrialId(trialId);
            return convocations.stream().allMatch(c -> Boolean.TRUE.equals(c.getIsValidated()));
        }
    }

    private final TrialRepository trialRepository;
    private final IsConvenedToRepository isConvenedToRepository;
    private final ParticipateAtRepository participateAtRepository;
    private final NotificationService notificationService;

    @Override
    public Optional<TrialResultsDTO> getTrialResults(Integer trialId) {
        return trialRepository.findById(trialId).map(trial -> {
            TrialResultsDTO dto = new TrialResultsDTO();
            dto.setTrialId(trial.getId());
            dto.setTrialName(trial.getName());
            if (trial.getTimeSlot() != null) {
                dto.setStartTime(trial.getTimeSlot().getStart());
                dto.setEndTime(trial.getTimeSlot().getEnd());
            }

            boolean isTeamTrial = participateAtRepository.hasTeamParticipation(trialId);
            dto.setTeamTrial(isTeamTrial);

            if (isTeamTrial) {
                List<ParticipateAt> participations = participateAtRepository.findByTrialId(trialId);
                List<ResultDTO> results = participations.stream()
                        .map(p -> new ResultDTO(
                                p.getTeam().getId(),
                                p.getTeam().getName(),
                                TEAM_TYPE,
                                p.getTeam().getCountry() != null ? p.getTeam().getCountry().getCode() : null,
                                p.getResult(),
                                Boolean.TRUE.equals(p.getIsValidated()),
                                Boolean.TRUE.equals(p.getIsForfeit())
                        ))
                        .toList();
                dto.setResults(results);
            } else {
                List<IsConvenedTo> convocations = isConvenedToRepository.findByTrialId(trialId);
                List<ResultDTO> results = convocations.stream()
                        .map(c -> new ResultDTO(
                                c.getUser().getId(),
                                c.getUser().getName() + " " + c.getUser().getLastname(),
                                ATHLETE_TYPE,
                                c.getUser().getCountry() != null ? c.getUser().getCountry().getCode() : null,
                                c.getResult(),
                                Boolean.TRUE.equals(c.getIsValidated()),
                                Boolean.TRUE.equals(c.getIsForfeit())
                        ))
                        .toList();
                dto.setResults(results);
            }

            return dto;
        });
    }

    @Override
    @Transactional
    public ResultDTO setAthleteResult(Integer trialId, Integer athleteId, String result) {
        Trial trial = trialRepository.findById(trialId)
                .orElseThrow(() -> new IllegalArgumentException(TRIAL_NOT_FOUND));
        checkTrialStarted(trial);
        validateResult(result);

        IsConvenedTo convocation = isConvenedToRepository
                .findByTrialIdAndUserId(trialId, athleteId)
                .orElseThrow(() -> new IllegalArgumentException(ATHLETE_NOT_REGISTERED));

        convocation.setResult(result);
        IsConvenedTo saved = isConvenedToRepository.save(convocation);

        return new ResultDTO(
                saved.getUser().getId(),
                saved.getUser().getName() + " " + saved.getUser().getLastname(),
                ATHLETE_TYPE,
                saved.getUser().getCountry() != null ? saved.getUser().getCountry().getCode() : null,
                saved.getResult(),
                Boolean.TRUE.equals(saved.getIsValidated()),
                Boolean.TRUE.equals(saved.getIsForfeit())
        );
    }

    @Override
    @Transactional
    public ResultDTO setTeamResult(Integer trialId, Integer teamId, String result) {
        Trial trial = trialRepository.findById(trialId)
                .orElseThrow(() -> new IllegalArgumentException(TRIAL_NOT_FOUND));
        checkTrialStarted(trial);
        validateResult(result);

        ParticipateAt participation = participateAtRepository
                .findByTrialIdAndTeamId(trialId, teamId)
                .orElseThrow(() -> new IllegalArgumentException(TEAM_NOT_REGISTERED));

        participation.setResult(result);
        ParticipateAt saved = participateAtRepository.save(participation);

        return new ResultDTO(
                saved.getTeam().getId(),
                saved.getTeam().getName(),
                TEAM_TYPE,
                saved.getTeam().getCountry() != null ? saved.getTeam().getCountry().getCode() : null,
                saved.getResult(),
                Boolean.TRUE.equals(saved.getIsValidated()),
                Boolean.TRUE.equals(saved.getIsForfeit())
        );
    }

    @Override
    @Transactional
    public List<ResultDTO> setBulkResults(Integer trialId, BulkSetResultRequest request) {
        Trial trial = trialRepository.findById(trialId)
                .orElseThrow(() -> new IllegalArgumentException(TRIAL_NOT_FOUND));
        checkTrialStarted(trial);

        List<ResultDTO> updatedResults = new ArrayList<>();

        for (SetResultRequest item : request.getResults()) {
            ResultDTO dto;
            if (ATHLETE_TYPE.equalsIgnoreCase(item.getParticipantType())) {
                dto = setAthleteResult(trialId, item.getParticipantId(), item.getResult());
            } else if (TEAM_TYPE.equalsIgnoreCase(item.getParticipantType())) {
                dto = setTeamResult(trialId, item.getParticipantId(), item.getResult());
            } else {
                throw new IllegalArgumentException("Type de participant invalide : " + item.getParticipantType());
            }
            updatedResults.add(dto);
        }

        return updatedResults;
    }

    @Override
    @Transactional
    public ResultDTO validateAthleteResult(Integer trialId, Integer athleteId) {
        Trial trial = trialRepository.findById(trialId)
                .orElseThrow(() -> new IllegalArgumentException(TRIAL_NOT_FOUND));
        checkTrialStarted(trial);

        IsConvenedTo convocation = isConvenedToRepository
                .findByTrialIdAndUserId(trialId, athleteId)
                .orElseThrow(() -> new IllegalArgumentException(ATHLETE_NOT_REGISTERED));

        convocation.setIsValidated(true);
        IsConvenedTo saved = isConvenedToRepository.save(convocation);

        // Notifie les utilisateurs si tous les résultats sont maintenant validés
        if (hasAllResultsValidated(trialId)) {
            notificationService.notifyEventResults(trial);
        }

        return new ResultDTO(
                saved.getUser().getId(),
                saved.getUser().getName() + " " + saved.getUser().getLastname(),
                ATHLETE_TYPE,
                saved.getUser().getCountry() != null ? saved.getUser().getCountry().getCode() : null,
                saved.getResult(),
                Boolean.TRUE.equals(saved.getIsValidated()),
                Boolean.TRUE.equals(saved.getIsForfeit())
        );
    }

    @Override
    @Transactional
    public ResultDTO validateTeamResult(Integer trialId, Integer teamId) {
        Trial trial = trialRepository.findById(trialId)
                .orElseThrow(() -> new IllegalArgumentException(TRIAL_NOT_FOUND));
        checkTrialStarted(trial);

        ParticipateAt participation = participateAtRepository
                .findByTrialIdAndTeamId(trialId, teamId)
                .orElseThrow(() -> new IllegalArgumentException(TEAM_NOT_REGISTERED));

        participation.setIsValidated(true);
        ParticipateAt saved = participateAtRepository.save(participation);

        // Notifie les utilisateurs si tous les résultats sont maintenant validés
        if (hasAllResultsValidated(trialId)) {
            notificationService.notifyEventResults(trial);
        }

        return new ResultDTO(
                saved.getTeam().getId(),
                saved.getTeam().getName(),
                TEAM_TYPE,
                saved.getTeam().getCountry() != null ? saved.getTeam().getCountry().getCode() : null,
                saved.getResult(),
                Boolean.TRUE.equals(saved.getIsValidated()),
                Boolean.TRUE.equals(saved.getIsForfeit())
        );
    }

    @Override
    @Transactional
    public TrialResultsDTO validateAllResults(Integer trialId) {
        Trial trial = trialRepository.findById(trialId)
                .orElseThrow(() -> new IllegalArgumentException(TRIAL_NOT_FOUND));
        checkTrialStarted(trial);

        boolean isTeamTrial = participateAtRepository.hasTeamParticipation(trialId);

        if (isTeamTrial) {
            List<ParticipateAt> participations = participateAtRepository.findByTrialId(trialId);
            participations.forEach(p -> p.setIsValidated(true));
            participateAtRepository.saveAll(participations);
        } else {
            List<IsConvenedTo> convocations = isConvenedToRepository.findByTrialId(trialId);
            convocations.forEach(c -> c.setIsValidated(true));
            isConvenedToRepository.saveAll(convocations);
        }

        // Notifie tous les utilisateurs abonnés que les résultats sont disponibles
        notificationService.notifyEventResults(trial);

        return getTrialResults(trialId)
                .orElseThrow(() -> new IllegalArgumentException(TRIAL_NOT_FOUND));
    }

    @Override
    @Transactional
    public ResultDTO invalidateAthleteResult(Integer trialId, Integer athleteId) {
        Trial trial = trialRepository.findById(trialId)
                .orElseThrow(() -> new IllegalArgumentException(TRIAL_NOT_FOUND));
        checkTrialStarted(trial);

        IsConvenedTo convocation = isConvenedToRepository
                .findByTrialIdAndUserId(trialId, athleteId)
                .orElseThrow(() -> new IllegalArgumentException(ATHLETE_NOT_REGISTERED));

        convocation.setIsValidated(false);
        IsConvenedTo saved = isConvenedToRepository.save(convocation);

        return new ResultDTO(
                saved.getUser().getId(),
                saved.getUser().getName() + " " + saved.getUser().getLastname(),
                ATHLETE_TYPE,
                saved.getUser().getCountry() != null ? saved.getUser().getCountry().getCode() : null,
                saved.getResult(),
                Boolean.TRUE.equals(saved.getIsValidated()),
                Boolean.TRUE.equals(saved.getIsForfeit())
        );
    }

    @Override
    @Transactional
    public ResultDTO invalidateTeamResult(Integer trialId, Integer teamId) {
        Trial trial = trialRepository.findById(trialId)
                .orElseThrow(() -> new IllegalArgumentException(TRIAL_NOT_FOUND));
        checkTrialStarted(trial);

        ParticipateAt participation = participateAtRepository
                .findByTrialIdAndTeamId(trialId, teamId)
                .orElseThrow(() -> new IllegalArgumentException(TEAM_NOT_REGISTERED));

        participation.setIsValidated(false);
        ParticipateAt saved = participateAtRepository.save(participation);

        return new ResultDTO(
                saved.getTeam().getId(),
                saved.getTeam().getName(),
                TEAM_TYPE,
                saved.getTeam().getCountry() != null ? saved.getTeam().getCountry().getCode() : null,
                saved.getResult(),
                Boolean.TRUE.equals(saved.getIsValidated()),
                Boolean.TRUE.equals(saved.getIsForfeit())
        );
    }
}
