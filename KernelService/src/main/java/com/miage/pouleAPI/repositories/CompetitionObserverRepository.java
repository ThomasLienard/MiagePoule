package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.entity.CompetitionObserver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CompetitionObserverRepository extends JpaRepository<CompetitionObserver, Integer> {

    Collection<CompetitionObserver> findByCompetition(Competition competition);

    List<CompetitionObserver> findByUser(ApplicationUser user);
}
