package com.miage.pouleAPI.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miage.pouleAPI.users.Observer;
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
@Table(name = "Application_user")
public class ApplicationUser implements Observer {

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

    // Champs pour la gestion des comptes par l'admin
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_account_activated")
    private Boolean isAccountActivated = false;

    @Column(name = "must_change_password")
    private Boolean mustChangePassword = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @Column(name = "deactivation_reason")
    private String deactivationReason;

    @ManyToOne
    @JoinColumn(name = "Country_code")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Country country;

    @ManyToOne
    @JoinColumn(name = "role_name", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Role role;

    @ManyToMany
    @JoinTable(
        name = "can_be_found_at",
        joinColumns = @JoinColumn(name = "id"),
        inverseJoinColumns = @JoinColumn(name = "id_geoloc")
    )
    @JsonIgnoreProperties({"users", "hibernateLazyInitializer", "handler"})
    @JsonIgnore
    private Set<Geoloc> geolocs = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "have_a_time_schedule",
        joinColumns = @JoinColumn(name = "id"),
        inverseJoinColumns = @JoinColumn(name = "id_event")
    )
    @JsonIgnoreProperties({"users", "tasks", "metrics", "hibernateLazyInitializer", "handler"})
    @JsonIgnore
    private Set<Event> events = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "is_a_part_of",
        joinColumns = @JoinColumn(name = "id"),
        inverseJoinColumns = @JoinColumn(name = "id_team")
    )
    @JsonIgnoreProperties({"users", "hibernateLazyInitializer", "handler"})
    private Set<Team> teams = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "must_do",
        joinColumns = @JoinColumn(name = "id"),
        inverseJoinColumns = @JoinColumn(name = "id_task")
    )
    @Column(name = "daily_tasks")
    @JsonIgnoreProperties({"events", "hibernateLazyInitializer", "handler"})
    @JsonIgnore
    private Set<Task> dailyTasks = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "Subscribe_to",
        joinColumns = @JoinColumn(name = "id"),
        inverseJoinColumns = @JoinColumn(name = "id_notification")
    )
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Set<Notification> notifications = new HashSet<>();

    @ManyToMany(mappedBy = "users")
    @JsonIgnoreProperties({"users", "metricsEvents", "hibernateLazyInitializer", "handler"})
    @JsonIgnore
    private Set<Metrics> metrics = new HashSet<>();

    @ManyToMany(mappedBy = "observers")
    private Set<Competition> observedCompetitions = new HashSet<>();

    @Override
    public void update(Notification notification) {

    }
}
