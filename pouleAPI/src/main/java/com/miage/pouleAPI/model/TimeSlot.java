package com.miage.pouleAPI.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTimeSlot;
    private Date startTime;
    private Date endTime;
    public Integer getIdTimeSlot() {
        return idTimeSlot;
    }
    public void setIdTimeSlot(Integer idTimeSlot) {
        this.idTimeSlot = idTimeSlot;
    }
    public Date getStartTime() {
        return startTime;
    }
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    public Date getEndTime() {
        return endTime;
    }
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    
}