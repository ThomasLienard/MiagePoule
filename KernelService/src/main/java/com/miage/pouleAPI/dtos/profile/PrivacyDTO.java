package com.miage.pouleAPI.dtos.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrivacyDTO {
    private String categoryName;
    private String label;
    private String purpose;
    private String sharingLevel;
    private boolean mandatory;
    private boolean enabled;
}