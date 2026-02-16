package com.miage.pouleAPI.dtos.team;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberDTO {
    
    private Integer id;
    
    private String name;
    
    private String lastname;
    
    private String countryCode;
}
