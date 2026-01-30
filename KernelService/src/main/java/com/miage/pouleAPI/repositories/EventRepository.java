package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    @Query("SELECT e FROM Event e WHERE e.competition.id = :competitionId")
    List<Event> findByCompetitionId(@Param("competitionId") Integer competitionId);
    
    @Query("SELECT e FROM Event e WHERE e.typeEvent.name != 'TRIAL'")
    List<Event> findByTypeEventNameNotEqual();
    
    @Query("SELECT e FROM Event e WHERE e.competition.id = :competitionId AND e.typeEvent.name != 'TRIAL'")
    List<Event> findByCompetitionIdAndTypeEventNameNotEqual(@Param("competitionId") Integer competitionId);
    Event findEventById(@Param("eventId") Integer eventId);
    List<Event> findByTimeSlotStartBeforeAndTimeSlotEndAfter(LocalDateTime now);
    List<Event> findByTimeSlotEndBetween(LocalDateTime start, LocalDateTime end);

}