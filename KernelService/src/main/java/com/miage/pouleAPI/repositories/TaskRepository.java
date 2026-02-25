package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query("SELECT DISTINCT t FROM Task t " +
           "JOIN t.users u " +
           "LEFT JOIN FETCH t.events e " +
           "LEFT JOIN FETCH e.timeSlot " +
           "LEFT JOIN FETCH e.place " +
           "WHERE u.id = :userId")
    List<Task> findTasksForUser(@Param("userId") Integer userId);

    @Query("SELECT DISTINCT t FROM Task t " +
           "JOIN t.users u " +
           "LEFT JOIN FETCH t.events e " +
           "LEFT JOIN FETCH e.timeSlot " +
           "LEFT JOIN FETCH e.place " +
           "WHERE u.id = :userId AND t.id = :taskId")
    Optional<Task> findTaskForUserById(@Param("userId") Integer userId, @Param("taskId") Integer taskId);
}
