package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.entity.Trial;
import com.miage.pouleAPI.services.interfaces.TrialService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("public/trials")
@CrossOrigin(origins = "http://localhost:5173")
public class TrialController {
    
    @Autowired
    private TrialService trialService;
    
    @GetMapping
    public List<Trial> getAllTrials() {
        return trialService.getAllTrials();
    }

    @GetMapping("/{eventId}")
    public Optional<Trial> getTrialsByEventId(@PathVariable Integer eventId) {
        return trialService.getTrialById(eventId);
    }
    
}
