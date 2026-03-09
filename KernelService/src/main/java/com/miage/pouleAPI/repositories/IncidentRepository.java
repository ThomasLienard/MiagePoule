package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Integer> {
    @Query("SELECT i FROM Incident i WHERE i.event.id = :eventId")
    List<Incident> findByEventId(@Param("eventId") Integer eventId);

    @Query("SELECT i FROM Incident i WHERE i.place.id = :placeId")
    List<Incident> findByPlaceId(@Param("placeId") Integer placeId);

    @Query("SELECT i FROM Incident i WHERE i.alertLevel.name = :alertLevelName")
    List<Incident> findByAlertLevel(@Param("alertLevelName") String alertLevelName);
}
