package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "is_a_part_of")
public class IsAPartOf {

    @EmbeddedId
    private IsAPartOfId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "id")
    private ApplicationUser user;

    @ManyToOne
    @MapsId("teamId")
    @JoinColumn(name = "id_team")
    private Team team;

}
