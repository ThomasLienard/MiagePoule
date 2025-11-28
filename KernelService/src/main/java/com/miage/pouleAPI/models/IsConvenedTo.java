package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

}
