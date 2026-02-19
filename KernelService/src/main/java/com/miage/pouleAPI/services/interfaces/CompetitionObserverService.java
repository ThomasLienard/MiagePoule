package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.entity.CompetitionObserver;

import java.util.List;

public interface CompetitionObserverService {
    List<CompetitionObserver> getCompetitionObserversByUserId(Integer userId);
}
