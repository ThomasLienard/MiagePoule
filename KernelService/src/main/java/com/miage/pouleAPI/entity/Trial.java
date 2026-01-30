package com.miage.pouleAPI.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Trial")
@NoArgsConstructor
@Getter
@Setter
@PrimaryKeyJoinColumn(name = "id_event")
public class Trial extends Event {

}
