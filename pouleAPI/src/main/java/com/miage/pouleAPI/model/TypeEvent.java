package com.miage.pouleAPI.model;

import jakarta.persistence.*;

@Entity
public class TypeEvent {
    @Id
    private String typeEventName;

    public String getTypeEventName() {
        return typeEventName;
    }

    public void setTypeEventName(String typeEventName) {
        this.typeEventName = typeEventName;
    }
    
    
}