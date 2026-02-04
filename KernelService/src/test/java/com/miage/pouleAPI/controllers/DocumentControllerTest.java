package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.document.DocumentDTO;
import com.miage.pouleAPI.dtos.document.DocumentResponse;
import com.miage.pouleAPI.dtos.document.DocumentUploadRequest;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.services.interfaces.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @InjectMocks
    private DocumentController sut;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
    }

    @Test
    void uploadTicket_happyPath_returnsCreated() {
        String email = "user@example.com";
        when(userDetails.getUsername()).thenReturn(email);
        ApplicationUser user = new ApplicationUser(); user.setId(12); user.setEmail(email);
        when(applicationUserRepository.findByEmail(email)).thenReturn(Optional.of(user));

        MockMultipartFile file = new MockMultipartFile("file", "ticket.pdf", "application/pdf", "data".getBytes());
        DocumentUploadRequest req = new DocumentUploadRequest();
        req.setFile(file);
        req.setTypeId(1);
        req.setDescription("desc");

        DocumentResponse resp = new DocumentResponse("ok", new DocumentDTO());
        when(documentService.uploadTicket(12, file, 1, "desc")).thenReturn(resp);

        ResponseEntity<DocumentResponse> response = sut.uploadTicket(userDetails, req);
        assertEquals(201, response.getStatusCode().value());
        assertEquals(resp, response.getBody());
    }

    @Test
    void getUserTickets_happyPath_returnsList() {
        String email = "a@b.com";
        when(userDetails.getUsername()).thenReturn(email);
        ApplicationUser user = new ApplicationUser(); user.setId(3); user.setEmail(email);
        when(applicationUserRepository.findByEmail(email)).thenReturn(Optional.of(user));

        List<DocumentDTO> list = List.of(new DocumentDTO());
        when(documentService.getUserTickets(3)).thenReturn(list);

        ResponseEntity<List<DocumentDTO>> response = sut.getUserTickets(userDetails);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(list, response.getBody());
    }

    @Test
    void downloadTicket_happyPath_setsHeadersAndBody() {
        String email = "user@example.com";
        when(userDetails.getUsername()).thenReturn(email);
        ApplicationUser user = new ApplicationUser(); user.setId(7); user.setEmail(email);
        when(applicationUserRepository.findByEmail(email)).thenReturn(Optional.of(user));

        byte[] fileContent = "pdfdata".getBytes();
        when(documentService.downloadTicket(7, 2)).thenReturn(fileContent);

        ResponseEntity<byte[]> response = sut.downloadTicket(userDetails, 2);
        assertEquals(200, response.getStatusCode().value());
        assertArrayEquals(fileContent, response.getBody());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains("ticket_2.pdf"));
    }

    @Test
    void deleteTicket_returnsNoContent() {
        String email = "x@x.com";
        when(userDetails.getUsername()).thenReturn(email);
        ApplicationUser user = new ApplicationUser(); user.setId(9); user.setEmail(email);
        when(applicationUserRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ResponseEntity<Void> resp = sut.deleteTicket(userDetails, 5);
        assertEquals(204, resp.getStatusCode().value());
        verify(documentService, times(1)).deleteTicket(9,5);
    }

    @Test
    void getDocumentCount_returnsValue() {
        String email = "a@b.com";
        when(userDetails.getUsername()).thenReturn(email);
        ApplicationUser user = new ApplicationUser(); user.setId(4); user.setEmail(email);
        when(applicationUserRepository.findByEmail(email)).thenReturn(Optional.of(user));

        when(documentService.getUserDocumentCount(4)).thenReturn(42L);
        ResponseEntity<Long> resp = sut.getDocumentCount(userDetails);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(42L, resp.getBody());
    }

    @Test
    void methods_withUnauthenticatedUser_throw() {
        assertThrows(IllegalArgumentException.class, () -> sut.getUserTickets(null));
    }
}
