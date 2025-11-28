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
public class SubscribeToId implements Serializable {

    @Column(name = "id")
    private Integer userId;

    @Column(name = "id_notification")
    private Integer notificationId;

}
