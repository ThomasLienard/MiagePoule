package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.dtos.team.CreateTeamRequestDTO;
import com.miage.pouleAPI.dtos.team.TeamDTO;
import com.miage.pouleAPI.dtos.team.UpdateTeamRequestDTO;

import java.util.List;
import java.util.Optional;

public interface TeamService {
    
    List<TeamDTO> findAll();
    
    Optional<TeamDTO> findById(Integer id);
    
    TeamDTO create(CreateTeamRequestDTO dto);
    
    TeamDTO update(Integer id, UpdateTeamRequestDTO dto);
    
    void delete(Integer id);
}
