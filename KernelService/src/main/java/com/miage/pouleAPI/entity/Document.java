package com.miage.pouleAPI.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "document")
public class Document {

    @Id
    @Column(name = "id_doc")
    private Integer id;

    @Column(name = "file", nullable = false, columnDefinition = "BYTEA")
    private byte[] file;

    @Column(name = "encrypted_key", length = 500)
    private String encryptedKey;

    @Column(name = "encryption_iv", length = 100)
    private String encryptionIv;

    @Column(name = "encryption_algorithm")
    private String encryptionAlgorithm = "AES/GCM/NoPadding";

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_doc", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TypeOfDocument type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "documents"})
    private ApplicationUser user;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}