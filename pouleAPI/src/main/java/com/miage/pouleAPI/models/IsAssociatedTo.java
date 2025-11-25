package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "is_associated_to")
public class IsAssociatedTo {

    @EmbeddedId
    private IsAssociatedToId id;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "id_event")
    private Event event;

    @ManyToOne
    @MapsId("taskId")
    @JoinColumn(name = "id_task")
    private Task task;

}

