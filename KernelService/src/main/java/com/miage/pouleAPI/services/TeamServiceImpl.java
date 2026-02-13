package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.team.CreateTeamRequestDTO;
import com.miage.pouleAPI.dtos.team.TeamDTO;
import com.miage.pouleAPI.dtos.team.TeamMemberDTO;
import com.miage.pouleAPI.dtos.team.UpdateTeamRequestDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Country;
import com.miage.pouleAPI.entity.Team;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.CountryRepository;
import com.miage.pouleAPI.repositories.ParticipateAtRepository;
import com.miage.pouleAPI.repositories.TeamRepository;
import com.miage.pouleAPI.services.interfaces.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private static final String DOES_NOT_EXIST = " n'existe pas";

    private final TeamRepository teamRepository;
    private final CountryRepository countryRepository;
    private final ApplicationUserRepository userRepository;
    private final ParticipateAtRepository participateAtRepository;

    @Override
    public List<TeamDTO> findAll() {
        return teamRepository.findAllWithUsers().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public Optional<TeamDTO> findById(Integer id) {
        return teamRepository.findByIdWithUsers(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public TeamDTO create(CreateTeamRequestDTO dto) {
        // Vérifier que le pays existe
        Country country = countryRepository.findById(dto.getCountryCode())
                .orElseThrow(() -> new IllegalArgumentException("Le pays avec le code " + dto.getCountryCode() + DOES_NOT_EXIST));

        // Créer la nouvelle équipe
        Team team = new Team();
        team.setName(dto.getName());
        team.setCountry(country);
        team.setUsers(new HashSet<>());

        // Ajouter les membres si fournis
        if (dto.getMemberIds() != null && !dto.getMemberIds().isEmpty()) {
            Set<ApplicationUser> members = new HashSet<>();
            for (Integer userId : dto.getMemberIds()) {
                ApplicationUser user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("L'utilisateur avec l'ID " + userId + DOES_NOT_EXIST));
                members.add(user);
            }
            team.setUsers(members);
        }

        Team savedTeam = teamRepository.save(team);

        // Mettre à jour la relation bidirectionnelle
        if (!savedTeam.getUsers().isEmpty()) {
            for (ApplicationUser user : savedTeam.getUsers()) {
                user.getTeams().add(savedTeam);
                userRepository.save(user);
            }
        }

        return convertToDTO(savedTeam);
    }

    @Override
    @Transactional
    public TeamDTO update(Integer id, UpdateTeamRequestDTO dto) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("L'équipe avec l'ID " + id + DOES_NOT_EXIST));

        // Mettre à jour le nom
        team.setName(dto.getName());

        // Mettre à jour le pays
        Country country = countryRepository.findById(dto.getCountryCode())
                .orElseThrow(() -> new IllegalArgumentException("Le pays avec le code " + dto.getCountryCode() + DOES_NOT_EXIST));
        team.setCountry(country);

        // Gérer la mise à jour des membres
        Set<ApplicationUser> oldMembers = new HashSet<>(team.getUsers());
        Set<ApplicationUser> newMembers = new HashSet<>();

        if (dto.getMemberIds() != null) {
            for (Integer userId : dto.getMemberIds()) {
                ApplicationUser user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("L'utilisateur avec l'ID " + userId + DOES_NOT_EXIST));
                newMembers.add(user);
            }
        }

        // Retirer les anciens membres qui ne sont plus dans la nouvelle liste
        for (ApplicationUser oldMember : oldMembers) {
            if (!newMembers.contains(oldMember)) {
                oldMember.getTeams().remove(team);
                userRepository.save(oldMember);
            }
        }

        // Ajouter les nouveaux membres
        for (ApplicationUser newMember : newMembers) {
            if (!oldMembers.contains(newMember)) {
                newMember.getTeams().add(team);
                userRepository.save(newMember);
            }
        }

        team.setUsers(newMembers);
        Team updatedTeam = teamRepository.save(team);

        return convertToDTO(updatedTeam);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("L'équipe avec l'ID " + id + DOES_NOT_EXIST));

        // Supprimer toutes les participations de l'équipe aux épreuves
        participateAtRepository.deleteByTeamId(id);

        // Retirer l'équipe de tous les utilisateurs
        for (ApplicationUser user : team.getUsers()) {
            user.getTeams().remove(team);
            userRepository.save(user);
        }

        teamRepository.delete(team);
    }

    private TeamDTO convertToDTO(Team team) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setCountryCode(team.getCountry() != null ? team.getCountry().getCode() : null);
        dto.setMembers(team.getUsers().stream()
                .map(this::convertUserToMemberDTO)
                .collect(Collectors.toSet()));
        return dto;
    }
    
    private TeamMemberDTO convertUserToMemberDTO(ApplicationUser user) {
        TeamMemberDTO dto = new TeamMemberDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setLastname(user.getLastname());
        dto.setCountryCode(user.getCountry() != null ? user.getCountry().getCode() : null);
        return dto;
    }
}
