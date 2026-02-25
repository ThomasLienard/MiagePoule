package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.agenda.VolunteerTaskDTO;
import com.miage.pouleAPI.dtos.agenda.VolunteerTaskEventDTO;
import com.miage.pouleAPI.dtos.place.PlaceDTO;
import com.miage.pouleAPI.dtos.timeslot.TimeSlotDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.Place;
import com.miage.pouleAPI.entity.Task;
import com.miage.pouleAPI.entity.TimeSlot;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.TaskRepository;
import com.miage.pouleAPI.services.interfaces.VolunteerAgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VolunteerAgendaServiceImpl implements VolunteerAgendaService {

    private static final String ROLE_VOLUNTEER = "VOLONTAIRE";

    private final ApplicationUserRepository userRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VolunteerTaskDTO> getCurrentVolunteerAgenda() {
        ApplicationUser user = getCurrentVolunteer();

        return taskRepository.findTasksForUser(user.getId()).stream()
                .sorted(taskComparator())
                .map(this::toTaskDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VolunteerTaskDTO> getCurrentVolunteerTask(Integer taskId) {
        ApplicationUser user = getCurrentVolunteer();

        return taskRepository.findTaskForUserById(user.getId(), taskId)
                .map(this::toTaskDto);
    }

    private ApplicationUser getCurrentVolunteer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        ApplicationUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));

        if (user.getRole() == null || !ROLE_VOLUNTEER.equals(user.getRole().getRoleName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces reserve aux benevoles");
        }

        return user;
    }

    private Comparator<Task> taskComparator() {
        return Comparator.comparing(this::getTaskStart, Comparator.nullsLast(LocalDateTime::compareTo))
                .thenComparing(Task::getId);
    }

    private LocalDateTime getTaskStart(Task task) {
        if (task == null || task.getEvents() == null) {
            return null;
        }

        return task.getEvents().stream()
                .map(Event::getTimeSlot)
                .filter(Objects::nonNull)
                .map(TimeSlot::getStart)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    private VolunteerTaskDTO toTaskDto(Task task) {
        List<VolunteerTaskEventDTO> events = task.getEvents().stream()
                .sorted(Comparator.comparing(this::getEventStart, Comparator.nullsLast(LocalDateTime::compareTo)))
                .map(this::toEventDto)
                .toList();

        return new VolunteerTaskDTO(
                task.getId(),
                task.getName(),
                task.getDescription(),
                events
        );
    }

    private LocalDateTime getEventStart(Event event) {
        if (event == null || event.getTimeSlot() == null) {
            return null;
        }

        return event.getTimeSlot().getStart();
    }

    private VolunteerTaskEventDTO toEventDto(Event event) {
        return new VolunteerTaskEventDTO(
                event.getId(),
                event.getName(),
                toTimeSlotDto(event.getTimeSlot()),
                toPlaceDto(event.getPlace())
        );
    }

    private TimeSlotDTO toTimeSlotDto(TimeSlot timeSlot) {
        if (timeSlot == null) {
            return null;
        }
        return new TimeSlotDTO(timeSlot.getStart(), timeSlot.getEnd());
    }

    private PlaceDTO toPlaceDto(Place place) {
        if (place == null) {
            return null;
        }
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
}
