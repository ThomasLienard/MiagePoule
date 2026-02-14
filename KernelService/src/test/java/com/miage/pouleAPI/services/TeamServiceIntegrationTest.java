package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.team.CreateTeamRequestDTO;
import com.miage.pouleAPI.dtos.team.TeamDTO;
import com.miage.pouleAPI.dtos.team.TeamMemberDTO;
import com.miage.pouleAPI.dtos.team.UpdateTeamRequestDTO;
import com.miage.pouleAPI.services.interfaces.TeamService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TeamServiceIntegrationTest {

    @Autowired
    private TeamService teamService;

    @Test
    void shouldFindAllTeamsFromDataSql() {
        List<TeamDTO> teams = teamService.findAll();

        assertNotNull(teams);
        assertTrue(teams.size() >= 4, "Should have at least 4 teams from data.sql");

        assertTrue(teams.stream().anyMatch(t -> "Team A".equals(t.getName())));
        assertTrue(teams.stream().anyMatch(t -> "Team B".equals(t.getName())));
        assertTrue(teams.stream().anyMatch(t -> "Team C".equals(t.getName())));
        assertTrue(teams.stream().anyMatch(t -> "Team D".equals(t.getName())));
    }

    @Test
    void shouldFindTeamByIdWithMembersFromDataSql() {
        Optional<TeamDTO> teamOpt = teamService.findById(1);

        assertTrue(teamOpt.isPresent());
        TeamDTO team = teamOpt.get();

        assertEquals(1, team.getId());
        assertEquals("Team A", team.getName());
        assertEquals("FR", team.getCountryCode());
        
        // Team A has 1 member (user id 1) according to data.sql
        assertNotNull(team.getMembers());
        assertEquals(1, team.getMembers().size());
    }

    @Test
    void shouldFindTeamWithMultipleMembersFromDataSql() {
        Optional<TeamDTO> teamOpt = teamService.findById(2);

        assertTrue(teamOpt.isPresent());
        TeamDTO team = teamOpt.get();

        assertEquals(2, team.getId());
        assertEquals("Team B", team.getName());
        assertEquals("US", team.getCountryCode());
        
        // Team B has 1 member (user id 2) according to data.sql
        assertNotNull(team.getMembers());
        assertEquals(1, team.getMembers().size());
    }

    @Test
    void shouldCreateNewTeamWithMembers() {
        CreateTeamRequestDTO dto = new CreateTeamRequestDTO();
        dto.setName("Test Team " + System.currentTimeMillis());
        dto.setCountryCode("FR");
        Set<Integer> memberIds = new HashSet<>();
        memberIds.add(3); // Marie Athlete
        dto.setMemberIds(memberIds);

        TeamDTO created = teamService.create(dto);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getName().startsWith("Test Team"));
        assertEquals("FR", created.getCountryCode());
        assertEquals(1, created.getMembers().size());
        assertTrue(created.getMembers().stream().anyMatch(m -> m.getId().equals(3)));
    }

    @Test
    void shouldCreateTeamWithoutMembers() {
        CreateTeamRequestDTO dto = new CreateTeamRequestDTO();
        dto.setName("Empty Team " + System.currentTimeMillis());
        dto.setCountryCode("DE");
        dto.setMemberIds(new HashSet<>());

        TeamDTO created = teamService.create(dto);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getName().startsWith("Empty Team"));
        assertEquals("DE", created.getCountryCode());
        assertTrue(created.getMembers().isEmpty());
    }

    @Test
    void shouldUpdateTeamName() {
        // Find Team A from data.sql
        List<TeamDTO> teams = teamService.findAll();
        Optional<TeamDTO> teamAOpt = teams.stream()
                .filter(t -> "Team A".equals(t.getName()))
                .findFirst();
        
        assertTrue(teamAOpt.isPresent(), "Team A should exist in data.sql");
        TeamDTO teamA = teamAOpt.get();

        UpdateTeamRequestDTO dto = new UpdateTeamRequestDTO();
        dto.setName("Updated Team A");
        dto.setCountryCode("FR");
        dto.setMemberIds(teamA.getMembers().stream()
                .map(TeamMemberDTO::getId)
                .collect(java.util.stream.Collectors.toSet()));

        TeamDTO updated = teamService.update(teamA.getId(), dto);

        assertNotNull(updated);
        assertEquals("Updated Team A", updated.getName());
        assertEquals("FR", updated.getCountryCode());
    }

    @Test
    void shouldUpdateTeamCountry() {
        // Find Team C from data.sql
        List<TeamDTO> teams = teamService.findAll();
        Optional<TeamDTO> teamCOpt = teams.stream()
                .filter(t -> "Team C".equals(t.getName()))
                .findFirst();
        
        assertTrue(teamCOpt.isPresent(), "Team C should exist in data.sql");
        TeamDTO teamC = teamCOpt.get();

        UpdateTeamRequestDTO dto = new UpdateTeamRequestDTO();
        dto.setName("Team C");
        dto.setCountryCode("ES");
        dto.setMemberIds(new HashSet<>());

        TeamDTO updated = teamService.update(teamC.getId(), dto);

        assertNotNull(updated);
        assertEquals("Team C", updated.getName());
        assertEquals("ES", updated.getCountryCode());
    }

    @Test
    void shouldAddMembersToTeam() {
        // Find Team D from data.sql (should have no members initially)
        List<TeamDTO> teams = teamService.findAll();
        Optional<TeamDTO> teamDOpt = teams.stream()
                .filter(t -> "Team D".equals(t.getName()))
                .findFirst();
        
        assertTrue(teamDOpt.isPresent(), "Team D should exist in data.sql");
        TeamDTO teamD = teamDOpt.get();

        UpdateTeamRequestDTO dto = new UpdateTeamRequestDTO();
        dto.setName("Team D");
        dto.setCountryCode(teamD.getCountryCode());
        Set<Integer> memberIds = new HashSet<>();
        memberIds.add(3); // Marie Athlete
        memberIds.add(5); // John Doe
        dto.setMemberIds(memberIds);

        TeamDTO updated = teamService.update(teamD.getId(), dto);

        assertNotNull(updated);
        assertEquals(2, updated.getMembers().size());
        assertTrue(updated.getMembers().stream().anyMatch(m -> m.getId().equals(3)));
        assertTrue(updated.getMembers().stream().anyMatch(m -> m.getId().equals(5)));
    }

    @Test
    void shouldRemoveMembersFromTeam() {
        // Find Team A from data.sql (has member with id 1)
        List<TeamDTO> teams = teamService.findAll();
        Optional<TeamDTO> teamAOpt = teams.stream()
                .filter(t -> "Team A".equals(t.getName()))
                .findFirst();
        
        assertTrue(teamAOpt.isPresent(), "Team A should exist in data.sql");
        TeamDTO teamA = teamAOpt.get();

        UpdateTeamRequestDTO dto = new UpdateTeamRequestDTO();
        dto.setName("Team A");
        dto.setCountryCode("FR");
        dto.setMemberIds(new HashSet<>()); // Remove all members

        TeamDTO updated = teamService.update(teamA.getId(), dto);

        assertNotNull(updated);
        assertTrue(updated.getMembers().isEmpty());
    }

    @Test
    void shouldDeleteTeam() {
        CreateTeamRequestDTO createDto = new CreateTeamRequestDTO();
        createDto.setName("To Delete Team " + System.currentTimeMillis());
        createDto.setCountryCode("IT");
        createDto.setMemberIds(new HashSet<>());

        TeamDTO created = teamService.create(createDto);
        Integer teamId = created.getId();

        assertNotNull(teamId);

        teamService.delete(teamId);

        Optional<TeamDTO> deleted = teamService.findById(teamId);
        assertFalse(deleted.isPresent());
    }

    @Test
    void shouldThrowExceptionWhenCreatingTeamWithInvalidCountry() {
        CreateTeamRequestDTO dto = new CreateTeamRequestDTO();
        dto.setName("Invalid Team " + System.currentTimeMillis());
        dto.setCountryCode("XX");
        dto.setMemberIds(new HashSet<>());

        assertThrows(IllegalArgumentException.class, () -> teamService.create(dto));
    }

    @Test
    void shouldThrowExceptionWhenCreatingTeamWithInvalidMember() {
        CreateTeamRequestDTO dto = new CreateTeamRequestDTO();
        dto.setName("Invalid Team " + System.currentTimeMillis());
        dto.setCountryCode("FR");
        dto.setMemberIds(new HashSet<>(Set.of(9999)));

        assertThrows(IllegalArgumentException.class, () -> teamService.create(dto));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTeam() {
        UpdateTeamRequestDTO dto = new UpdateTeamRequestDTO();
        dto.setName("Non Existent");
        dto.setCountryCode("FR");
        dto.setMemberIds(new HashSet<>());

        assertThrows(IllegalArgumentException.class, () -> teamService.update(9999, dto));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTeam() {
        assertThrows(IllegalArgumentException.class, () -> teamService.delete(9999));
    }

    @Test
    void shouldReturnEmptyWhenFindingNonExistentTeam() {
        Optional<TeamDTO> team = teamService.findById(9999);
        assertFalse(team.isPresent());
    }
}
