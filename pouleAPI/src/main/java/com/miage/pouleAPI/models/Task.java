package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

}
