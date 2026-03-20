package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.agenda.AgendaUploadItemDTO;
import com.miage.pouleAPI.dtos.agenda.TaskUploadItemDTO;
import com.miage.pouleAPI.dtos.agenda.UploadAgendaResponse;
import com.miage.pouleAPI.dtos.agenda.VolunteerProcessResult;
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
import com.miage.pouleAPI.repositories.EventRepository;
import com.miage.pouleAPI.repositories.TaskRepository;
import com.miage.pouleAPI.services.interfaces.VolunteerAgendaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class VolunteerAgendaServiceImpl implements VolunteerAgendaService {

    private static final String ROLE_VOLUNTEER = "VOLONTAIRE";

    private final ApplicationUserRepository userRepository;
    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VolunteerTaskDTO> getCurrentVolunteerAgenda() {
        ApplicationUser user = getCurrentVolunteer();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        return taskRepository.findTasksForUser(user.getId()).stream()
                .filter(task -> isTaskInAllowedPeriod(task, today, tomorrow))
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

    @Override
    @Transactional
    public UploadAgendaResponse uploadAgendas(List<AgendaUploadItemDTO> items) {
        List<VolunteerProcessResult> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (AgendaUploadItemDTO item : items) {
            try {
                int tasksCreated = processVolunteerAgenda(item);
                results.add(new VolunteerProcessResult(
                        item.volunteerEmail(), true, tasksCreated, "Agenda téléversé avec succès"));
                successCount++;
            } catch (Exception e) {
                log.warn("Échec du traitement de l'agenda pour {}: {}", item.volunteerEmail(), e.getMessage());
                results.add(new VolunteerProcessResult(
                        item.volunteerEmail(), false, 0, e.getMessage()));
                failedCount++;
            }
        }

        return new UploadAgendaResponse(items.size(), successCount, failedCount, results);
    }

    private int processVolunteerAgenda(AgendaUploadItemDTO item) {
        ApplicationUser volunteer = getVolunteerForAgendaUpload(item.volunteerEmail());
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Event> resolvedEvents = resolveTomorrowEvents(item.tasks(), tomorrow);

        // Remplacer uniquement les tâches du lendemain et conserver celles d'aujourd'hui (ou autres dates)
        Set<Task> keptTasks = keepTasksOutsideDate(volunteer.getDailyTasks(), tomorrow);

        volunteer.getDailyTasks().clear();
        volunteer.getDailyTasks().addAll(keptTasks);

        int tasksCreated = createTasksForVolunteer(volunteer, item.tasks(), resolvedEvents);

        userRepository.save(volunteer);
        return tasksCreated;
    }

    private ApplicationUser getVolunteerForAgendaUpload(String volunteerEmail) {
        ApplicationUser volunteer = userRepository.findByEmail(volunteerEmail)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Bénévole non trouvé : " + volunteerEmail));

        if (volunteer.getRole() == null || !ROLE_VOLUNTEER.equals(volunteer.getRole().getRoleName())) {
            throw new IllegalArgumentException(
                    "L'utilisateur " + volunteerEmail + " n'est pas un bénévole");
        }

        return volunteer;
    }

    private List<Event> resolveTomorrowEvents(List<TaskUploadItemDTO> tasks, LocalDate tomorrow) {
        List<Event> resolvedEvents = new ArrayList<>();
        for (TaskUploadItemDTO taskItem : tasks) {
            Event event = resolveUniqueEvent(taskItem);
            validateEventScheduledForTomorrow(event, taskItem, tomorrow);
            resolvedEvents.add(event);
        }
        return resolvedEvents;
    }

    private Event resolveUniqueEvent(TaskUploadItemDTO taskItem) {
        List<Event> events = eventRepository.findByCompetitionNameAndEventName(
                taskItem.competitionName().trim(),
                taskItem.eventName().trim()
        );

        if (events.isEmpty()) {
            throw new IllegalArgumentException(
                    "Événement non trouvé pour competition='"
                            + taskItem.competitionName()
                            + "' et event='"
                            + taskItem.eventName()
                            + "'");
        }

        if (events.size() > 1) {
            throw new IllegalArgumentException(
                    "Événement ambigu pour competition='"
                            + taskItem.competitionName()
                            + "' et event='"
                            + taskItem.eventName()
                            + "' ("
                            + events.size()
                            + " correspondances)");
        }

        return events.get(0);
    }

    private void validateEventScheduledForTomorrow(Event event, TaskUploadItemDTO taskItem, LocalDate tomorrow) {
        if (event.getTimeSlot() == null || event.getTimeSlot().getStart() == null) {
            throw new IllegalArgumentException(
                    "Événement sans créneau valide pour competition='"
                            + taskItem.competitionName()
                            + "' et event='"
                            + taskItem.eventName()
                            + "'");
        }

        LocalDate eventDate = event.getTimeSlot().getStart().toLocalDate();
        if (!eventDate.equals(tomorrow)) {
            throw new IllegalArgumentException(
                    "L'événement '"
                            + taskItem.eventName()
                            + "' (competition='"
                            + taskItem.competitionName()
                            + "') n'est pas planifié pour demain");
        }
    }

    private Set<Task> keepTasksOutsideDate(Set<Task> existingTasks, LocalDate date) {
        Set<Task> keptTasks = new HashSet<>();
        for (Task existingTask : existingTasks) {
            if (!isTaskScheduledForDate(existingTask, date)) {
                keptTasks.add(existingTask);
            }
        }
        return keptTasks;
    }

    private int createTasksForVolunteer(ApplicationUser volunteer, List<TaskUploadItemDTO> tasks, List<Event> resolvedEvents) {
        int tasksCreated = 0;
        for (int i = 0; i < tasks.size(); i++) {
            TaskUploadItemDTO taskItem = tasks.get(i);
            Event event = resolvedEvents.get(i);

            Task task = new Task();
            task.setName(taskItem.name());
            task.setDescription(taskItem.description());
            task.setEvent(event);
            task.setUsers(new HashSet<>());

            volunteer.getDailyTasks().add(taskRepository.save(task));
            tasksCreated++;
        }
        return tasksCreated;
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
        if (task == null || task.getEvent() == null) {
            return null;
        }

        TimeSlot timeSlot = task.getEvent().getTimeSlot();
        if (timeSlot == null) {
            return null;
        }

        return timeSlot.getStart();
    }

    private boolean isTaskInAllowedPeriod(Task task, LocalDate today, LocalDate tomorrow) {
        LocalDateTime taskStart = getTaskStart(task);
        if (taskStart == null) {
            return false;
        }

        LocalDate taskDate = taskStart.toLocalDate();
        return taskDate.equals(today) || taskDate.equals(tomorrow);
    }

    private boolean isTaskScheduledForDate(Task task, LocalDate date) {
        LocalDateTime taskStart = getTaskStart(task);
        return taskStart != null && taskStart.toLocalDate().equals(date);
    }

    private VolunteerTaskDTO toTaskDto(Task task) {
        VolunteerTaskEventDTO event = task.getEvent() != null 
                ? toEventDto(task.getEvent()) 
                : null;

        return new VolunteerTaskDTO(
                task.getId(),
                task.getName(),
                task.getDescription(),
                event
        );
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
