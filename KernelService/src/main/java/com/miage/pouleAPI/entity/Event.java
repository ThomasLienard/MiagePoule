package com.miage.pouleAPI.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Event")
@Inheritance(strategy = InheritanceType.JOINED)
public class Event {

    @Id
    @Column(name = "id_event")
    private Integer id;

    @Column(name = "name_event")
    private String name;

    @Column(name = "description_event", length = 1500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "type_event_name", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TypeEvent typeEvent;

    @ManyToOne
    @JoinColumn(name = "id_place")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Place place;

    @ManyToOne
    @JoinColumn(name = "id_time_slot", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TimeSlot timeSlot;

    @ManyToOne
    @JoinColumn(name = "id_competition", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "championship"})
    private Competition competition;

    @ManyToMany(mappedBy = "events")
    @JsonIgnore
    @JsonIgnoreProperties({"events", "geolocs", "teams", "dailyTasks", "notifications", "metrics", "password"})
    private Set<ApplicationUser> users = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "is_associated_to",
        joinColumns = @JoinColumn(name = "id"),
        inverseJoinColumns = @JoinColumn(name = "id_task")
    )
    @JsonIgnoreProperties({"events", "hibernateLazyInitializer", "handler"})
    private Set<Task> tasks = new HashSet<>();

    @ManyToMany(mappedBy = "metricsEvents")
    @JsonIgnoreProperties({"metricsEvents", "users", "hibernateLazyInitializer", "handler"})
    private Set<Metrics> metrics = new HashSet<>();
}
