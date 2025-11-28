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
public class IsAPartOfId implements Serializable {

    @Column(name = "id")
    private Integer userId;

    @Column(name = "id_team")
    private Integer teamId;

}
