package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.auth.jwt.JwtService;
import com.miage.pouleAPI.config.SecurityConfig;
import com.miage.pouleAPI.dtos.agenda.VolunteerTaskDTO;
import com.miage.pouleAPI.dtos.agenda.VolunteerTaskEventDTO;
import com.miage.pouleAPI.dtos.place.PlaceDTO;
import com.miage.pouleAPI.dtos.timeslot.TimeSlotDTO;
import com.miage.pouleAPI.services.interfaces.VolunteerAgendaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VolunteerAgendaController.class)
@Import(SecurityConfig.class)
class VolunteerAgendaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VolunteerAgendaService volunteerAgendaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private VolunteerTaskDTO taskDTO1;
    private VolunteerTaskDTO taskDTO2;
    private PlaceDTO placeDTO;
    private TimeSlotDTO timeSlotDTO;
    private VolunteerTaskEventDTO eventDTO;

    @BeforeEach
    void setUp() {
        // Setup place
        placeDTO = new PlaceDTO();
        placeDTO.setId(1);
        placeDTO.setName("France Stadium");
        placeDTO.setCity("Saint-Denis");
        placeDTO.setZip("93200");
        placeDTO.setStreet("Main Street");
        placeDTO.setNumber("1");
        placeDTO.setParking(true);
        placeDTO.setLatitude(48.924459);
        placeDTO.setLongitude(2.360164);

        // Setup time slot
        timeSlotDTO = new TimeSlotDTO(
                LocalDateTime.now().withHour(10).withMinute(0),
                LocalDateTime.now().withHour(11).withMinute(0)
        );

        // Setup event
        eventDTO = new VolunteerTaskEventDTO();
        eventDTO.setEventId(1);
        eventDTO.setEventName("100m Trial Heat 1");
        eventDTO.setTimeSlot(timeSlotDTO);
        eventDTO.setPlace(placeDTO);

        // Setup tasks
        taskDTO1 = new VolunteerTaskDTO();
        taskDTO1.setId(1);
        taskDTO1.setName("Prepare track");
        taskDTO1.setDescription("Ensure the track surface is clean");
        taskDTO1.setEvent(eventDTO);

        taskDTO2 = new VolunteerTaskDTO();
        taskDTO2.setId(2);
        taskDTO2.setName("Check timing system");
        taskDTO2.setDescription("Verify sensors and timing devices");
        taskDTO2.setEvent(eventDTO);
    }

    @Test
    @WithMockUser(roles = "VOLONTAIRE")
    @DisplayName("GET /volunteer/agenda - Should return agenda for volunteer")
    void testGetAgenda_Success() throws Exception {
        List<VolunteerTaskDTO> tasks = Arrays.asList(taskDTO1, taskDTO2);
        when(volunteerAgendaService.getCurrentVolunteerAgenda()).thenReturn(tasks);

        mockMvc.perform(get("/volunteer/agenda")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Prepare track"))
                .andExpect(jsonPath("$[0].description").value("Ensure the track surface is clean"))
                .andExpect(jsonPath("$[0].event.eventId").value(1))
                .andExpect(jsonPath("$[0].event.eventName").value("100m Trial Heat 1"))
                .andExpect(jsonPath("$[0].event.place.name").value("France Stadium"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Check timing system"));

        verify(volunteerAgendaService, times(1)).getCurrentVolunteerAgenda();
    }

    @Test
    @WithMockUser(roles = "VOLONTAIRE")
    @DisplayName("GET /volunteer/agenda - Should return empty list when no tasks")
    void testGetAgenda_EmptyList() throws Exception {
        when(volunteerAgendaService.getCurrentVolunteerAgenda()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/volunteer/agenda")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(volunteerAgendaService, times(1)).getCurrentVolunteerAgenda();
    }

    @Test
    @WithMockUser(roles = "ATHLETE")
    @DisplayName("GET /volunteer/agenda - Should return 403 for non-volunteer")
    void testGetAgenda_Forbidden() throws Exception {
        mockMvc.perform(get("/volunteer/agenda")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(volunteerAgendaService, never()).getCurrentVolunteerAgenda();
    }

    @Test
    @WithMockUser(roles = "VOLONTAIRE")
    @DisplayName("GET /volunteer/agenda/tasks/{taskId} - Should return task details when found")
    void testGetTaskDetails_Found() throws Exception {
        when(volunteerAgendaService.getCurrentVolunteerTask(1)).thenReturn(Optional.of(taskDTO1));

        mockMvc.perform(get("/volunteer/agenda/tasks/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Prepare track"))
                .andExpect(jsonPath("$.description").value("Ensure the track surface is clean"))
                .andExpect(jsonPath("$.event.eventId").value(1))
                .andExpect(jsonPath("$.event.eventName").value("100m Trial Heat 1"))
                .andExpect(jsonPath("$.event.timeSlot").exists())
                .andExpect(jsonPath("$.event.place.name").value("France Stadium"));

        verify(volunteerAgendaService, times(1)).getCurrentVolunteerTask(1);
    }

    @Test
    @WithMockUser(roles = "VOLONTAIRE")
    @DisplayName("GET /volunteer/agenda/tasks/{taskId} - Should return 404 when task not found")
    void testGetTaskDetails_NotFound() throws Exception {
        when(volunteerAgendaService.getCurrentVolunteerTask(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/volunteer/agenda/tasks/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(volunteerAgendaService, times(1)).getCurrentVolunteerTask(999);
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    @DisplayName("GET /volunteer/agenda/tasks/{taskId} - Should return 403 for non-volunteer")
    void testGetTaskDetails_Forbidden() throws Exception {
        mockMvc.perform(get("/volunteer/agenda/tasks/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(volunteerAgendaService, never()).getCurrentVolunteerTask(any());
    }

    @Test
    @WithMockUser(roles = "VOLONTAIRE")
    @DisplayName("GET /volunteer/agenda - Should handle task without event")
    void testGetAgenda_TaskWithoutEvent() throws Exception {
        VolunteerTaskDTO taskWithoutEvent = new VolunteerTaskDTO();
        taskWithoutEvent.setId(3);
        taskWithoutEvent.setName("Task without event");
        taskWithoutEvent.setDescription("No event associated");
        taskWithoutEvent.setEvent(null);

        List<VolunteerTaskDTO> tasks = Arrays.asList(taskWithoutEvent);
        when(volunteerAgendaService.getCurrentVolunteerAgenda()).thenReturn(tasks);

        mockMvc.perform(get("/volunteer/agenda")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].name").value("Task without event"))
                .andExpect(jsonPath("$[0].event").doesNotExist());

        verify(volunteerAgendaService, times(1)).getCurrentVolunteerAgenda();
    }

    @Test
    @WithMockUser(roles = "VOLONTAIRE")
    @DisplayName("GET /volunteer/agenda/tasks/{taskId} - Should handle task without event")
    void testGetTaskDetails_TaskWithoutEvent() throws Exception {
        VolunteerTaskDTO taskWithoutEvent = new VolunteerTaskDTO();
        taskWithoutEvent.setId(3);
        taskWithoutEvent.setName("Task without event");
        taskWithoutEvent.setDescription("No event associated");
        taskWithoutEvent.setEvent(null);

        when(volunteerAgendaService.getCurrentVolunteerTask(3)).thenReturn(Optional.of(taskWithoutEvent));

        mockMvc.perform(get("/volunteer/agenda/tasks/3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Task without event"))
                .andExpect(jsonPath("$.event").doesNotExist());

        verify(volunteerAgendaService, times(1)).getCurrentVolunteerTask(3);
    }
}
