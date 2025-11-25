package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class IsAssociatedToId implements Serializable {

    @Column(name = "id_event")
    private Integer eventId;

    @Column(name = "id_task")
    private Integer taskId;

}
