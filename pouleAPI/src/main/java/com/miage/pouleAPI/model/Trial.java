package com.miage.pouleAPI.model;

import jakarta.persistence.*;

@Entity
public class Trial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTrial;

    @ManyToOne
    private Event event;

    @ManyToOne
    private TimeSlot timeSlot;

    public Integer getIdTrial() {
        return idTrial;
    }

    public void setIdTrial(Integer idTrial) {
        this.idTrial = idTrial;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    
}