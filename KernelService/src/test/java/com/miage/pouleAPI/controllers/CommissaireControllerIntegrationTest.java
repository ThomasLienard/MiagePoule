package com.miage.pouleAPI.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miage.pouleAPI.dtos.event.CancelEventRequestDTO;
import com.miage.pouleAPI.dtos.event.UpdateEventRequestDTO;
import com.miage.pouleAPI.dtos.place.PlaceDTO;
import com.miage.pouleAPI.dtos.timeslot.TimeSlotDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommissaireControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "commissaire@test.com", roles = {"COMMISSAIRE"})
    void shouldAcceptValidEventRequest() throws Exception {
        Integer eventId = 1;

        UpdateEventRequestDTO request = new UpdateEventRequestDTO();
        request.setName("Nom existant");
        request.setCompetitionId(1);
        request.setTimeSlot(new TimeSlotDTO(
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(5).plusHours(1)
        ));

        PlaceDTO place = new PlaceDTO();
        place.setName("Lieu");
        place.setCity("Ville");
        place.setStreet("Rue");
        place.setParking(false);
        request.setPlace(place);

        mockMvc.perform(put("/commissaire/events/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    @WithMockUser(username = "commissaire@test.com", roles = {"COMMISSAIRE"})
    void shouldReturnActualDateWhenMissing() throws Exception {
        Integer eventId = 2;
        UpdateEventRequestDTO request = new UpdateEventRequestDTO();
        request.setName("Nom existant");
        request.setCompetitionId(1);

        request.setTimeSlot(new TimeSlotDTO(null, LocalDateTime.now().plusDays(5)));

        PlaceDTO place = new PlaceDTO();
        place.setName("Lieu");
        place.setCity("Ville");
        place.setStreet("Rue");
        place.setParking(true);
        request.setPlace(place);

        mockMvc.perform(put("/commissaire/events/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void update_shouldAllowCommissaireToUpdateDatesOnly() throws Exception {
        Integer eventId = 1;
        UpdateEventRequestDTO request = new UpdateEventRequestDTO();
        request.setName("Nom");
        request.setCompetitionId(1);
        request.setTimeSlot(new TimeSlotDTO(LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(10).plusHours(2)));

        PlaceDTO place = new PlaceDTO();
        place.setName("Lieu");
        place.setCity("Ville");
        place.setStreet("Rue");
        place.setParking(false);
        request.setPlace(place);

        mockMvc.perform(put("/commissaire/events/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }
    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void shouldForbiddenWhenUserIsNotAdmin() throws Exception {
        UpdateEventRequestDTO request = new UpdateEventRequestDTO();
        request.setTimeSlot(new TimeSlotDTO(null,
                LocalDateTime.now().plusDays(5).plusHours(1)));

        mockMvc.perform(put("/commissaire/events/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldForbiddenWhenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/commissaire/events/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void update_shouldReturn403_whenAccessingAdminRoute() throws Exception {
        mockMvc.perform(put("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COMMISSAIRE")
    void cancelEvent_shouldUpdateStatusAndReason() throws Exception {
        Integer eventId = 1;
        CancelEventRequestDTO cancelReq = new CancelEventRequestDTO("Pluie torrentielle");

        mockMvc.perform(patch("/commissaire/events/" + eventId + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isNoContent());
    }
}
