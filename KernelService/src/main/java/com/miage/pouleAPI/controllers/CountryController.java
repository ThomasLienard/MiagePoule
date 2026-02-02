package com.miage.pouleAPI.controllers;

import com.miage.pouleAPI.services.interfaces.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
public class CountryController {
    private final CountryService countryService;

    @GetMapping
    public ResponseEntity<List<String>> getCountryCodes() {
        return ResponseEntity.ok(countryService.getAllCountryCodes());
    }
}