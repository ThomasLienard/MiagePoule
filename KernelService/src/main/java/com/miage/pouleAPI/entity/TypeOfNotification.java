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
@Table(name = "Type_of_notification")
public class TypeOfNotification {

    @Id
    @Column(name = "name_type_of_notification")
    private String name;

}
