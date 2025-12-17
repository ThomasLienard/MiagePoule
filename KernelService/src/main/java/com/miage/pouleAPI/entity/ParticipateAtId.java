package com.miage.pouleAPI.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ParticipateAtId implements Serializable {

    @Column(name = "id_team")
    private Integer teamId;

    @Column(name = "id_trial")
    private Integer trialId;

    // equals & hashCode
}

