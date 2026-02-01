package com.miage.pouleAPI.services;

import com.miage.pouleAPI.auth.repository.ApplicationUserRepository;
import com.miage.pouleAPI.dtos.profile.PrivacyDTO;
import com.miage.pouleAPI.entity.ApplicationUser;
import com.miage.pouleAPI.entity.DataCategory;
import com.miage.pouleAPI.entity.PrivacySetting;
import com.miage.pouleAPI.repositories.PrivacySettingRepository;
import com.miage.pouleAPI.services.interfaces.PrivacyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrivacyServiceImpl implements PrivacyService {

    private final PrivacySettingRepository privacyRepo;
    private final ApplicationUserRepository userRepo;

    @Override
    @Transactional(readOnly = true)
    public List<PrivacyDTO> getUserPrivacySettings(Integer userId) {
        Map<DataCategory, Boolean> userChoices = privacyRepo.findByUserId(userId).stream()
                .collect(Collectors.toMap(PrivacySetting::getCategory, PrivacySetting::isEnabled));

        return Arrays.stream(DataCategory.values()).map(cat -> {
            PrivacyDTO dto = new PrivacyDTO();
            dto.setCategoryName(cat.name());
            dto.setLabel(cat.getLabel());
            dto.setPurpose(cat.getPurpose());
            dto.setSharingLevel(cat.getSharingLevel());
            dto.setMandatory(cat.isMandatory());

            dto.setEnabled(cat.isMandatory() || userChoices.getOrDefault(cat, true));

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateSetting(Integer userId, String categoryName, boolean enabled) {
        DataCategory cat = DataCategory.valueOf(categoryName);

        if (cat.isMandatory()) {
            throw new IllegalArgumentException("Impossible de modifier un traitement obligatoire.");
        }

        ApplicationUser user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        PrivacySetting setting = privacyRepo.findByUserIdAndCategory(userId, cat)
                .orElse(new PrivacySetting(user, cat, enabled));

        setting.setEnabled(enabled);
        privacyRepo.save(setting);
    }
}