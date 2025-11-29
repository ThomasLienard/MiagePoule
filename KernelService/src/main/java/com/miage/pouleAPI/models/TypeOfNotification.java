package com.miage.pouleAPI.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Type_of_notification")
public class TypeOfNotification {

    @Id
    @Column(name = "name_type_of_notification")
    private String name;

}
