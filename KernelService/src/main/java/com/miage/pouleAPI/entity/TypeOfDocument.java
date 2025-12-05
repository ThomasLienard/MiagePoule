package com.miage.pouleAPI.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "Type_of_document")
public class TypeOfDocument{

    @Id
    @Column(name = "id_type_doc")
    private Integer id;

    @Column(name = "name_type_doc", unique = true, nullable = false)
    private String name;

}
