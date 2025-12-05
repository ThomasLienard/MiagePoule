package main.java.com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.entity.Trial;
import com.miage.pouleAPI.service.TrialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("public/trials")
public class TrialController {
    
    @Autowired
    private TrialService trialService;
    
    @GetMapping
    public List<Trial> getAllTrials() {
        return trialService.getAllTrials();
    }
    
}
