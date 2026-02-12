package com.miage.pouleAPI.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "competition_observers")
public class CompetitionObserver {

    @EmbeddedId
    private CompetitionObserverId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "id_user")
    private ApplicationUser user;

    @ManyToOne
    @MapsId("CompetitionId")
    @JoinColumn(name = "id_competition")
    private Competition competition;

}
