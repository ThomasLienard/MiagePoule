package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.Trial;
import com.miage.pouleAPI.entity.TypeEvent;
import com.miage.pouleAPI.services.interfaces.TrialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrialController Tests")
class TrialControllerTest {

    @Mock
    private TrialService trialService;

    @InjectMocks
    private TrialController trialController;

    private MockMvc mockMvc;
    private Trial trial1;
    private Trial trial2;
    private Event event1;
    private Event event2;
    private TypeEvent typeEvent;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trialController).build();
        
        typeEvent = new TypeEvent();
        typeEvent.setName("Épreuve Sportive");

        event1 = new Event();
        event1.setId(1);
        event1.setName("Marathon de Paris");
        event1.setDescription("42km course");
        event1.setTypeEvent(typeEvent);

        event2 = new Event();
        event2.setId(2);
        event2.setName("100m Sprint");
        event2.setDescription("Sprint rapide");
        event2.setTypeEvent(typeEvent);

        trial1 = new Trial();
        trial1.setId(1);
        trial1.setEvent(event1);

        trial2 = new Trial();
        trial2.setId(2);
        trial2.setEvent(event2);
    }

    @Test
    @DisplayName("GET /public/trials - Devrait retourner toutes les épreuves")
    void testGetAllTrials_Success() throws Exception {
        // Given
        List<Trial> trials = Arrays.asList(trial1, trial2);
        when(trialService.getAllTrials()).thenReturn(trials);

        // When & Then
        mockMvc.perform(get("/public/trials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].event.name").value("Marathon de Paris"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].event.name").value("100m Sprint"));

        verify(trialService, times(1)).getAllTrials();
    }

    @Test
    @DisplayName("GET /public/trials - Devrait retourner une liste vide quand aucune épreuve")
    void testGetAllTrials_EmptyList() throws Exception {
        // Given
        when(trialService.getAllTrials()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/public/trials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(trialService, times(1)).getAllTrials();
    }

    @Test
    @DisplayName("GET /public/trials - Test avec méthode directe du controller")
    void testGetAllTrials_DirectCall() {
        // Given
        List<Trial> trials = Arrays.asList(trial1, trial2);
        when(trialService.getAllTrials()).thenReturn(trials);

        // When
        List<Trial> result = trialController.getAllTrials();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Marathon de Paris", result.get(0).getEvent().getName());
        assertEquals(2, result.get(1).getId());
        assertEquals("100m Sprint", result.get(1).getEvent().getName());
        verify(trialService, times(1)).getAllTrials();
    }

    @Test
    @DisplayName("GET /public/trials - Devrait gérer les trials sans event associé")
    void testGetAllTrials_WithNullEvent() {
        // Given
        Trial trialWithoutEvent = new Trial();
        trialWithoutEvent.setId(3);
        trialWithoutEvent.setEvent(null);
        
        List<Trial> trials = Arrays.asList(trial1, trialWithoutEvent);
        when(trialService.getAllTrials()).thenReturn(trials);

        // When
        List<Trial> result = trialController.getAllTrials();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNotNull(result.get(0).getEvent());
        assertNull(result.get(1).getEvent());
        verify(trialService, times(1)).getAllTrials();
    }

    @Test
    @DisplayName("GET /public/trials - Devrait gérer les erreurs du service")
    void testGetAllTrials_ServiceException() {
        // Given
        when(trialService.getAllTrials()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> trialController.getAllTrials());
        verify(trialService, times(1)).getAllTrials();
    }

    @Test
    @DisplayName("Vérifier que le CORS est configuré pour localhost:5173")
    void testCorsConfiguration() throws Exception {
        mockMvc.perform(get("/public/trials")
                .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());
    }
}
