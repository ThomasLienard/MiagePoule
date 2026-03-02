package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.event.UpdateEventRequestDTO;
import com.miage.pouleAPI.services.interfaces.AdminEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/commissaire/events")
@RequiredArgsConstructor
public class CommissaireController {

    private final AdminEventService adminEventService;

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateEventDates(@PathVariable Integer id, 
                                                 @RequestBody @Valid UpdateEventRequestDTO request) {
        request.setId(id);
        adminEventService.updateEvent(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}