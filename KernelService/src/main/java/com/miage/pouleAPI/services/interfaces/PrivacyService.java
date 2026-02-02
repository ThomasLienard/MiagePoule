package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.dtos.profile.PrivacyDTO;

import java.util.List;

public interface PrivacyService {
    List<PrivacyDTO> getUserPrivacySettings(Integer userId);
    void updateSetting(Integer userId, String categoryName, boolean enabled);
}