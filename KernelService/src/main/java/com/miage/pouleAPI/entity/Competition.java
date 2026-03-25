package com.miage.pouleAPI.entity;

import com.miage.pouleAPI.users.Subject;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Competition")
public class Competition implements Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_competition")
    private Integer id;

    @Column(name = "name_competition", nullable = false, unique = true)
    private String name;

    @Column(name = "description_competition")
    private String description;

    @ManyToOne
    @JoinColumn(name = "id_championship", nullable = false)
    private Championship championship;

    @Column(name = "start_date_competition", nullable = false)
    private LocalDate start;

    @Column(name = "end_date_competition", nullable = false)
    private LocalDate end;

    @ManyToMany
    @JoinTable(
            name = "competition_observers",
            joinColumns = @JoinColumn(name = "id_competition"),    // FK vers Competition
            inverseJoinColumns = @JoinColumn(name = "id_user")      // FK vers ApplicationUser
    )
    private Collection<ApplicationUser> observers = new HashSet<>();

    public Competition(Integer id, String name, String description, Championship championship, LocalDate start, LocalDate end) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.championship = championship;
        this.start = start;
        this.end = end;
    }

    @Override
    public void attach(ApplicationUser user) {
        this.observers.add(user);
    }

    @Override
    public void detach(ApplicationUser user) {
        this.observers.remove(user);
    }

    @Override
    public void notifyObservers(Notification notif) {
        this.observers.forEach(observer -> observer.update(notif));
    }
}
