// DocumentDTO.java
package com.miage.pouleAPI.dtos.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Integer id;
    private String fileName;
    private String originalFileName;
    private String typeName;
    private Integer typeId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String contentType;
    private Long fileSize;
    private String description;
    private Boolean isEncrypted;
    private String downloadUrl;
}