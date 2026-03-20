package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    @Query("SELECT e FROM Event e WHERE e.competition.id = :competitionId")
    List<Event> findByCompetitionId(@Param("competitionId") Integer competitionId);

    @Query("SELECT e FROM Event e WHERE e.place.id = :placeId")
    List<Event> findByPlaceId(@Param("placeId") Integer placeId);
        @Query("SELECT e FROM Event e WHERE LOWER(e.competition.name) = LOWER(:competitionName) AND LOWER(e.name) = LOWER(:eventName)")
        List<Event> findByCompetitionNameAndEventName(
            @Param("competitionName") String competitionName,
            @Param("eventName") String eventName
        );

    @Query("SELECT e FROM Event e WHERE e.typeEvent.name != 'TRIAL'")
    List<Event> findByTypeEventNameNotEqual();

    @Query("SELECT e FROM Event e WHERE e.competition.id = :competitionId AND e.typeEvent.name != 'TRIAL'")
    List<Event> findByCompetitionIdAndTypeEventNameNotEqual(@Param("competitionId") Integer competitionId);
    Event findEventById(@Param("eventId") Integer eventId);

    @Query("SELECT e FROM Event e WHERE e.timeSlot.start < :now AND e.timeSlot.end > :now")
    List<Event> findOngoingEvents(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM Event e WHERE e.timeSlot.start between :start and :end")
    List<Event> findByTimeSlotStartBetween(LocalDateTime start, LocalDateTime end);

    List<Event> findByTimeSlotEndBetween(LocalDateTime start, LocalDateTime end);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO have_a_time_schedule (id, id_event) VALUES (:userId, :eventId)", nativeQuery = true)
    void linkCommissaireToEvent(@Param("userId") Integer userId, @Param("eventId") Integer eventId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM have_a_time_schedule WHERE id_event = :eventId", nativeQuery = true)
    void unlinkAllCommissairesFromEvent(@Param("eventId") Integer eventId);

    @Query(value = "SELECT id FROM have_a_time_schedule WHERE id_event = :eventId LIMIT 1", nativeQuery = true)
    Integer findCommissaireIdByEventId(@Param("eventId") Integer eventId);

}