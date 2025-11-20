package com.miage.pouleAPI.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idNotification;
    private String descriptionNotification;
    private Date emissionDate;

    @ManyToOne
    private ApplicationUser user;

    @ManyToOne
    private TypeOfNotification typeOfNotification;

    @ManyToOne
    private Severity severity;

    public Integer getIdNotification() {
        return idNotification;
    }

    public void setIdNotification(Integer idNotification) {
        this.idNotification = idNotification;
    }

    public String getDescriptionNotification() {
        return descriptionNotification;
    }

    public void setDescriptionNotification(String descriptionNotification) {
        this.descriptionNotification = descriptionNotification;
    }

    public Date getEmissionDate() {
        return emissionDate;
    }

    public void setEmissionDate(Date emissionDate) {
        this.emissionDate = emissionDate;
    }

    public ApplicationUser getUser() {
        return user;
    }

    public void setUser(ApplicationUser user) {
        this.user = user;
    }

    public TypeOfNotification getTypeOfNotification() {
        return typeOfNotification;
    }

    public void setTypeOfNotification(TypeOfNotification typeOfNotification) {
        this.typeOfNotification = typeOfNotification;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    
}