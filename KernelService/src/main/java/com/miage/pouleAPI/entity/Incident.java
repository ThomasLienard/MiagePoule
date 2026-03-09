package com.miage.pouleAPI.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Incident")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incident")
    private Integer id;

    @Column(name = "title_incident", nullable = false)
    private String title;

    @Column(name = "description_incident", length = 2000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "name_alert_level", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AlertLevel alertLevel;

    @ManyToOne
    @JoinColumn(name = "id_event")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Event event;

    @ManyToOne
    @JoinColumn(name = "id_place")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Place place;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ApplicationUser createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
