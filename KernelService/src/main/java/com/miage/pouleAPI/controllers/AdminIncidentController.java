package com.miage.pouleAPI.controllers;


import com.miage.pouleAPI.services.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/incident")
@RequiredArgsConstructor
public class AdminIncidentController {

    private final NotificationService notificationService;



}
