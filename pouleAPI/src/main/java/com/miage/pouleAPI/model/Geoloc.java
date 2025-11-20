package com.miage.pouleAPI.model;

import jakarta.persistence.*;

@Entity
public class Geoloc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idGeoloc;
    private Double latitudeGeoloc;
    private Double longitudeGeoloc;
    public Integer getIdGeoloc() {
        return idGeoloc;
    }
    public void setIdGeoloc(Integer idGeoloc) {
        this.idGeoloc = idGeoloc;
    }
    public Double getLatitudeGeoloc() {
        return latitudeGeoloc;
    }
    public void setLatitudeGeoloc(Double latitudeGeoloc) {
        this.latitudeGeoloc = latitudeGeoloc;
    }
    public Double getLongitudeGeoloc() {
        return longitudeGeoloc;
    }
    public void setLongitudeGeoloc(Double longitudeGeoloc) {
        this.longitudeGeoloc = longitudeGeoloc;
    }
    
    
}