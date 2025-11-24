package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Application_user")
public class ApplicationUser {

    @Id
    @Column(name = "id")
    private Integer id;

    private String name;
    private String lastname;

    @Column(length = 100)
    private String password;

    private String email;

    @ManyToOne
    @JoinColumn(name = "Country_code")
    private Country country;

    @ManyToOne
    @JoinColumn(name = "role_name", nullable = false)
    private Role role;

}
