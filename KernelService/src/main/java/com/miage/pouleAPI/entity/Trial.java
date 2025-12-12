package com.miage.pouleAPI.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
