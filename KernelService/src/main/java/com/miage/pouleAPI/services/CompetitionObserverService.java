package com.miage.pouleAPI.services;


import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.CompetitionObserver;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.CompetitionObserverRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompetitionObserverService {

    private final CompetitionObserverRepository competitionObserverRepository;
    private final ApplicationUserRepository applicationUserRepository;

    public CompetitionObserverService(CompetitionObserverRepository competitionObserverRepository, ApplicationUserRepository applicationUserRepository) {
        this.competitionObserverRepository = competitionObserverRepository;
        this.applicationUserRepository = applicationUserRepository;
    }

    public List<CompetitionObserver> getCompetitionObserversByUserId(Integer userId) {
        ApplicationUser user = applicationUserRepository.findById(userId).orElseThrow();

        return this.competitionObserverRepository.findByUser(user);
    }
}
