package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.dtos.document.DocumentDTO;
import com.miage.pouleAPI.dtos.document.DocumentResponse;
import com.miage.pouleAPI.dtos.document.DocumentUploadRequest;
import com.miage.pouleAPI.entity.Document;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.services.interfaces.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final ApplicationUserRepository applicationUserRepository;

    @PostMapping(value = "/tickets/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadTicket(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute DocumentUploadRequest request) {

        Integer userId = getUserIdFromUserDetails(userDetails);
        DocumentResponse response = documentService.uploadTicket(
                userId,
                request.getFile(),
                request.getTypeId(),
                request.getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<DocumentDTO>> getUserTickets(
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer userId = getUserIdFromUserDetails(userDetails);
        List<DocumentDTO> tickets = documentService.getUserTickets(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<DocumentDTO> getTicketById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("ticketId") Integer ticketId) {

        Integer userId = getUserIdFromUserDetails(userDetails);
        DocumentDTO ticket = documentService.getTicketById(userId, ticketId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/tickets/{ticketId}/download")
    public ResponseEntity<byte[]> downloadTicket(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("ticketId") Integer ticketId) {

        Integer userId = getUserIdFromUserDetails(userDetails);
        byte[] fileContent = documentService.downloadTicket(userId, ticketId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ticket_" + ticketId + ".pdf");
        headers.setContentLength(fileContent.length);
        headers.setCacheControl("no-cache, no-store, must-revalidate");

        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }

    @DeleteMapping("/tickets/{ticketId}")
    public ResponseEntity<Void> deleteTicket(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("ticketId") Integer ticketId) {

        Integer userId = getUserIdFromUserDetails(userDetails);
        documentService.deleteTicket(userId, ticketId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getDocumentCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer userId = getUserIdFromUserDetails(userDetails);
        long count = documentService.getUserDocumentCount(userId);
        return ResponseEntity.ok(count);
    }

    // Méthode utilitaire pour obtenir l'ID utilisateur depuis l'email
    private Integer getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("User not authenticated");
        }

        String email = userDetails.getUsername(); // Dans votre cas, c'est l'email
        return applicationUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email))
                .getId();
    }
}