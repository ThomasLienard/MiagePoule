package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.document.DocumentDTO;
import com.miage.pouleAPI.dtos.document.DocumentResponse;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Document;
import com.miage.pouleAPI.entity.TypeOfDocument;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.DocumentRepository;
import com.miage.pouleAPI.repositories.TypeOfDocumentRepository;
import com.miage.pouleAPI.services.interfaces.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private TypeOfDocumentRepository typeOfDocumentRepository;

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private DocumentServiceImpl sut;

    @BeforeEach
    void setUp() {
        // Set @Value fields
        ReflectionTestUtils.setField(sut, "maxFileSize", 10_000_000L);
        ReflectionTestUtils.setField(sut, "allowedContentTypes", new String[]{"application/pdf"});
    }

    @Test
    void uploadTicket_happyPath_returnsDocumentResponse() throws Exception {
        int userId = 1;
        int typeId = 2;
        byte[] fileBytes = "hello".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "ticket.pdf", "application/pdf", fileBytes);

        ApplicationUser user = new ApplicationUser();
        user.setId(userId);
        when(applicationUserRepository.findByIdAndIsActiveTrue(userId)).thenReturn(Optional.of(user));

        TypeOfDocument type = new TypeOfDocument();
        type.setId(typeId);
        type.setName("TICKET");
        when(typeOfDocumentRepository.findById(typeId)).thenReturn(Optional.of(type));

        when(documentRepository.countByUserId(userId)).thenReturn(0L);

        byte[] encrypted = "enc".getBytes();
        EncryptionService.EncryptedData encryptedData = new EncryptionService.EncryptedData(encrypted, "encKey", "iv");
        when(encryptionService.encrypt(fileBytes)).thenReturn(encryptedData);

        // Prepare saved document to be returned by repository.save
        Document saved = new Document();
        saved.setId(123);
        saved.setFileName("ticket_123.pdf");
        saved.setOriginalFileName("ticket.pdf");
        saved.setContentType("application/pdf");
        saved.setFileSize((long) fileBytes.length);
        saved.setIsEncrypted(true);
        saved.setType(type);
        saved.setUser(user);

        when(documentRepository.save(any())).thenReturn(saved);

        DocumentResponse response = sut.uploadTicket(userId, file, typeId, "desc");

        assertNotNull(response);
        assertEquals("Ticket uploaded and encrypted successfully", response.getMessage());
        assertNotNull(response.getDocument());
        DocumentDTO dto = response.getDocument();
        assertEquals(saved.getId(), dto.getId());
        assertEquals(saved.getFileName(), dto.getFileName());
        assertEquals("TICKET", dto.getTypeName());
        assertEquals("application/pdf", dto.getContentType());
    }

    @Test
    void downloadTicket_happyPath_returnsBytes() throws Exception {
        int userId = 1;
        int docId = 5;

        Document doc = new Document();
        doc.setId(docId);
        ApplicationUser u = new ApplicationUser(); u.setId(userId);
        doc.setUser(u);
        doc.setFile("encbytes".getBytes());
        doc.setEncryptedKey("encKey");
        doc.setEncryptionIv("iv");

        when(documentRepository.findByUserIdAndId(userId, docId)).thenReturn(Optional.of(doc));

        byte[] plain = "plain".getBytes();
        when(encryptionService.decrypt(any())).thenReturn(plain);

        byte[] result = sut.downloadTicket(userId, docId);
        assertArrayEquals(plain, result);
    }

    @Test
    void downloadTicket_notFound_throwsEntityNotFound() {
        when(documentRepository.findByUserIdAndId(1, 1)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> sut.downloadTicket(1,1));
    }

    @Test
    void getUserTickets_combinesTypes() {
        int userId = 2;
        TypeOfDocument t1 = new TypeOfDocument(); t1.setId(1); t1.setName("TICKET");
        TypeOfDocument t2 = new TypeOfDocument(); t2.setId(2); t2.setName("EVENT_TICKET");
        TypeOfDocument t3 = new TypeOfDocument(); t3.setId(3); t3.setName("SEASON_PASS");

        Document d1 = new Document(); d1.setId(1); d1.setType(t1);
        Document d2 = new Document(); d2.setId(2); d2.setType(t2);
        Document d3 = new Document(); d3.setId(3); d3.setType(t3);

        when(documentRepository.findByUserIdAndTypeName(userId, "TICKET")).thenReturn(List.of(d1));
        when(documentRepository.findByUserIdAndTypeName(userId, "EVENT_TICKET")).thenReturn(List.of(d2));
        when(documentRepository.findByUserIdAndTypeName(userId, "SEASON_PASS")).thenReturn(List.of(d3));

        List<DocumentDTO> results = sut.getUserTickets(userId);
        assertEquals(3, results.size());
    }

    @Test
    void deleteTicket_happyPath_invokesDelete() {
        int userId = 1;
        int docId = 9;
        Document doc = new Document(); doc.setId(docId);
        when(documentRepository.findByUserIdAndId(userId, docId)).thenReturn(Optional.of(doc));

        sut.deleteTicket(userId, docId);

        verify(documentRepository, times(1)).delete(doc);
    }

    @Test
    void deleteTicket_notFound_throws() {
        when(documentRepository.findByUserIdAndId(1,2)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> sut.deleteTicket(1,2));
    }

    @Test
    void getUserDocumentCount_delegatesToRepository() {
        when(documentRepository.countByUserId(3)).thenReturn(7L);
        assertEquals(7L, sut.getUserDocumentCount(3));
    }
}
