package com.map.MapService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "Event")
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
    private TypeEvent typeEvent;

    @ManyToOne
    @JoinColumn(name = "id_place")
    private Place place;

    @ManyToOne
    @JoinColumn(name = "id_time_slot", nullable = false)
    private TimeSlot timeSlot;

    @ManyToOne
    @JoinColumn(name = "id_competition", nullable = false)
    private Competition competition;

    @ManyToMany(mappedBy = "events")
    private Set<ApplicationUser> users = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "is_associated_to",
        joinColumns = @JoinColumn(name = "id"),
        inverseJoinColumns = @JoinColumn(name = "id_task")
    )
    private Set<Task> tasks = new HashSet<>();

    @ManyToMany(mappedBy = "metricsEvents")
    private Set<Metrics> metrics = new HashSet<>();

}
