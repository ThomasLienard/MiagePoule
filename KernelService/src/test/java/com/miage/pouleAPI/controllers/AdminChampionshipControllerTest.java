package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.championship.ChampionshipDTO;
import com.miage.pouleAPI.dtos.championship.CreateChampionshipRequestDTO;
import com.miage.pouleAPI.services.interfaces.ChampionshipService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminChampionshipControllerTest {

    @Mock
    private ChampionshipService championshipService;

    @InjectMocks
    private AdminChampionshipController adminChampionshipController;

    @Test
    void create_shouldReturn201() {
        CreateChampionshipRequestDTO req = new CreateChampionshipRequestDTO(
                "Ligue 1", "Desc", LocalDate.now(), LocalDate.now().plusMonths(9));

        ResponseEntity<Void> response = adminChampionshipController.create(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(championshipService).save(req);
    }

    @Test
    void update_shouldReturn202_AndCompleteFields() {
        Integer id = 1;
        ChampionshipDTO existing = new ChampionshipDTO("Desc Initiale", LocalDate.now(), id, "Nom Initial", LocalDate.now().plusDays(10));
        ChampionshipDTO updateReq = new ChampionshipDTO("", null, id, "", null);

        when(championshipService.findById(id)).thenReturn(Optional.of(existing));

        ResponseEntity<Void> response = adminChampionshipController.update(updateReq);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Nom Initial", updateReq.getName());
        verify(championshipService).update(updateReq);
    }
}