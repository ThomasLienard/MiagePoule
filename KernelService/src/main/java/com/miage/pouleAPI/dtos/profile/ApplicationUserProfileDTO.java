package com.miage.pouleAPI.dtos.profile;

import com.miage.pouleAPI.entity.Country;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationUserProfileDTO {

    private Integer id;

    private String name;

    private String lastname;

    private String currentPassword;

    private String newPassword;

    private String email;

    private Country country;
}
