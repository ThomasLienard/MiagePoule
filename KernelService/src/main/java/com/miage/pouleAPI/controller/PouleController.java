package com.miage.pouleAPI.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PouleController {

    @GetMapping("/my-events")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

}
