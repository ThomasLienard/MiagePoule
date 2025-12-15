package com.miage.pouleAPI.dto.place;

import java.math.BigDecimal;

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
    private BigDecimal latitude;
    private BigDecimal longitude;
}

