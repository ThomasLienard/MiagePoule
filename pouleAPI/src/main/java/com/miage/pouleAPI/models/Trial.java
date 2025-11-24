package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Trial")
public class Trial {

    @Id
    @Column(name = "id_trial")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_event", unique = true, nullable = false)
    private Event event;

}
