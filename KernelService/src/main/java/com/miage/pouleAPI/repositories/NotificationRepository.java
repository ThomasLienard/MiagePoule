package com.miage.pouleAPI.repositories;


import com.miage.pouleAPI.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
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
