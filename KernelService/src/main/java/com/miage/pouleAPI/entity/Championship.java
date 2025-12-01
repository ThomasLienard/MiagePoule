package com.miage.pouleAPI.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "Championship")
public class Championship {

    @Id
    @Column(name = "id_championship")
    private Integer id;

    @Column(name = "description_championship", length = 1500)
    private String description;

    @Column(name = "name_championship", nullable = false)
    private String name;

    @Column(name = "start_date_championship", nullable = false)
    private LocalDate start;

    @Column(name = "end_date_championship", nullable = false)
    private LocalDate end;

}
