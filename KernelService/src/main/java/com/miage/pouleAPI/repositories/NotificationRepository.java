package com.miage.pouleAPI.repositories;


import com.miage.pouleAPI.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
