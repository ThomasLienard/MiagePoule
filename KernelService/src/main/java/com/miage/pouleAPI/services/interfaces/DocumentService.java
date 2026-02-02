package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.dtos.document.DocumentDTO;
import com.miage.pouleAPI.dtos.document.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    DocumentResponse uploadTicket(Integer userId, MultipartFile file, Integer typeId, String description);
    List<DocumentDTO> getUserTickets(Integer userId);
    DocumentDTO getTicketById(Integer userId, Integer documentId);
    byte[] downloadTicket(Integer userId, Integer documentId);
    void deleteTicket(Integer userId, Integer documentId);
    long getUserDocumentCount(Integer userId);
}