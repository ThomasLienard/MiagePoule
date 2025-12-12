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
@Table(name = "Document")
public class Document {

    @Id
    @Column(name = "id_doc")
    private Integer id;

    @Lob
    @Column(name = "file", nullable = false)
    private byte[] file;

    @ManyToOne
    @JoinColumn(name = "id_type_doc", nullable = false)
    private TypeOfDocument type;

    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private ApplicationUser user;

}
