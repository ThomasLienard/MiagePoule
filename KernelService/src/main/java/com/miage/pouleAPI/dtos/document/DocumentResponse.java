// DocumentResponse.java
package com.miage.pouleAPI.dtos.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private String message;
    private DocumentDTO document;
}