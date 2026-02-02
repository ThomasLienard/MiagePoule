// DocumentUploadRequest.java
package com.miage.pouleAPI.dtos.document;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DocumentUploadRequest {
    private Integer typeId;
    private String description;
    private MultipartFile file;
}