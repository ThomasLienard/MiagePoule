package com.miage.pouleAPI.adapter;

import com.miage.pouleAPI.dto.trial.TrialDetailDTO;
import com.miage.pouleAPI.dto.trial.TrialSummaryDTO;
import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.Place;
import com.miage.pouleAPI.entity.TimeSlot;
import com.miage.pouleAPI.entity.Trial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TrialAdapter Tests")
class TrialAdapterTest {

    private TrialAdapter trialAdapter;
    private Trial trial;
    private Event event;
    private Competition competition;
    private TimeSlot timeSlot;
    private Place place;

    @BeforeEach
    void setUp() {
        trialAdapter = new TrialAdapter();

        competition = new Competition();
        competition.setId(1);
        competition.setName("Championnats de France");

        timeSlot = new TimeSlot();
        timeSlot.setStart(LocalDateTime.of(2025, 6, 20, 8, 0));
        timeSlot.setEnd(LocalDateTime.of(2025, 6, 20, 14, 0));

        place = new Place();
        place.setId(2);
        place.setName("Stade Olympique");
        place.setDescription("Main stadium");
        place.setStreet("Avenue Pierre de Coubertin");
        place.setNumber("1");
        place.setCity("Paris");
        place.setZip("75012");
        place.setParking(true);
        place.setLatitude(48.8467);
        place.setLongitude(2.3775);

        event = new Event();
        event.setId(1);
        event.setName("Marathon de Paris");
        event.setDescription("42km course");
        event.setCompetition(competition);
        event.setTimeSlot(timeSlot);
        event.setPlace(place);

        trial = new Trial();
        trial.setId(1);
        trial.setEvent(event);
    }

    @Test
    @DisplayName("entityToSummaryDto() - Devrait convertir Trial en TrialSummaryDTO")
    void testEntityToSummaryDto_Success() {
        // When
        TrialSummaryDTO dto = trialAdapter.entityToSummaryDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals("Marathon de Paris", dto.getName());
        assertEquals("42km course", dto.getDescription());
    }

