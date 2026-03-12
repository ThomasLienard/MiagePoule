package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.agenda.VolunteerTaskDTO;
import com.miage.pouleAPI.entity.*;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VolunteerAgendaServiceImplTest {

    @Mock
    private ApplicationUserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private VolunteerAgendaServiceImpl volunteerAgendaService;

    private ApplicationUser volunteer;
    private Role volunteerRole;
    private Event eventToday;
    private Event eventTomorrow;
    private Event eventYesterday;
    private Event eventNextWeek;
    private TimeSlot timeSlotToday;
    private TimeSlot timeSlotTomorrow;
    private TimeSlot timeSlotYesterday;
    private TimeSlot timeSlotNextWeek;
    private Task taskToday;
    private Task taskTomorrow;
    private Task taskYesterday;
    private Task taskNextWeek;
    private Task taskWithoutEvent;

    @BeforeEach
    void setUp() {
        // Setup role
        volunteerRole = new Role();
        volunteerRole.setRoleName("VOLONTAIRE");

        // Setup volunteer user
        volunteer = new ApplicationUser();
        volunteer.setId(1);
        volunteer.setEmail("volunteer@example.com");
        volunteer.setName("Jean");
        volunteer.setLastname("Volontaire");
        volunteer.setRole(volunteerRole);

        // Setup time slots
        timeSlotToday = new TimeSlot();
        timeSlotToday.setStart(LocalDateTime.now().withHour(10).withMinute(0));
        timeSlotToday.setEnd(LocalDateTime.now().withHour(11).withMinute(0));

        timeSlotTomorrow = new TimeSlot();
        timeSlotTomorrow.setStart(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0));
        timeSlotTomorrow.setEnd(LocalDateTime.now().plusDays(1).withHour(15).withMinute(0));

        timeSlotYesterday = new TimeSlot();
        timeSlotYesterday.setStart(LocalDateTime.now().minusDays(1).withHour(9).withMinute(0));
        timeSlotYesterday.setEnd(LocalDateTime.now().minusDays(1).withHour(10).withMinute(0));

        timeSlotNextWeek = new TimeSlot();
        timeSlotNextWeek.setStart(LocalDateTime.now().plusDays(7).withHour(10).withMinute(0));
        timeSlotNextWeek.setEnd(LocalDateTime.now().plusDays(7).withHour(11).withMinute(0));

        // Setup events
        eventToday = new Event();
        eventToday.setId(1);
        eventToday.setName("Event Today");
        eventToday.setTimeSlot(timeSlotToday);

        eventTomorrow = new Event();
        eventTomorrow.setId(2);
        eventTomorrow.setName("Event Tomorrow");
        eventTomorrow.setTimeSlot(timeSlotTomorrow);

        eventYesterday = new Event();
        eventYesterday.setId(3);
        eventYesterday.setName("Event Yesterday");
        eventYesterday.setTimeSlot(timeSlotYesterday);

        eventNextWeek = new Event();
        eventNextWeek.setId(4);
        eventNextWeek.setName("Event Next Week");
        eventNextWeek.setTimeSlot(timeSlotNextWeek);

        // Setup tasks
        taskToday = new Task();
        taskToday.setId(1);
        taskToday.setName("Prepare track");
        taskToday.setDescription("Clean the track surface");
        taskToday.setEvent(eventToday);

        taskTomorrow = new Task();
        taskTomorrow.setId(2);
        taskTomorrow.setName("Check timing system");
        taskTomorrow.setDescription("Verify all sensors");
        taskTomorrow.setEvent(eventTomorrow);

        taskYesterday = new Task();
        taskYesterday.setId(3);
        taskYesterday.setName("Setup equipment");
        taskYesterday.setDescription("Setup event equipment");
        taskYesterday.setEvent(eventYesterday);

        taskNextWeek = new Task();
        taskNextWeek.setId(4);
        taskNextWeek.setName("Future task");
        taskNextWeek.setDescription("Task for next week");
        taskNextWeek.setEvent(eventNextWeek);

        taskWithoutEvent = new Task();
        taskWithoutEvent.setId(5);
        taskWithoutEvent.setName("Task without event");
        taskWithoutEvent.setDescription("No event associated");
        taskWithoutEvent.setEvent(null);
    }

    private void mockSecurityContext() {
        when(authentication.getName()).thenReturn("volunteer@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("getCurrentVolunteerAgenda() - Should return only today's tasks")
    void testGetCurrentVolunteerAgenda_OnlyToday() {
        mockSecurityContext();
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(volunteer));
        when(taskRepository.findTasksForUser(1)).thenReturn(Arrays.asList(taskToday, taskYesterday, taskNextWeek));

        List<VolunteerTaskDTO> result = volunteerAgendaService.getCurrentVolunteerAgenda();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Prepare track");
        assertThat(result.get(0).getEvent().getEventName()).isEqualTo("Event Today");
        verify(taskRepository, times(1)).findTasksForUser(1);
    }

    @Test
    @DisplayName("getCurrentVolunteerAgenda() - Should return only tomorrow's tasks")
    void testGetCurrentVolunteerAgenda_OnlyTomorrow() {
        mockSecurityContext();
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(volunteer));
        when(taskRepository.findTasksForUser(1)).thenReturn(Arrays.asList(taskTomorrow, taskYesterday, taskNextWeek));

        List<VolunteerTaskDTO> result = volunteerAgendaService.getCurrentVolunteerAgenda();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Check timing system");
        assertThat(result.get(0).getEvent().getEventName()).isEqualTo("Event Tomorrow");
        verify(taskRepository, times(1)).findTasksForUser(1);
    }

    @Test
    @DisplayName("getCurrentVolunteerAgenda() - Should return both today and tomorrow tasks")
    void testGetCurrentVolunteerAgenda_TodayAndTomorrow() {
        mockSecurityContext();
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(volunteer));
        when(taskRepository.findTasksForUser(1)).thenReturn(Arrays.asList(taskToday, taskTomorrow, taskYesterday, taskNextWeek));

        List<VolunteerTaskDTO> result = volunteerAgendaService.getCurrentVolunteerAgenda();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Prepare track");
        assertThat(result.get(1).getName()).isEqualTo("Check timing system");
        verify(taskRepository, times(1)).findTasksForUser(1);
    }

    @Test
    @DisplayName("getCurrentVolunteerAgenda() - Should exclude past tasks")
    void testGetCurrentVolunteerAgenda_ExcludePast() {
        mockSecurityContext();
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(volunteer));
        when(taskRepository.findTasksForUser(1)).thenReturn(Arrays.asList(taskYesterday));

        List<VolunteerTaskDTO> result = volunteerAgendaService.getCurrentVolunteerAgenda();

        assertThat(result).isEmpty();
        verify(taskRepository, times(1)).findTasksForUser(1);
    }

    @Test
    @DisplayName("getCurrentVolunteerAgenda() - Should exclude tasks after tomorrow")
    void testGetCurrentVolunteerAgenda_ExcludeFuture() {
        mockSecurityContext();
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(volunteer));
        when(taskRepository.findTasksForUser(1)).thenReturn(Arrays.asList(taskNextWeek));

        List<VolunteerTaskDTO> result = volunteerAgendaService.getCurrentVolunteerAgenda();

        assertThat(result).isEmpty();
        verify(taskRepository, times(1)).findTasksForUser(1);
    }

    @Test
    @DisplayName("getCurrentVolunteerAgenda() - Should exclude tasks without event")
    void testGetCurrentVolunteerAgenda_ExcludeWithoutEvent() {
        mockSecurityContext();
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(volunteer));
        when(taskRepository.findTasksForUser(1)).thenReturn(Arrays.asList(taskToday, taskWithoutEvent));

        List<VolunteerTaskDTO> result = volunteerAgendaService.getCurrentVolunteerAgenda();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Prepare track");
        verify(taskRepository, times(1)).findTasksForUser(1);
    }

    @Test
    @DisplayName("getCurrentVolunteerAgenda() - Should exclude tasks with event but no time slot")
    void testGetCurrentVolunteerAgenda_ExcludeWithoutTimeSlot() {
        mockSecurityContext();
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(volunteer));
        
        Event eventWithoutTimeSlot = new Event();
        eventWithoutTimeSlot.setId(5);
        eventWithoutTimeSlot.setName("Event without time slot");
        eventWithoutTimeSlot.setTimeSlot(null);
        
        Task taskWithoutTimeSlot = new Task();
        taskWithoutTimeSlot.setId(6);
        taskWithoutTimeSlot.setName("Task with event but no time slot");
        taskWithoutTimeSlot.setEvent(eventWithoutTimeSlot);
        
        when(taskRepository.findTasksForUser(1)).thenReturn(Arrays.asList(taskToday, taskWithoutTimeSlot));

        List<VolunteerTaskDTO> result = volunteerAgendaService.getCurrentVolunteerAgenda();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Prepare track");
        verify(taskRepository, times(1)).findTasksForUser(1);
    }

    @Test
    @DisplayName("getCurrentVolunteerAgenda() - Should sort tasks by time slot start")
    void testGetCurrentVolunteerAgenda_Sorted() {
        mockSecurityContext();
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(volunteer));
        
        // Tomorrow task should come after today task when sorted
        when(taskRepository.findTasksForUser(1)).thenReturn(Arrays.asList(taskTomorrow, taskToday));

        List<VolunteerTaskDTO> result = volunteerAgendaService.getCurrentVolunteerAgenda();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Prepare track"); // today first
        assertThat(result.get(1).getName()).isEqualTo("Check timing system"); // tomorrow second
        verify(taskRepository, times(1)).findTasksForUser(1);
    }

    @Test
    @DisplayName("getCurrentVolunteerAgenda() - Should return empty list when no tasks")
    void testGetCurrentVolunteerAgenda_EmptyList() {
        mockSecurityContext();
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(volunteer));
        when(taskRepository.findTasksForUser(1)).thenReturn(Collections.emptyList());

        List<VolunteerTaskDTO> result = volunteerAgendaService.getCurrentVolunteerAgenda();

        assertThat(result).isEmpty();
        verify(taskRepository, times(1)).findTasksForUser(1);
    }

    @Test
    @DisplayName("getCurrentVolunteerAgenda() - Should throw exception when user not found")
    void testGetCurrentVolunteerAgenda_UserNotFound() {
        mockSecurityContext();
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerAgendaService.getCurrentVolunteerAgenda())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Utilisateur non trouve")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(userRepository, times(1)).findByEmail("volunteer@example.com");
        verify(taskRepository, never()).findTasksForUser(any());
    }

    @Test
    @DisplayName("getCurrentVolunteerAgenda() - Should throw exception when user is not volunteer")
    void testGetCurrentVolunteerAgenda_NotVolunteer() {
        mockSecurityContext();
        
        Role athleteRole = new Role();
        athleteRole.setRoleName("ATHLETE");
        
        ApplicationUser athlete = new ApplicationUser();
        athlete.setId(2);
        athlete.setEmail("volunteer@example.com");
        athlete.setRole(athleteRole);
        
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(athlete));

        assertThatThrownBy(() -> volunteerAgendaService.getCurrentVolunteerAgenda())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Acces reserve aux benevoles")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(userRepository, times(1)).findByEmail("volunteer@example.com");
        verify(taskRepository, never()).findTasksForUser(any());
    }

    @Test
    @DisplayName("getCurrentVolunteerAgenda() - Should throw exception when user has no role")
    void testGetCurrentVolunteerAgenda_NoRole() {
        mockSecurityContext();
        
        ApplicationUser userWithoutRole = new ApplicationUser();
        userWithoutRole.setId(3);
        userWithoutRole.setEmail("volunteer@example.com");
        userWithoutRole.setRole(null);
        
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(userWithoutRole));

        assertThatThrownBy(() -> volunteerAgendaService.getCurrentVolunteerAgenda())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Acces reserve aux benevoles")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(userRepository, times(1)).findByEmail("volunteer@example.com");
        verify(taskRepository, never()).findTasksForUser(any());
    }

    @Test
    @DisplayName("getCurrentVolunteerTask() - Should return task when found")
    void testGetCurrentVolunteerTask_Found() {
        mockSecurityContext();
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(volunteer));
        when(taskRepository.findTaskForUserById(1, 1)).thenReturn(Optional.of(taskToday));

        Optional<VolunteerTaskDTO> result = volunteerAgendaService.getCurrentVolunteerTask(1);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Prepare track");
        assertThat(result.get().getEvent().getEventName()).isEqualTo("Event Today");
        verify(taskRepository, times(1)).findTaskForUserById(1, 1);
    }

    @Test
    @DisplayName("getCurrentVolunteerTask() - Should return empty when task not found")
    void testGetCurrentVolunteerTask_NotFound() {
        mockSecurityContext();
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(volunteer));
        when(taskRepository.findTaskForUserById(1, 999)).thenReturn(Optional.empty());

        Optional<VolunteerTaskDTO> result = volunteerAgendaService.getCurrentVolunteerTask(999);

        assertThat(result).isEmpty();
        verify(taskRepository, times(1)).findTaskForUserById(1, 999);
    }

    @Test
    @DisplayName("getCurrentVolunteerTask() - Should throw exception when user not found")
    void testGetCurrentVolunteerTask_UserNotFound() {
        mockSecurityContext();
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerAgendaService.getCurrentVolunteerTask(1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Utilisateur non trouve")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(userRepository, times(1)).findByEmail("volunteer@example.com");
        verify(taskRepository, never()).findTaskForUserById(any(), any());
    }

    @Test
    @DisplayName("getCurrentVolunteerTask() - Should throw exception when user is not volunteer")
    void testGetCurrentVolunteerTask_NotVolunteer() {
        mockSecurityContext();
        
        Role commissaireRole = new Role();
        commissaireRole.setRoleName("COMMISSAIRE");
        
        ApplicationUser commissaire = new ApplicationUser();
        commissaire.setId(4);
        commissaire.setEmail("volunteer@example.com");
        commissaire.setRole(commissaireRole);
        
        when(userRepository.findByEmail("volunteer@example.com")).thenReturn(Optional.of(commissaire));

        assertThatThrownBy(() -> volunteerAgendaService.getCurrentVolunteerTask(1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Acces reserve aux benevoles")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(userRepository, times(1)).findByEmail("volunteer@example.com");
        verify(taskRepository, never()).findTaskForUserById(any(), any());
    }
}
