package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "can_be_found_at")
public class CanBeFoundAt {

    @EmbeddedId
    private CanBeFoundAtId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "id")
    private ApplicationUser user;

    @ManyToOne
    @MapsId("geolocId")
    @JoinColumn(name = "id_geoloc")
    private Geoloc geoloc;

}
