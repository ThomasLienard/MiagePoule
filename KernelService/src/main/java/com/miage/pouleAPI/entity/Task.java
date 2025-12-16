package com.miage.pouleAPI.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Task")
public class Task {

    @Id
    @Column(name = "id_task")
    private Integer id;

    @Column(name = "task_name", nullable = false)
    private String name;

    @Column(name = "task_description", length = 1500)
    private String description;

    @ManyToMany(mappedBy = "tasks")
    private Set<Event> events = new HashSet<>();

    @ManyToMany(mappedBy = "dailyTasks")
    private Set<ApplicationUser> users = new HashSet<>();

}
