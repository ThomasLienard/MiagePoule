package main.java.com.miage.pouleAPI.services.impl;

import java.util.List;

import com.miage.pouleAPI.entity.Trial;

import main.java.com.miage.pouleAPI.repositories.TrialRepository;
import main.java.com.miage.pouleAPI.services.TrialService;

@Service
public class TrialServiceImpl implements  TrialService {
    
    @Autowired
    private TrialRepository trialRepository;
    
    public List<Trial> getAllTrials() {
        return trialRepository.findAll();
    }

 

}
