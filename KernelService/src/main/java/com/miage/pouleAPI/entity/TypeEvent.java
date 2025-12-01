package com.miage.pouleAPI.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "type_event")
public class TypeEvent {

    @Id
    @Column(name = "type_event_name")
    private String name;
}
