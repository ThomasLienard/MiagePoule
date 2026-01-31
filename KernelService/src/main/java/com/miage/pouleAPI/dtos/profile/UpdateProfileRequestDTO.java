package com.miage.pouleAPI.dtos.profile;

import com.miage.pouleAPI.entity.Country;
import lombok.Data;

@Data
public class UpdateProfileRequestDTO {
    private String name;
    private String lastname;
    private String email;
    private Country country;
}