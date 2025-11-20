package com.miage.pouleAPI.model;

import jakarta.persistence.*;

@Entity
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPlace;
    private String namePlace;
    private String cityPlace;
    private String zipCodePlace;
    private String streetPlace;
    private Boolean parkingPlace;
    private String numberPlace;
    private String descriptionPlace;
    private Double latitudePlace;
    private Double longitudePlace;
    
    public Integer getIdPlace() {
        return idPlace;
    }
    public void setIdPlace(Integer idPlace) {
        this.idPlace = idPlace;
    }
    public String getNamePlace() {
        return namePlace;
    }
    public void setNamePlace(String namePlace) {
        this.namePlace = namePlace;
    }
    public String getCityPlace() {
        return cityPlace;
    }
    public void setCityPlace(String cityPlace) {
        this.cityPlace = cityPlace;
    }
    public String getZipCodePlace() {
        return zipCodePlace;
    }
    public void setZipCodePlace(String zipCodePlace) {
        this.zipCodePlace = zipCodePlace;
    }
    public String getStreetPlace() {
        return streetPlace;
    }
    public void setStreetPlace(String streetPlace) {
        this.streetPlace = streetPlace;
    }
    public Boolean getParkingPlace() {
        return parkingPlace;
    }
    public void setParkingPlace(Boolean parkingPlace) {
        this.parkingPlace = parkingPlace;
    }
    public String getNumberPlace() {
        return numberPlace;
    }
    public void setNumberPlace(String numberPlace) {
        this.numberPlace = numberPlace;
    }
    public String getDescriptionPlace() {
        return descriptionPlace;
    }
    public void setDescriptionPlace(String descriptionPlace) {
        this.descriptionPlace = descriptionPlace;
    }
    public Double getLatitudePlace() {
        return latitudePlace;
    }
    public void setLatitudePlace(Double latitudePlace) {
        this.latitudePlace = latitudePlace;
    }
    public Double getLongitudePlace() {
        return longitudePlace;
    }
    public void setLongitudePlace(Double longitudePlace) {
        this.longitudePlace = longitudePlace;
    }
    
    
}