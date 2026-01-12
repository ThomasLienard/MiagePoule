package com.miage.pouleAPI.repositories.interfaces;

import com.miage.pouleAPI.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    @Query("SELECT e FROM Event e WHERE e.competition.id = :competitionId")
    List<Event> findByCompetitionId(@Param("competitionId") Integer competitionId);
}