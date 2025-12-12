package com.miage.pouleAPI.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Trial")
public class Trial {

    @Id
    @Column(name = "id_trial")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_event", unique = true, nullable = false)
    @JsonIgnoreProperties({"tasks", "metrics", "users"})
    private Event event;
}
