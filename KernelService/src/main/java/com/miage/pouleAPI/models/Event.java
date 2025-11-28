package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Event")
public class Event {

    @Id
    @Column(name = "id_event")
    private Integer id;

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


}
