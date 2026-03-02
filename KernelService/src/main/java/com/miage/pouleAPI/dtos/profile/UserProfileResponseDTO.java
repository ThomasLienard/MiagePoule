package com.miage.pouleAPI.dtos.profile;

import lombok.Data;

@Data
public class UserProfileResponseDTO {
    private Integer id;
    private String name;
    private String lastname;
    private String email;
    private String countryCode;
    private String role;
    private boolean hasSignedCharter;
    private boolean isAccountActivated;
    private boolean isAccountValidated;
}