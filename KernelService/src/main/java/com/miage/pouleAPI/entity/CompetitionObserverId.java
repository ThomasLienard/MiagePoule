package com.miage.pouleAPI.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class CompetitionObserverId implements Serializable {

    @Column(name = "id_competition")
    private Integer CompetitionId;

    @Column(name = "id_user")
    private Integer userId;
}
