package com.miage.pouleAPI.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "Application_user")
public class ApplicationUser {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column
    private String name;
    @Column
    private String lastname;

    @Column(length = 100)
    private String password;

    @Column(unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "Country_code")
    private Country country;

    @ManyToOne
    @JoinColumn(name = "role_name", nullable = false)
    private Role role;

    @ManyToMany
    @JoinTable(
        name = "can_be_found_at",
        joinColumns = @JoinColumn(name = "id"),
        inverseJoinColumns = @JoinColumn(name = "id_geoloc")
    )
    private Set<Geoloc> geolocs = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "have_a_time_schedule",
        joinColumns = @JoinColumn(name = "id"),
        inverseJoinColumns = @JoinColumn(name = "id_event")
    )
    private Set<Event> events = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "is_a_part_of",
        joinColumns = @JoinColumn(name = "id"),
        inverseJoinColumns = @JoinColumn(name = "id_team")
    )
    private Set<Team> teams = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "must_do",
        joinColumns = @JoinColumn(name = "id"),
        inverseJoinColumns = @JoinColumn(name = "id_task")
    )
    @Column(name = "daily_tasks")
    private Set<Task> dailyTasks = new HashSet<>();


    @ManyToMany
    @JoinTable(
        name = "Subscribe_to",
        joinColumns = @JoinColumn(name = "id"),
        inverseJoinColumns = @JoinColumn(name = "id_notification")
    )
    private Set<Notification> notifications = new HashSet<>();
}
