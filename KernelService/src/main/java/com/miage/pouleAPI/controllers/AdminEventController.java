package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.event.CreateEventRequestDTO;
import com.miage.pouleAPI.dtos.event.EventDetailDTO;
import com.miage.pouleAPI.dtos.event.UpdateEventRequestDTO;
import com.miage.pouleAPI.services.interfaces.AdminEventService;
import com.miage.pouleAPI.services.interfaces.CompetitionService;
import com.miage.pouleAPI.services.interfaces.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final AdminEventService adminEventService;
    private final EventService eventService;
    private final CompetitionService competitionService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateEventRequestDTO request) {
        adminEventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Integer id,
                                       @RequestBody @Valid UpdateEventRequestDTO request) {

        request.setId(id);

        var existing = eventService.getEventById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        checkUpdateRequest(request, existing);

        adminEventService.updateEvent(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    private void checkUpdateRequest(UpdateEventRequestDTO request, EventDetailDTO existing) {
        checkName(request, existing);
        checkDescription(request, existing);
        checkTimeSlot(request, existing);
        checkCompetitionId(request, existing);
        checkPlace(request, existing);
        checkRankings(request, existing);
    }

    private void checkName(UpdateEventRequestDTO request, EventDetailDTO existing) {
        if (request.getName() == null || request.getName().isBlank()) {
            request.setName(existing.getName());
        }
    }

    private void checkDescription(UpdateEventRequestDTO request, EventDetailDTO existing) {
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            request.setDescription(existing.getDescription());
        }
    }

    private void checkTimeSlot(UpdateEventRequestDTO request, EventDetailDTO existing) {
        if (request.getTimeSlot() == null) {
            request.setTimeSlot(existing.getTimeSlot());
            return;
        }

        if (request.getTimeSlot().getStart() == null) {
            request.getTimeSlot().setStart(existing.getTimeSlot().getStart());
        }

        if (request.getTimeSlot().getEnd() == null) {
            request.getTimeSlot().setEnd(existing.getTimeSlot().getEnd());
        }
    }

    private void checkPlace(UpdateEventRequestDTO request, EventDetailDTO existing) {
        if (request.getPlace() == null) {
            request.setPlace(existing.getPlace());
        }
    }

    private void checkRankings(UpdateEventRequestDTO request, EventDetailDTO existing) {
        if (request.getRankings() == null || request.getRankings().isEmpty()) {
            request.setRankings(existing.getRankings());
        }
    }

    private void checkCompetitionId(UpdateEventRequestDTO request, EventDetailDTO existing) {
        if (request.getCompetitionId() == null) {
            request.setCompetitionId(competitionService.findByName(existing.getCompetitionName()).get().getId());
        }
    }
}