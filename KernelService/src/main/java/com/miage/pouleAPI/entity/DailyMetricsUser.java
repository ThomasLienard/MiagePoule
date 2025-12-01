package com.miage.pouleAPI.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "Daily_metrics_user")
public class DailyMetricsUser {

    @Id
    @Column(name = "date_metric", nullable = false)
    private LocalDateTime dateMetric;

    @Column(name = "nb_connexions")
    private Integer nbConnexions;

    @Column(name = "time_on_the_app")
    private Double timeOnTheApp;

    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private ApplicationUser user;

    @ManyToMany(mappedBy = "dailyMetricsUsers")
    private Set<Metrics> metrics = new HashSet<>();
}
