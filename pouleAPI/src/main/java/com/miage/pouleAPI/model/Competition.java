package com.miage.pouleAPI.model;

import jakarta.persistence.*;

@Entity
public class Competition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCompetition;
    private String nameCompetition;
    private String descriptionCompetition;

    @ManyToOne
    private Championship championship;

    public Integer getIdCompetition() {
        return idCompetition;
    }

    public void setIdCompetition(Integer idCompetition) {
        this.idCompetition = idCompetition;
    }

    public String getNameCompetition() {
        return nameCompetition;
    }

    public void setNameCompetition(String nameCompetition) {
        this.nameCompetition = nameCompetition;
    }

    public String getDescriptionCompetition() {
        return descriptionCompetition;
    }

    public void setDescriptionCompetition(String descriptionCompetition) {
        this.descriptionCompetition = descriptionCompetition;
    }

    public Championship getChampionship() {
        return championship;
    }

    public void setChampionship(Championship championship) {
        this.championship = championship;
    }
    
    
}