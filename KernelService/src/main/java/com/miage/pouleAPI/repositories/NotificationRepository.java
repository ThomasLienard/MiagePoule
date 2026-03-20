package com.miage.pouleAPI.repositories;


import com.miage.pouleAPI.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    @Query("SELECT n FROM Notification n WHERE n.event.id = :eventId")
    List<Notification> findByEventId(@Param("eventId") Integer eventId);

    @Query("SELECT n FROM Notification n WHERE n.place.id = :placeId")
    List<Notification> findByPlaceId(@Param("placeId") Integer placeId);

    @Query("SELECT n FROM Notification n WHERE n.severity.name = :severityName")
    List<Notification> findBySeverity(@Param("severityName") String severityName);


	interface NotificationTypeCountProjection {
		String getType();
		Long getCount();
	}

	@Query("""
		SELECT CAST(n.type AS string) AS type, COUNT(n) AS count
		FROM Notification n
		WHERE n.emissionDate >= :start AND n.emissionDate < :end
		GROUP BY n.type
		""")
	List<NotificationTypeCountProjection> countSentByTypeBetween(
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end
	);
}
