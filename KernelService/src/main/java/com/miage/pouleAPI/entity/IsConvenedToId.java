package com.miage.pouleAPI.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class IsConvenedToId implements Serializable {

    @Column(name = "id")
    private Integer userId;

    @Column(name = "id_trial")
    private Integer trialId;
}

