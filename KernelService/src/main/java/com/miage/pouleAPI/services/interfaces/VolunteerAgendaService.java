package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.dtos.agenda.VolunteerTaskDTO;

import java.util.List;
import java.util.Optional;

public interface VolunteerAgendaService {
    List<VolunteerTaskDTO> getCurrentVolunteerAgenda();
    Optional<VolunteerTaskDTO> getCurrentVolunteerTask(Integer taskId);
}
