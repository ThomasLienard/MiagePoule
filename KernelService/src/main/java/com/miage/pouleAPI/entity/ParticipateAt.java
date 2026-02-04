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
@Table(name = "participate_at")
public class ParticipateAt {

    @EmbeddedId
    private ParticipateAtId id;

    @ManyToOne
    @MapsId("teamId")
    @JoinColumn(name = "id_team")
    private Team team;

    @ManyToOne
    @MapsId("trialId")
    @JoinColumn(name = "id_trial")
    private Trial trial;

    @Column(name = "trial_result_team")
    private String result;

    @Column(name = "is_forfeit")
    private Boolean isForfeit = false;
}

