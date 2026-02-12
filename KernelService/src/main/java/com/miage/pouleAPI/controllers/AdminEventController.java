package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.event.CreateEventRequestDTO;
import com.miage.pouleAPI.services.interfaces.AdminEventService;
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

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateEventRequestDTO request) {
        adminEventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}