    @Test
    @DisplayName("entityToSummaryDto() - Devrait retourner null pour Trial null")
    void testEntityToSummaryDto_NullTrial() {
        // When
        TrialSummaryDTO dto = trialAdapter.entityToSummaryDto(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("entityToSummaryDto() - Devrait retourner null si Event est null")
    void testEntityToSummaryDto_NullEvent() {
        // Given
        trial.setEvent(null);

        // When
        TrialSummaryDTO dto = trialAdapter.entityToSummaryDto(trial);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("entityListToSummaryDtoList() - Devrait convertir liste de Trials")
    void testEntityListToSummaryDtoList_Success() {
        // Given
        Event event2 = new Event();
        event2.setId(2);
        event2.setName("100m Sprint");
        event2.setDescription("Sprint rapide");
        
        Trial trial2 = new Trial();
        trial2.setId(2);
        trial2.setEvent(event2);
        
        List<Trial> trials = Arrays.asList(trial, trial2);

        // When
        List<TrialSummaryDTO> dtos = trialAdapter.entityListToSummaryDtoList(trials);

        // Then
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("Marathon de Paris", dtos.get(0).getName());
        assertEquals("100m Sprint", dtos.get(1).getName());
    }

    @Test
    @DisplayName("entityListToSummaryDtoList() - Devrait filtrer les Trials avec Event null")
    void testEntityListToSummaryDtoList_FilterNullEvents() {
        // Given
        Trial trialWithoutEvent = new Trial();
        trialWithoutEvent.setId(2);
        trialWithoutEvent.setEvent(null);
        
        List<Trial> trials = Arrays.asList(trial, trialWithoutEvent);

        // When
        List<TrialSummaryDTO> dtos = trialAdapter.entityListToSummaryDtoList(trials);

        // Then
        assertNotNull(dtos);
        assertEquals(1, dtos.size()); // Seulement le trial avec event
        assertEquals("Marathon de Paris", dtos.get(0).getName());
    }

    @Test
    @DisplayName("entityListToSummaryDtoList() - Devrait retourner liste vide pour liste vide")
    void testEntityListToSummaryDtoList_EmptyList() {
        // When
        List<TrialSummaryDTO> dtos = trialAdapter.entityListToSummaryDtoList(Collections.emptyList());

        // Then
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    @DisplayName("entityListToSummaryDtoList() - Devrait retourner null pour liste null")
    void testEntityListToSummaryDtoList_NullList() {
        // When
        List<TrialSummaryDTO> dtos = trialAdapter.entityListToSummaryDtoList(null);

        // Then
        assertNull(dtos);
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait convertir Trial complet en TrialDetailDTO")
    void testEntityToDetailDto_Complete() {
        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals("Marathon de Paris", dto.getName());
        assertEquals("42km course", dto.getDescription());
        assertEquals("Championnats de France", dto.getCompetitionName());
        
        assertNotNull(dto.getTimeSlot());
        assertEquals(timeSlot.getStart(), dto.getTimeSlot().getStart());
        assertEquals(timeSlot.getEnd(), dto.getTimeSlot().getEnd());
        
        assertNotNull(dto.getPlace());
        assertEquals("Stade Olympique", dto.getPlace().getName());
        assertEquals("Paris", dto.getPlace().getCity());
        assertEquals(48.8467, dto.getPlace().getLatitude());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait gérer Trial sans Competition")
    void testEntityToDetailDto_WithoutCompetition() {
        // Given
        event.setCompetition(null);

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertNull(dto.getCompetitionName());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait gérer Trial sans TimeSlot")
    void testEntityToDetailDto_WithoutTimeSlot() {
        // Given
        event.setTimeSlot(null);

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertNull(dto.getTimeSlot());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait gérer Trial sans Place")
    void testEntityToDetailDto_WithoutPlace() {
        // Given
        event.setPlace(null);

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNotNull(dto);
        assertNull(dto.getPlace());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait retourner null pour Trial null")
    void testEntityToDetailDto_NullTrial() {
        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait retourner null si Event est null")
    void testEntityToDetailDto_NullEvent() {
        // Given
        trial.setEvent(null);

        // When
        TrialDetailDTO dto = trialAdapter.entityToDetailDto(trial);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("summaryDtoToEntity() - Devrait convertir TrialSummaryDTO en Trial")
    void testSummaryDtoToEntity_Success() {
        // Given
        TrialSummaryDTO dto = new TrialSummaryDTO(1, "Test Trial", "Test Description");

        // When
        Trial result = trialAdapter.summaryDtoToEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNotNull(result.getEvent());
        assertEquals("Test Trial", result.getEvent().getName());
        assertEquals("Test Description", result.getEvent().getDescription());
    }

    @Test
    @DisplayName("summaryDtoToEntity() - Devrait retourner null pour DTO null")
    void testSummaryDtoToEntity_NullDto() {
        // When
        Trial result = trialAdapter.summaryDtoToEntity(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("detailDtoToEntity() - Devrait convertir TrialDetailDTO en Trial")
    void testDetailDtoToEntity_Success() {
        // Given
        TrialDetailDTO dto = new TrialDetailDTO();
        dto.setId(1);
        dto.setName("Test Trial");
        dto.setDescription("Test Description");

        // When
        Trial result = trialAdapter.detailDtoToEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNotNull(result.getEvent());
        assertEquals("Test Trial", result.getEvent().getName());
        assertEquals("Test Description", result.getEvent().getDescription());
    }

    @Test
    @DisplayName("detailDtoToEntity() - Devrait retourner null pour DTO null")
    void testDetailDtoToEntity_NullDto() {
        // When
        Trial result = trialAdapter.detailDtoToEntity(null);

        // Then
        assertNull(result);
    }
}
