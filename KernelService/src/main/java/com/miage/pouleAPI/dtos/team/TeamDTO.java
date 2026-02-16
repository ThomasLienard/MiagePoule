package com.miage.pouleAPI.dtos.team;
import lombok.AllArgsConstructor; 
import lombok.Getter; 
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamDTO {

    private Integer id;
    
    private String name;
    
    private String countryCode;
    
    private Set<TeamMemberDTO> members;
}
