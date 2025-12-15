package com.miage.pouleAPI.dto.place;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDTO {
    private Integer id;
    private String name;
    private String description;
    private String street;
    private String number;
    private String city;
    private String zip;
    private Boolean parking;
    private Double latitude;
    private Double longitude;
}

