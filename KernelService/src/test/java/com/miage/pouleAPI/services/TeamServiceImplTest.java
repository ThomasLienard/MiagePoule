package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.team.CreateTeamRequestDTO;
import com.miage.pouleAPI.dtos.team.TeamDTO;
import com.miage.pouleAPI.dtos.team.UpdateTeamRequestDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Country;
import com.miage.pouleAPI.entity.Team;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.CountryRepository;
import com.miage.pouleAPI.repositories.TeamRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private ApplicationUserRepository userRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    private Country countryFR;
    private Country countryUS;
    private ApplicationUser athlete1;
    private ApplicationUser athlete2;
    private Team team;

    @BeforeEach
    void setUp() {
        countryFR = new Country();
        countryFR.setCode("FR");

        countryUS = new Country();
        countryUS.setCode("US");

        athlete1 = new ApplicationUser();
        athlete1.setId(1);
        athlete1.setName("John");
        athlete1.setLastname("Doe");
        athlete1.setCountry(countryUS);
        athlete1.setTeams(new HashSet<>());

        athlete2 = new ApplicationUser();
        athlete2.setId(2);
        athlete2.setName("Jane");
        athlete2.setLastname("Smith");
        athlete2.setCountry(countryFR);
        athlete2.setTeams(new HashSet<>());

        team = new Team();
        team.setId(1);
        team.setName("Team A");
        team.setCountry(countryFR);
        team.setUsers(new HashSet<>(Arrays.asList(athlete1, athlete2)));
    }

    @Test
    void findAll_ShouldReturnAllTeams() {
        List<Team> teams = Arrays.asList(team);
        when(teamRepository.findAllWithUsers()).thenReturn(teams);

        List<TeamDTO> result = teamService.findAll();

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Team A");
        assertThat(result.get(0).getCountryCode()).isEqualTo("FR");
        assertThat(result.get(0).getMembers()).hasSize(2);
        verify(teamRepository, times(1)).findAllWithUsers();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoTeams() {
        when(teamRepository.findAllWithUsers()).thenReturn(Collections.emptyList());

        List<TeamDTO> result = teamService.findAll();

        assertThat(result).isNotNull().isEmpty();
        verify(teamRepository, times(1)).findAllWithUsers();
    }

    @Test
    void findById_ShouldReturnTeam_WhenExists() {
        when(teamRepository.findByIdWithUsers(1)).thenReturn(Optional.of(team));

        Optional<TeamDTO> result = teamService.findById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getName()).isEqualTo("Team A");
        assertThat(result.get().getCountryCode()).isEqualTo("FR");
        assertThat(result.get().getMembers()).hasSize(2);
        verify(teamRepository, times(1)).findByIdWithUsers(1);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        when(teamRepository.findByIdWithUsers(999)).thenReturn(Optional.empty());

        Optional<TeamDTO> result = teamService.findById(999);

        assertThat(result).isEmpty();
        verify(teamRepository, times(1)).findByIdWithUsers(999);
    }

    @Test
    void create_ShouldCreateTeamWithMembers() {
        CreateTeamRequestDTO dto = new CreateTeamRequestDTO();
        dto.setName("New Team");
        dto.setCountryCode("FR");
        dto.setMemberIds(new HashSet<>(Arrays.asList(1, 2)));

        when(countryRepository.findById("FR")).thenReturn(Optional.of(countryFR));
        when(userRepository.findById(1)).thenReturn(Optional.of(athlete1));
        when(userRepository.findById(2)).thenReturn(Optional.of(athlete2));
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        TeamDTO result = teamService.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Team A");
        assertThat(result.getCountryCode()).isEqualTo("FR");
        assertThat(result.getMembers()).hasSize(2);

        verify(countryRepository, times(1)).findById("FR");
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(2);
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(userRepository, times(2)).save(any(ApplicationUser.class));
    }

    @Test
    void create_ShouldCreateTeamWithoutMembers() {
        CreateTeamRequestDTO dto = new CreateTeamRequestDTO();
        dto.setName("New Team");
        dto.setCountryCode("FR");
        dto.setMemberIds(new HashSet<>());

        Team emptyTeam = new Team();
        emptyTeam.setId(1);
        emptyTeam.setName("New Team");
        emptyTeam.setCountry(countryFR);
        emptyTeam.setUsers(new HashSet<>());

        when(countryRepository.findById("FR")).thenReturn(Optional.of(countryFR));
        when(teamRepository.save(any(Team.class))).thenReturn(emptyTeam);

        TeamDTO result = teamService.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Team");
        assertThat(result.getCountryCode()).isEqualTo("FR");
        assertThat(result.getMembers()).isEmpty();

        verify(countryRepository, times(1)).findById("FR");
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(userRepository, never()).findById(any());
    }

    @Test
    void create_ShouldThrowException_WhenCountryNotFound() {
        CreateTeamRequestDTO dto = new CreateTeamRequestDTO();
        dto.setName("New Team");
        dto.setCountryCode("XX");
        dto.setMemberIds(new HashSet<>());

        when(countryRepository.findById("XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Le pays avec le code XX n'existe pas");

        verify(countryRepository, times(1)).findById("XX");
        verify(teamRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenUserNotFound() {
        CreateTeamRequestDTO dto = new CreateTeamRequestDTO();
        dto.setName("New Team");
        dto.setCountryCode("FR");
        dto.setMemberIds(new HashSet<>(Arrays.asList(999)));

        when(countryRepository.findById("FR")).thenReturn(Optional.of(countryFR));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("L'utilisateur avec l'ID 999 n'existe pas");

        verify(countryRepository, times(1)).findById("FR");
        verify(userRepository, times(1)).findById(999);
        verify(teamRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateTeamSuccessfully() {
        UpdateTeamRequestDTO dto = new UpdateTeamRequestDTO();
        dto.setName("Updated Team");
        dto.setCountryCode("US");
        dto.setMemberIds(new HashSet<>(Arrays.asList(1)));

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(countryRepository.findById("US")).thenReturn(Optional.of(countryUS));
        when(userRepository.findById(1)).thenReturn(Optional.of(athlete1));

        Team updatedTeam = new Team();
        updatedTeam.setId(1);
        updatedTeam.setName("Updated Team");
        updatedTeam.setCountry(countryUS);
        updatedTeam.setUsers(new HashSet<>(Arrays.asList(athlete1)));

        when(teamRepository.save(any(Team.class))).thenReturn(updatedTeam);

        TeamDTO result = teamService.update(1, dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Team");
        assertThat(result.getCountryCode()).isEqualTo("US");
        assertThat(result.getMembers()).hasSize(1);

        verify(teamRepository, times(1)).findById(1);
        verify(countryRepository, times(1)).findById("US");
        verify(userRepository, times(1)).findById(1);
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    void update_ShouldThrowException_WhenTeamNotFound() {
        UpdateTeamRequestDTO dto = new UpdateTeamRequestDTO();
        dto.setName("Updated Team");
        dto.setCountryCode("FR");
        dto.setMemberIds(new HashSet<>());

        when(teamRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.update(999, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("L'équipe avec l'ID 999 n'existe pas");

        verify(teamRepository, times(1)).findById(999);
        verify(teamRepository, never()).save(any());
    }

    @Test
    void update_ShouldRemoveOldMembers() {
        UpdateTeamRequestDTO dto = new UpdateTeamRequestDTO();
        dto.setName("Updated Team");
        dto.setCountryCode("FR");
        dto.setMemberIds(new HashSet<>());

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(countryRepository.findById("FR")).thenReturn(Optional.of(countryFR));

        Team updatedTeam = new Team();
        updatedTeam.setId(1);
        updatedTeam.setName("Updated Team");
        updatedTeam.setCountry(countryFR);
        updatedTeam.setUsers(new HashSet<>());

        when(teamRepository.save(any(Team.class))).thenReturn(updatedTeam);

        TeamDTO result = teamService.update(1, dto);

        assertThat(result).isNotNull();
        assertThat(result.getMembers()).isEmpty();

        verify(userRepository, times(2)).save(any(ApplicationUser.class));
    }

    @Test
    void delete_ShouldDeleteTeamSuccessfully() {
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));

        teamService.delete(1);

        verify(teamRepository, times(1)).findById(1);
        verify(userRepository, times(2)).save(any(ApplicationUser.class));
        verify(teamRepository, times(1)).delete(team);
    }

    @Test
    void delete_ShouldThrowException_WhenTeamNotFound() {
        when(teamRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.delete(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("L'équipe avec l'ID 999 n'existe pas");

        verify(teamRepository, times(1)).findById(999);
        verify(teamRepository, never()).delete(any());
    }

    @Test
    void convertToDTO_ShouldMapAllFields() {
        when(teamRepository.findByIdWithUsers(1)).thenReturn(Optional.of(team));

        Optional<TeamDTO> result = teamService.findById(1);

        assertThat(result).isPresent();
        TeamDTO dto = result.get();
        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getName()).isEqualTo("Team A");
        assertThat(dto.getCountryCode()).isEqualTo("FR");
        assertThat(dto.getMembers()).hasSize(2);
        assertThat(dto.getMembers().stream().anyMatch(m -> m.getName().equals("John"))).isTrue();
        assertThat(dto.getMembers().stream().anyMatch(m -> m.getName().equals("Jane"))).isTrue();
    }
}
