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
@Table(name = "is_convened_to")
public class IsConvenedTo {

    @EmbeddedId
    private IsConvenedToId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "id")
    private ApplicationUser user;

    @ManyToOne
    @MapsId("trialId")
    @JoinColumn(name = "id_trial")
    private Trial trial;

    @Column(name = "trial_result_athlete")
    private String result;

    @Column(name = "is_forfeit")
    private Boolean isForfeit = false;
}
