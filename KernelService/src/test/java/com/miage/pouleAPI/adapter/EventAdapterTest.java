package com.miage.pouleAPI.adapter;

import com.miage.pouleAPI.dto.event.EventDetailDTO;
import com.miage.pouleAPI.dto.event.EventSummaryDTO;
import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.Place;
import com.miage.pouleAPI.entity.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventAdapter Tests")
class EventAdapterTest {

    private EventAdapter eventAdapter;
    private Event event;
    private Competition competition;
    private TimeSlot timeSlot;
    private Place place;

    @BeforeEach
    void setUp() {
        eventAdapter = new EventAdapter();

        competition = new Competition();
        competition.setId(1);
        competition.setName("TechWorld 2025");

        timeSlot = new TimeSlot();
        timeSlot.setStart(LocalDateTime.of(2025, 6, 15, 9, 0));
        timeSlot.setEnd(LocalDateTime.of(2025, 6, 15, 18, 0));

        place = new Place();
        place.setId(1);
        place.setName("Convention Center");
        place.setDescription("Large venue");
        place.setStreet("Rue de la Paix");
        place.setNumber("10");
        place.setCity("Paris");
        place.setZip("75001");
        place.setParking(true);
        place.setLatitude(48.8566);
        place.setLongitude(2.3522);

        event = new Event();
        event.setId(1);
        event.setName("Tech Conference 2025");
        event.setDescription("Annual technology conference");
        event.setCompetition(competition);
        event.setTimeSlot(timeSlot);
        event.setPlace(place);
    }

    @Test
    @DisplayName("entityToSummaryDto() - Devrait convertir Event en EventSummaryDTO")
    void testEntityToSummaryDto_Success() {
        // When
        EventSummaryDTO dto = eventAdapter.entityToSummaryDto(event);

        // Then
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals("Tech Conference 2025", dto.getName());
        assertEquals("Annual technology conference", dto.getDescription());
    }

    @Test
    @DisplayName("entityToSummaryDto() - Devrait retourner null pour Event null")
    void testEntityToSummaryDto_NullEvent() {
        // When
        EventSummaryDTO dto = eventAdapter.entityToSummaryDto(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("entityListToSummaryDtoList() - Devrait convertir liste d'Events")
    void testEntityListToSummaryDtoList_Success() {
        // Given
        Event event2 = new Event();
        event2.setId(2);
        event2.setName("Music Festival");
        event2.setDescription("Summer music");
        
        List<Event> events = Arrays.asList(event, event2);

        // When
        List<EventSummaryDTO> dtos = eventAdapter.entityListToSummaryDtoList(events);

        // Then
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("Tech Conference 2025", dtos.get(0).getName());
        assertEquals("Music Festival", dtos.get(1).getName());
    }

    @Test
    @DisplayName("entityListToSummaryDtoList() - Devrait retourner liste vide pour liste vide")
    void testEntityListToSummaryDtoList_EmptyList() {
        // When
        List<EventSummaryDTO> dtos = eventAdapter.entityListToSummaryDtoList(Collections.emptyList());

        // Then
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    @DisplayName("entityListToSummaryDtoList() - Devrait retourner liste vide pour liste null")
    void testEntityListToSummaryDtoList_NullList() {
        // When
        List<EventSummaryDTO> dtos = eventAdapter.entityListToSummaryDtoList(null);

        // Then
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait convertir Event complet en EventDetailDTO")
    void testEntityToDetailDto_Complete() {
        // When
        EventDetailDTO dto = eventAdapter.entityToDetailDto(event);

        // Then
        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals("Tech Conference 2025", dto.getName());
        assertEquals("Annual technology conference", dto.getDescription());
        assertEquals("TechWorld 2025", dto.getCompetitionName());
        
        assertNotNull(dto.getTimeSlot());
        assertEquals(timeSlot.getStart(), dto.getTimeSlot().getStart());
        assertEquals(timeSlot.getEnd(), dto.getTimeSlot().getEnd());
        
        assertNotNull(dto.getPlace());
        assertEquals("Convention Center", dto.getPlace().getName());
        assertEquals("Paris", dto.getPlace().getCity());
        assertEquals(48.8566, dto.getPlace().getLatitude());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait gérer Event sans Competition")
    void testEntityToDetailDto_WithoutCompetition() {
        // Given
        event.setCompetition(null);

        // When
        EventDetailDTO dto = eventAdapter.entityToDetailDto(event);

        // Then
        assertNotNull(dto);
        assertNull(dto.getCompetitionName());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait gérer Event sans TimeSlot")
    void testEntityToDetailDto_WithoutTimeSlot() {
        // Given
        event.setTimeSlot(null);

        // When
        EventDetailDTO dto = eventAdapter.entityToDetailDto(event);

        // Then
        assertNotNull(dto);
        assertNull(dto.getTimeSlot());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait gérer Event sans Place")
    void testEntityToDetailDto_WithoutPlace() {
        // Given
        event.setPlace(null);

        // When
        EventDetailDTO dto = eventAdapter.entityToDetailDto(event);

        // Then
        assertNotNull(dto);
        assertNull(dto.getPlace());
    }

    @Test
    @DisplayName("entityToDetailDto() - Devrait retourner null pour Event null")
    void testEntityToDetailDto_NullEvent() {
        // When
        EventDetailDTO dto = eventAdapter.entityToDetailDto(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("summaryDtoToEntity() - Devrait convertir EventSummaryDTO en Event")
    void testSummaryDtoToEntity_Success() {
        // Given
        EventSummaryDTO dto = new EventSummaryDTO(1, "Test Event", "Test Description");

        // When
        Event result = eventAdapter.summaryDtoToEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Event", result.getName());
        assertEquals("Test Description", result.getDescription());
    }

    @Test
    @DisplayName("summaryDtoToEntity() - Devrait retourner null pour DTO null")
    void testSummaryDtoToEntity_NullDto() {
        // When
        Event result = eventAdapter.summaryDtoToEntity(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("detailDtoToEntity() - Devrait convertir EventDetailDTO en Event")
    void testDetailDtoToEntity_Success() {
        // Given
        EventDetailDTO dto = new EventDetailDTO();
        dto.setId(1);
        dto.setName("Test Event");
        dto.setDescription("Test Description");

        // When
        Event result = eventAdapter.detailDtoToEntity(dto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Event", result.getName());
        assertEquals("Test Description", result.getDescription());
    }

    @Test
    @DisplayName("detailDtoToEntity() - Devrait retourner null pour DTO null")
    void testDetailDtoToEntity_NullDto() {
        // When
        Event result = eventAdapter.detailDtoToEntity(null);

        // Then
        assertNull(result);
    }
}
