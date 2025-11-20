package com.miage.pouleAPI.model;

import jakarta.persistence.*;

@Entity
public class Championship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idChampionship;
    private String nameChampionship;
    private String descriptionChampionship;
    
    public Integer getIdChampionship() {
        return idChampionship;
    }
    public void setIdChampionship(Integer idChampionship) {
        this.idChampionship = idChampionship;
    }
    public String getNameChampionship() {
        return nameChampionship;
    }
    public void setNameChampionship(String nameChampionship) {
        this.nameChampionship = nameChampionship;
    }
    public String getDescriptionChampionship() {
        return descriptionChampionship;
    }
    public void setDescriptionChampionship(String descriptionChampionship) {
        this.descriptionChampionship = descriptionChampionship;
    }
}