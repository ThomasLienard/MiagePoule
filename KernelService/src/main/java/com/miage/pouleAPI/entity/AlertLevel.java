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
@Table(name = "alert_level")
public class AlertLevel {

    @Id
    @Column(name = "name_alert_level")
    private String name;
}
