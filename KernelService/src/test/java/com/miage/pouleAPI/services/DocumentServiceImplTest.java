package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.document.DocumentDTO;
import com.miage.pouleAPI.dtos.document.DocumentResponse;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Document;
import com.miage.pouleAPI.entity.Role;
import com.miage.pouleAPI.entity.TypeOfDocument;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.DocumentRepository;
import com.miage.pouleAPI.repositories.TypeOfDocumentRepository;
import com.miage.pouleAPI.services.interfaces.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

    @Captor
    private ArgumentCaptor<Document> documentCaptor;

    @BeforeEach
    void setUp() {
        // Set @Value fields
        ReflectionTestUtils.setField(sut, "maxFileSize", 10_000_000L);
        ReflectionTestUtils.setField(sut, "allowedContentTypes", new String[]{"application/pdf", "image/jpeg", "image/png"});
    }

    @Test
    void uploadDocument_happyPath_returnsDocumentResponse() throws Exception {
        int userId = 1;
        int typeId = 2;
        byte[] fileBytes = "hello".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", fileBytes);

        // User avec rôle et compte activé
        ApplicationUser user = new ApplicationUser();
        user.setId(userId);
        user.setIsAccountActivated(true);
        Role role = new Role();
        role.setRoleName("ATHLETE");
        user.setRole(role);
        when(applicationUserRepository.findByIdAndIsActiveTrue(userId)).thenReturn(Optional.of(user));

        TypeOfDocument type = new TypeOfDocument();
        type.setId(typeId);
        type.setName("PASSPORT");
        when(typeOfDocumentRepository.findById(typeId)).thenReturn(Optional.of(type));

        when(documentRepository.countByUserId(userId)).thenReturn(0L);

        byte[] encrypted = "enc".getBytes();
        EncryptionService.EncryptedData encryptedData = new EncryptionService.EncryptedData(encrypted, "encKey", "iv");
        when(encryptionService.encrypt(fileBytes)).thenReturn(encryptedData);

        // Prepare saved document to be returned by repository.save
        Document saved = new Document();
        saved.setId(123);
        saved.setFileName("document_123.pdf");
        saved.setOriginalFileName("document.pdf");
        saved.setContentType("application/pdf");
        saved.setFileSize((long) fileBytes.length);
        saved.setIsEncrypted(true);
        saved.setType(type);
        saved.setUser(user);

        when(documentRepository.save(any(Document.class))).thenReturn(saved);

        DocumentResponse response = sut.uploadDocument(userId, file, typeId, "desc");

        assertNotNull(response);
        assertEquals("Document uploaded and encrypted successfully", response.getMessage());
        assertNotNull(response.getDocument());
        DocumentDTO dto = response.getDocument();
        assertEquals(saved.getId(), dto.getId());
        assertEquals(saved.getFileName(), dto.getFileName());
        assertEquals("PASSPORT", dto.getTypeName());
        assertEquals("application/pdf", dto.getContentType());

        verify(documentRepository).save(documentCaptor.capture());
        Document captured = documentCaptor.getValue();
        assertNull(captured.getId()); // L'ID ne doit pas être set manuellement
        assertTrue(captured.getIsEncrypted());
        assertEquals("AES/GCM/NoPadding", captured.getEncryptionAlgorithm());
    }

    @Test
    void uploadDocument_accountAlreadyValidated_throwsException() {
        int userId = 1;
        ApplicationUser user = new ApplicationUser();
        user.setId(userId);
        user.setIsAccountActivated(true);
        user.setIsAccountValidated(true); // Compte déjà validé
        Role role = new Role();
        role.setRoleName("ATHLETE");
        user.setRole(role);
        when(applicationUserRepository.findByIdAndIsActiveTrue(userId)).thenReturn(Optional.of(user));

        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> sut.uploadDocument(userId, file, 1, "desc"));
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertTrue(exception.getMessage().contains("account is already validated"));
    }


    @Test
    void uploadDocument_volunteerUploadingCENAccreditation_success() throws Exception {
        int userId = 1;
        int typeId = 4; // CEN_ACCREDITATION

        ApplicationUser user = new ApplicationUser();
        user.setId(userId);
        user.setIsAccountActivated(true);
        Role role = new Role();
        role.setRoleName("VOLONTAIRE");
        user.setRole(role);
        when(applicationUserRepository.findByIdAndIsActiveTrue(userId)).thenReturn(Optional.of(user));

        TypeOfDocument type = new TypeOfDocument();
        type.setId(typeId);
        type.setName("CEN_ACCREDITATION");
        when(typeOfDocumentRepository.findById(typeId)).thenReturn(Optional.of(type));

        when(documentRepository.countByUserId(userId)).thenReturn(0L);

        MockMultipartFile file = new MockMultipartFile("file", "accred.pdf", "application/pdf", "data".getBytes());
        EncryptionService.EncryptedData encryptedData = new EncryptionService.EncryptedData("enc".getBytes(), "key", "iv");
        when(encryptionService.encrypt(any())).thenReturn(encryptedData);

        // Retourner un document avec le type défini
        Document savedDoc = new Document();
        savedDoc.setId(1);
        savedDoc.setType(type);
        savedDoc.setUser(user);
        when(documentRepository.save(any())).thenReturn(savedDoc);

        assertDoesNotThrow(() -> sut.uploadDocument(userId, file, typeId, "desc"));
    }

    @Test
    void uploadDocument_athleteUploadingMedicalCertificate_success() throws Exception {
        int userId = 1;
        int typeId = 6; // MEDICAL_CERTIFICATE

        ApplicationUser user = new ApplicationUser();
        user.setId(userId);
        user.setIsAccountActivated(true);
        Role role = new Role();
        role.setRoleName("ATHLETE");
        user.setRole(role);
        when(applicationUserRepository.findByIdAndIsActiveTrue(userId)).thenReturn(Optional.of(user));

        TypeOfDocument type = new TypeOfDocument();
        type.setId(typeId);
        type.setName("MEDICAL_CERTIFICATE");
        when(typeOfDocumentRepository.findById(typeId)).thenReturn(Optional.of(type));

        when(documentRepository.countByUserId(userId)).thenReturn(0L);

        MockMultipartFile file = new MockMultipartFile("file", "medical.pdf", "application/pdf", "data".getBytes());
        EncryptionService.EncryptedData encryptedData = new EncryptionService.EncryptedData("enc".getBytes(), "key", "iv");
        when(encryptionService.encrypt(any())).thenReturn(encryptedData);

        // Retourner un document avec le type défini
        Document savedDoc = new Document();
        savedDoc.setId(2);
        savedDoc.setType(type);
        savedDoc.setUser(user);
        when(documentRepository.save(any())).thenReturn(savedDoc);

        assertDoesNotThrow(() -> sut.uploadDocument(userId, file, typeId, "desc"));
    }

    @Test
    void uploadDocument_volunteerUploadingPassport_throwsSecurityException() {
        int userId = 1;
        int typeId = 5; // PASSPORT

        ApplicationUser user = new ApplicationUser();
        user.setId(userId);
        user.setIsAccountActivated(true);
        Role role = new Role();
        role.setRoleName("VOLONTAIRE");
        user.setRole(role);
        when(applicationUserRepository.findByIdAndIsActiveTrue(userId)).thenReturn(Optional.of(user));

        TypeOfDocument type = new TypeOfDocument();
        type.setId(typeId);
        type.setName("PASSPORT");
        when(typeOfDocumentRepository.findById(typeId)).thenReturn(Optional.of(type));


        MockMultipartFile file = new MockMultipartFile("file", "passport.pdf", "application/pdf", "data".getBytes());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> sut.uploadDocument(userId, file, typeId, "desc"));
        assertTrue(exception.getCause() instanceof SecurityException);
        assertTrue(exception.getMessage().contains("Only ATHLETES can upload passports"));
    }

    @Test
    void uploadDocument_quotaExceeded_throwsException() {
        int userId = 1;
        ApplicationUser user = new ApplicationUser();
        user.setId(userId);
        user.setIsAccountActivated(true);
        Role role = new Role();
        role.setRoleName("ATHLETE");
        user.setRole(role);
        when(applicationUserRepository.findByIdAndIsActiveTrue(userId)).thenReturn(Optional.of(user));

        TypeOfDocument type = new TypeOfDocument();
        type.setId(1);
        type.setName("PASSPORT");
        when(typeOfDocumentRepository.findById(any())).thenReturn(Optional.of(type));
        when(documentRepository.countByUserId(userId)).thenReturn(20L); // Quota max

        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> sut.uploadDocument(userId, file, 1, "desc"));
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertTrue(exception.getMessage().contains("Document quota exceeded"));
    }

    @Test
    void uploadDocument_invalidFileType_throwsException() {
        int userId = 1;
        ApplicationUser user = new ApplicationUser();
        user.setId(userId);
        user.setIsAccountActivated(true);
        Role role = new Role();
        role.setRoleName("ATHLETE");
        user.setRole(role);
        when(applicationUserRepository.findByIdAndIsActiveTrue(userId)).thenReturn(Optional.of(user));

        MockMultipartFile file = new MockMultipartFile("file", "test.exe", "application/x-msdownload", "data".getBytes());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> sut.uploadDocument(userId, file, 1, "desc"));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertTrue(exception.getMessage().contains("Content type not allowed"));
    }

    @Test
    void downloadDocument_happyPath_returnsBytes() throws Exception {
        int userId = 1;
        int docId = 5;

        Document doc = new Document();
        doc.setId(docId);
        ApplicationUser u = new ApplicationUser();
        u.setId(userId);
        doc.setUser(u);
        doc.setFile("encbytes".getBytes());
        doc.setEncryptedKey("encKey");
        doc.setEncryptionIv("iv");

        when(documentRepository.findByUserIdAndId(userId, docId)).thenReturn(Optional.of(doc));

        byte[] plain = "plain".getBytes();
        when(encryptionService.decrypt(any())).thenReturn(plain);

        byte[] result = sut.downloadDocument(userId, docId);
        assertArrayEquals(plain, result);
    }

    @Test
    void downloadDocument_notFound_throwsEntityNotFound() {
        when(documentRepository.findByUserIdAndId(1, 1)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> sut.downloadDocument(1,1));
    }

    @Test
    void getUserDocuments_returnsAllUserDocuments() {
        int userId = 2;

        TypeOfDocument type1 = new TypeOfDocument();
        type1.setName("PASSPORT");
        TypeOfDocument type2 = new TypeOfDocument();
        type2.setName("ID_CARD");

        Document d1 = new Document();
        d1.setId(1);
        d1.setType(type1);
        Document d2 = new Document();
        d2.setId(2);
        d2.setType(type2);

        when(documentRepository.findByUserId(userId)).thenReturn(List.of(d1, d2));

        List<DocumentDTO> results = sut.getUserDocuments(userId);
        assertEquals(2, results.size());
    }

    @Test
    void getUserDocumentsByType_returnsFilteredDocuments() {
        int userId = 2;
        String typeName = "PASSPORT";

        TypeOfDocument type = new TypeOfDocument();
        type.setName(typeName);

        Document d1 = new Document();
        d1.setId(1);
        d1.setType(type);
        Document d2 = new Document();
        d2.setId(2);
        d2.setType(type);

        when(documentRepository.findByUserIdAndTypeName(userId, typeName)).thenReturn(List.of(d1, d2));

        List<DocumentDTO> results = sut.getUserDocumentsByType(userId, typeName);
        assertEquals(2, results.size());
    }

    @Test
    void getDocumentById_happyPath_returnsDocument() {
        int userId = 1;
        int docId = 5;

        TypeOfDocument type = new TypeOfDocument();
        type.setName("PASSPORT");

        Document doc = new Document();
        doc.setId(docId);
        doc.setFileName("test.pdf");
        doc.setType(type);

        when(documentRepository.findByUserIdAndId(userId, docId)).thenReturn(Optional.of(doc));

        DocumentDTO result = sut.getDocumentById(userId, docId);
        assertNotNull(result);
        assertEquals(docId, result.getId());
        assertEquals("test.pdf", result.getFileName());
    }

    @Test
    void getDocumentById_notFound_throwsEntityNotFound() {
        when(documentRepository.findByUserIdAndId(1, 1)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> sut.getDocumentById(1,1));
    }

    @Test
    void deleteDocument_happyPath_invokesDelete() {
        int userId = 1;
        int docId = 9;
        Document doc = new Document();
        doc.setId(docId);
        when(documentRepository.findByUserIdAndId(userId, docId)).thenReturn(Optional.of(doc));

        sut.deleteDocument(userId, docId);

        verify(documentRepository, times(1)).delete(doc);
    }

    @Test
    void deleteDocument_notFound_throws() {
        when(documentRepository.findByUserIdAndId(1,2)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> sut.deleteDocument(1,2));
    }

    @Test
    void getUserDocumentCount_delegatesToRepository() {
        when(documentRepository.countByUserId(3)).thenReturn(7L);
        assertEquals(7L, sut.getUserDocumentCount(3));
    }
}