package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.document.DocumentDTO;
import com.miage.pouleAPI.dtos.document.DocumentResponse;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.Document;
import com.miage.pouleAPI.entity.TypeOfDocument;
import com.miage.pouleAPI.repositories.ApplicationUserRepository;
import com.miage.pouleAPI.repositories.DocumentRepository;
import com.miage.pouleAPI.repositories.TypeOfDocumentRepository;
import com.miage.pouleAPI.services.interfaces.DocumentService;
import com.miage.pouleAPI.services.interfaces.EncryptionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final TypeOfDocumentRepository typeOfDocumentRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final EncryptionService encryptionService;

    @Value("${app.storage.max-file-size:10485760}")
    private long maxFileSize;

    @Value("${app.storage.allowed-content-types:application/pdf}")
    private String[] allowedContentTypes;

    @Override
    @Transactional
    public DocumentResponse uploadTicket(Integer userId, MultipartFile file, Integer typeId, String description) {
        try {
            // Validation
            validateFile(file);

            // Vérifier l'utilisateur
            ApplicationUser user = applicationUserRepository.findByIdAndIsActiveTrue(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Active user not found with id: " + userId));

            // Vérifier le type de document
            TypeOfDocument type = typeOfDocumentRepository.findById(typeId)
                    .orElseThrow(() -> new EntityNotFoundException("Document type not found with id: " + typeId));

            // Vérifier le quota
            checkUserQuota(userId);

            // Lire et chiffrer le fichier
            byte[] fileContent = file.getBytes();
            EncryptionService.EncryptedData encryptedData = encryptionService.encrypt(fileContent);

            // Créer et sauvegarder le document
            Document document = createDocument(file, encryptedData, type, user, description);
            Document savedDocument = documentRepository.save(document);

            // Convertir en DTO
            DocumentDTO documentDTO = mapToDTO(savedDocument);


            return new DocumentResponse("Ticket uploaded and encrypted successfully", documentDTO);

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload ticket: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DocumentDTO> getUserTickets(Integer userId) {

        // Maintenant chercher par type
        List<Document> ticketDocs = documentRepository.findByUserIdAndTypeName(userId, "TICKET");


        List<Document> eventTicketDocs = documentRepository.findByUserIdAndTypeName(userId, "EVENT_TICKET");


        List<Document> seasonPassDocs = documentRepository.findByUserIdAndTypeName(userId, "SEASON_PASS");

        // Combiner tous les types de tickets
        List<Document> allTicketDocuments = new ArrayList<>(ticketDocs);
        allTicketDocuments.addAll(eventTicketDocs);
        allTicketDocuments.addAll(seasonPassDocs);


        return allTicketDocuments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentDTO getTicketById(Integer userId, Integer documentId) {
        Document document = documentRepository.findByUserIdAndId(userId, documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        return mapToDTO(document);
    }

    @Override
    public byte[] downloadTicket(Integer userId, Integer documentId) {
        Document document = documentRepository.findByUserIdAndId(userId, documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        try {
            // Créer l'objet EncryptedData et déchiffrer
            EncryptionService.EncryptedData encryptedData = new EncryptionService.EncryptedData(
                    document.getFile(),
                    document.getEncryptedKey(),
                    document.getEncryptionIv()
            );

            return encryptionService.decrypt(encryptedData);

        } catch (Exception e) {
            throw new RuntimeException("Failed to download ticket: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteTicket(Integer userId, Integer documentId) {
        Document document = documentRepository.findByUserIdAndId(userId, documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        documentRepository.delete(document);
    }

    @Override
    public long getUserDocumentCount(Integer userId) {
        return documentRepository.countByUserId(userId);
    }

    // Méthodes privées
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + maxFileSize + " bytes");
        }

        boolean contentTypeAllowed = false;
        String fileContentType = file.getContentType();

        if (fileContentType != null) {
            for (String allowedType : allowedContentTypes) {
                if (allowedType.equals(fileContentType)) {
                    contentTypeAllowed = true;
                    break;
                }
            }
        }

        if (!contentTypeAllowed) {
            throw new IllegalArgumentException("Content type not allowed. Only PDF files are accepted.");
        }
    }

    private void checkUserQuota(Integer userId) {
        long documentCount = getUserDocumentCount(userId);
        if (documentCount >= 50) { // Limite de 50 documents par utilisateur
            throw new IllegalStateException("Document quota exceeded. Maximum 50 documents per user.");
        }
    }

    private Document createDocument(MultipartFile file, EncryptionService.EncryptedData encryptedData,
                                    TypeOfDocument type, ApplicationUser user, String description) {
        Document document = new Document();
        document.setId(generateDocumentId());
        document.setFileName(generateUniqueFileName(file.getOriginalFilename()));
        document.setOriginalFileName(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setDescription(description);
        document.setFile(encryptedData.getData());
        document.setEncryptedKey(encryptedData.getEncryptedKey());
        document.setEncryptionIv(encryptedData.getIv());
        document.setEncryptionAlgorithm("AES/GCM/NoPadding");
        document.setIsEncrypted(true);
        document.setType(type);
        document.setUser(user);

        return document;
    }

    private Integer generateDocumentId() {
        return Math.abs(UUID.randomUUID().hashCode());
    }

    private String generateUniqueFileName(String originalFileName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        if (originalFileName != null) {
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                String name = originalFileName.substring(0, lastDotIndex);
                String extension = originalFileName.substring(lastDotIndex);
                return name + "_" + timestamp + "_" + uuid + extension;
            }
        }

        return "ticket_" + timestamp + "_" + uuid + ".pdf";
    }

    private DocumentDTO mapToDTO(Document document) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setFileName(document.getFileName());
        dto.setOriginalFileName(document.getOriginalFileName());
        dto.setTypeName(document.getType().getName());
        dto.setTypeId(document.getType().getId());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        dto.setContentType(document.getContentType());
        dto.setFileSize(document.getFileSize());
        dto.setDescription(document.getDescription());
        dto.setIsEncrypted(document.getIsEncrypted());
        dto.setDownloadUrl("/api/documents/tickets/" + document.getId() + "/download");
        return dto;
    }
}