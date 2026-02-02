package com.miage.pouleAPI.repositories;

import com.miage.pouleAPI.entity.PrivacySetting;
import com.miage.pouleAPI.entity.DataCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrivacySettingRepository extends JpaRepository<PrivacySetting, Long> {

    List<PrivacySetting> findByUserId(Integer userId);
    Optional<PrivacySetting> findByUserIdAndCategory(Integer userId, DataCategory category);
}