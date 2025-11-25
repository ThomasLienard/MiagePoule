package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Role")
public class Role {

    @Id
    @Column(name = "role_name")
    private String roleName;

    
}
