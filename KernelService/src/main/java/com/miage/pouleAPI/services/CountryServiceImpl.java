package com.miage.pouleAPI.services;

import com.miage.pouleAPI.entity.Country;
import com.miage.pouleAPI.repositories.CountryRepository;
import com.miage.pouleAPI.services.interfaces.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {
    private final CountryRepository countryRepository;

    public List<String> getAllCountryCodes() {
        // On ne renvoie que les codes (ex: "FR", "US") pour le Select du front
        return countryRepository.findAll().stream()
                .map(Country::getCode)
                .toList();
    }
}