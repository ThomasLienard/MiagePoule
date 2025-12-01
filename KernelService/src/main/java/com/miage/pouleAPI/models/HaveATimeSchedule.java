package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "have_a_time_schedule")
public class HaveATimeSchedule {

    @EmbeddedId
    private HaveATimeScheduleId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "id")
    private ApplicationUser user;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "id_event")
    private Event event;
}

