package com.miage.pouleAPI.model;

import jakarta.persistence.*;

@Entity
public class TypeOfNotification {
    @Id
    private String nameTypeNotification;

    public String getNameTypeNotification() {
        return nameTypeNotification;
    }

    public void setNameTypeNotification(String nameTypeNotification) {
        this.nameTypeNotification = nameTypeNotification;
    }
    
    
}