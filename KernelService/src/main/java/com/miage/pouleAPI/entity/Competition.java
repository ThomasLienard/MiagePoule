package com.miage.pouleAPI.entity;

import com.miage.pouleAPI.users.Observer;
import com.miage.pouleAPI.users.Subject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Competition")
public class Competition implements Subject {

    @Id
    @Column(name = "id_competition")
    private Integer id;

    @Column(name = "name_competition", nullable = false)
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

    @Transient
    private List<Observer> observers = new ArrayList<>();

    @Override
    public void attach(Observer o) {
        this.observers.add(o);
    }

    @Override
    public void detach(Observer o) {
        this.observers.remove(o);
    }

    @Override
    public void notifyObservers(Notification notif) {
        this.observers.forEach(observer -> observer.update(notif));
    }
}
