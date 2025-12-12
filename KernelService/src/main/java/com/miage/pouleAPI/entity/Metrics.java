package com.miage.pouleAPI.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Metrics")
public class Metrics {

    @Id
    @Column(name = "record_date", nullable = false)
    private LocalDateTime recordDate;

    @Column(name = "nb_connexions")
    private Integer nbConnexions;

    @Column(name = "avg_time_on_app")
    private Double avgTimeOnApp;

    @Column(name = "nb_sub_notif_event")
    private Integer nbSubNotifEvent;

    @Column(name = "nb_sent_notif_event")
    private Integer nbSentNotifEvent;

    @Column(name = "nb_sub_notif_security")
    private Integer nbSubNotifSecurity;

    @Column(name = "nb_sent_notif_security")
    private Integer nbSentNotifSecurity;

    @Column(name = "nb_sub_notif_result")
    private Integer nbSubNotifResult;

    @Column(name = "nb_sent_notif_result")
    private Integer nbSentNotifResult;

    @Column(name = "nb_sub_notif_incident")
    private Integer nbSubNotifIncident;

    @Column(name = "nb_sent_notif_incident")
    private Integer nbSentNotifIncident;

    @Column(name = "nb_volunteer")
    private Integer nbVolunteer;

    @ManyToMany
    @JoinTable(
        name = "includes",
        joinColumns = @JoinColumn(name = "record_date"),
        inverseJoinColumns = @JoinColumn(name = "id_event")
    )
    @Column(name = "events")
    private Set<Event> metricsEvents = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "is_about",
        joinColumns = @JoinColumn(name = "record_date"),
        inverseJoinColumns = @JoinColumn(name = "id")
    )
    private Set<ApplicationUser> users = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "is_used_in",
        joinColumns = @JoinColumn(name = "record_date"),
        inverseJoinColumns = @JoinColumn(name = "id_notification")
    )
    @Column(name = "notifications")
    private Set<Notification> metricsNotifications = new HashSet<>();


    @ManyToMany
    @JoinTable(
        name = "is_calculated_with",
        joinColumns = @JoinColumn(name = "record_date"),
        inverseJoinColumns = @JoinColumn(name = "date_metric")
    )
    @Column(name = "daily_metrics_users")
    private Set<DailyMetricsUser> dailyMetricsUsers = new HashSet<>();


}