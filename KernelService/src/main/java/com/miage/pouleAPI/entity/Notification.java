package com.miage.pouleAPI.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Notification")
public class Notification {

    @Id
    @Column(name = "id_notification")
    private Integer id;

    @Column(name = "description_notification", length = 1500, nullable = false)
    private String description;

    @Column(name = "emission_date", nullable = false)
    private LocalDateTime emissionDate;

    @ManyToOne
    @JoinColumn(name = "id_place")
    private Place place;

    @ManyToOne
    @JoinColumn(name = "id_event")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "name_severity", nullable = false)
    private Severity severity;

    @ManyToOne
    @JoinColumn(name = "name_type_of_notification", nullable = false)
    private TypeOfNotification type;

    @ManyToMany(mappedBy = "notifications")
    private Set<ApplicationUser> users = new HashSet<>();

    @ManyToMany(mappedBy = "metricsNotifications")
    private Set<Metrics> metrics = new HashSet<>();

}
