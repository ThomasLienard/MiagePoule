package com.miage.pouleAPI.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Role {
    @Id
    private String roleName;

    @ManyToMany(mappedBy = "roles")
    private Set<ApplicationUser> users = new HashSet<>();
    // getters and setters
}