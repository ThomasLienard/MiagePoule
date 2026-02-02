package com.miage.pouleAPI.dtos.profile;

import lombok.Data;

@Data
public class UpdateProfileRequestDTO {
    private String name;
    private String lastname;
    private String email;
    private String countryCode;
}