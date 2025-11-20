package com.miage.pouleAPI.model;

import jakarta.persistence.*;

@Entity
public class TypeOfDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTypeDoc;
    private String nameTypeDoc;
    public Integer getIdTypeDoc() {
        return idTypeDoc;
    }
    public void setIdTypeDoc(Integer idTypeDoc) {
        this.idTypeDoc = idTypeDoc;
    }
    public String getNameTypeDoc() {
        return nameTypeDoc;
    }
    public void setNameTypeDoc(String nameTypeDoc) {
        this.nameTypeDoc = nameTypeDoc;
    }

    
}