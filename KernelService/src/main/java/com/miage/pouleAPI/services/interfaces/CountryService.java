package com.miage.pouleAPI.services.interfaces;

import com.miage.pouleAPI.dtos.competition.CompetitionDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CountryService {
    List<String> getAllCountryCodes();
}
