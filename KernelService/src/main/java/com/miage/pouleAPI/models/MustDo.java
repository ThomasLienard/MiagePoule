package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "must_do")
public class MustDo {

    @EmbeddedId
    private MustDoId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "id")
    private ApplicationUser user;

    @ManyToOne
    @MapsId("taskId")
    @JoinColumn(name = "id_task")
    private Task task;


}
