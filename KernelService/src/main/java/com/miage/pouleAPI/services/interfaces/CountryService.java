package com.miage.pouleAPI.services.interfaces;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CountryService {
    List<String> getAllCountryCodes();
}
